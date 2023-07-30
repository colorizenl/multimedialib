//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.xml.XmlEscapers;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.util.DateParser;
import nl.colorize.util.FileUtils;
import nl.colorize.util.LogHelper;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.Stopwatch;
import nl.colorize.util.cli.Arg;
import nl.colorize.util.cli.CommandLineArgumentParser;
import org.teavm.diagnostics.Problem;
import org.teavm.tooling.ConsoleTeaVMToolLog;
import org.teavm.tooling.TeaVMTargetType;
import org.teavm.tooling.TeaVMTool;
import org.teavm.tooling.TeaVMToolException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Transpiles MultimediaLib applications to JavaScript using TeaVM. After transpilation
 * has been completed, the generated JavaScript code is combined with the application's
 * resource files and copied to the output directory.
 */
public class TeaVMTranspilerTool {

    @Arg(name = "project")
    protected String projectName;

    @Arg(name = "main", usage = "Main class that acts as application entry point")
    protected String mainClassName;

    @Arg(name = "resources", usage = "Location of the application's resource files")
    protected File resourceDir;

    @Arg(name = "out")
    protected File outputDir;

    @Arg(usage = "Minifies the generated JavaScript, off by default")
    protected boolean minify;

    private static final ResourceFile INDEX_FILE = new ResourceFile("browser/index.html");
    private static final ResourceFile RESOURCES_LIST = new ResourceFile("browser/browser-resources.txt");
    private static final ResourceFile JS_LIST = new ResourceFile("browser/javascript-libraries.txt");
    private static final Logger LOGGER = LogHelper.getLogger(TeaVMTranspilerTool.class);

    private static final List<String> TEXT_FILE_TYPES = List.of(
        ".atlas",
        ".csv",
        ".fnt",
        ".glsl",
        ".json",
        ".md",
        ".properties",
        ".txt",
        ".yaml",
        ".yml",
        "-manifest"
    );

    private static final List<String> RESOURCE_FILE_TYPES = List.of(
        ".css",
        ".fbx",
        ".g3db",
        ".gif",
        ".gltf",
        ".jpg",
        ".mp3",
        ".ogg",
        ".png",
        ".svg",
        ".ttf",
        ".wav"
    );

    private static final List<String> KNOWN_MISSING_CLASSES = List.of(
        "[java.lang.System.exit(I)V]",
        "[java.lang.reflect.TypeVariable]",
        "[java.lang.Class.getGenericSuperclass()Ljava/lang/reflect/Type;]",
        "[java.util.Properties.load(Ljava/io/Reader;)V]"
    );

    public static void main(String[] argv) {
        CommandLineArgumentParser argParser = new CommandLineArgumentParser(TeaVMTranspilerTool.class);
        TeaVMTranspilerTool transpiler = argParser.parse(argv, TeaVMTranspilerTool.class);
        transpiler.run();
    }

    protected void run() {
        Preconditions.checkArgument(resourceDir.exists(),
            "Resource directory not found: " + resourceDir.getAbsolutePath());

        outputDir.mkdir();
        checkMainClass();

        try {
            copyResources();
            transpile();
            printSummary();
        } catch (TeaVMToolException | IOException e) {
            LOGGER.log(Level.SEVERE, "Transpiling failed", e);
        }
    }

    private void checkMainClass() {
        try {
            Class<?> mainClass = Class.forName(mainClassName);
            mainClass.getDeclaredMethod("main", String[].class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid main class: " + mainClassName, e);
        }
    }

    private void printSummary() throws IOException {
        long htmlSize = new File(outputDir, "index.html").length();
        long jsSize = new File(outputDir, "classes.js").length();
        long appFileSize = FileUtils.countDirectorySize(outputDir);

        LOGGER.info("HTML file size:                   " + FileUtils.formatFileSize(htmlSize));
        LOGGER.info("Transpiled JavaScript file size:  " + FileUtils.formatFileSize(jsSize));
        LOGGER.info("Total application file size:      " + FileUtils.formatFileSize(appFileSize));
        LOGGER.info("Results saved to " + outputDir.getAbsolutePath());
    }

    private void transpile() throws TeaVMToolException {
        Stopwatch timer = new Stopwatch();
        LOGGER.info("Transpiling " + projectName + " to JavaScript");

        TeaVMTool transpiler = new TeaVMTool();
        transpiler.setClassLoader(getClass().getClassLoader());
        transpiler.setDebugInformationGenerated(true);
        transpiler.setIncremental(false);
        transpiler.setLog(new ConsoleTeaVMToolLog(true));
        transpiler.setMainClass(mainClassName);
        transpiler.setObfuscated(minify);
        transpiler.setSourceMapsFileGenerated(!minify);
        transpiler.setTargetDirectory(outputDir);
        transpiler.setTargetType(TeaVMTargetType.JAVASCRIPT);
        transpiler.generate();

        for (Problem problem : transpiler.getProblemProvider().getProblems()) {
            if (shouldReport(problem)) {
                LOGGER.log(Level.WARNING, "Error while transpiling: " + format(problem));
            }
        }

        LOGGER.info("Transpilation took " + Math.round(timer.tock() / 1000f) + "s");
    }

    private boolean shouldReport(Problem problem) {
        String params = Arrays.toString(problem.getParams());
        return KNOWN_MISSING_CLASSES.stream()
            .noneMatch(entry -> params.equals(entry));
    }

    private String format(Problem problem) {
        String text = problem.getText() + " " + Arrays.toString(problem.getParams());
        if (problem.getLocation() != null) {
            text += " (" + problem.getLocation().getSourceLocation() + ")";
        }
        return text;
    }

    protected void copyResources() {
        List<ResourceFile> resourceFiles = new ArrayList<>();
        resourceFiles.addAll(gatherFrameworkResourceFiles());
        resourceFiles.addAll(gatherApplicationResourceFiles());
        resourceFiles.add(generateManifest(resourceFiles));

        LOGGER.info("Copying " + resourceFiles.size() + " resource files");
        
        List<ResourceFile> textFiles = new ArrayList<>();
        List<String> jsLibraries = copyJavaScriptLibraries();

        for (ResourceFile file : resourceFiles) {
            if (isFileType(file, TEXT_FILE_TYPES)) {
                textFiles.add(file);
            } else {
                copyBinaryResourceFile(file);
            }
        }

        rewriteHTML(INDEX_FILE, textFiles, jsLibraries);
    }

    private List<String> copyJavaScriptLibraries() {
        List<String> fileNames = new ArrayList<>();

        File libDir = new File(outputDir, "libraries");
        libDir.mkdir();

        for (String line : JS_LIST.readLines(Charsets.UTF_8)) {
            if (!line.isEmpty() && !line.startsWith("#")) {
                ResourceFile file = new ResourceFile("browser/lib/" + line);
                File outputFile = new File(libDir, line);
                copyBinaryResourceFile(file, outputFile);

                if (!line.endsWith(".map")) {
                    fileNames.add(line);
                }
            }
        }

        return fileNames;
    }

    private boolean isFileType(ResourceFile needle, List<String> haystack) {
        return haystack.stream()
            .anyMatch(type -> needle.getName().toLowerCase().endsWith(type));
    }

    private void rewriteHTML(ResourceFile file, List<ResourceFile> textFiles, List<String> jsLibraries) {
        File outputFile = getOutputFile(file);

        try (PrintWriter writer = new PrintWriter(outputFile, Charsets.UTF_8.displayName())) {
            for (String line : file.readLines(Charsets.UTF_8)) {
                line = line.replace("{project}", projectName);
                line = line.replace("{js-libraries}", generateScriptTags(jsLibraries));
                line = line.replace("{timestamp}", generateTimestampTag());
                if (line.trim().equals("{resources}")) {
                    line = generateTextResourceFilesHTML(textFiles);
                }
                writer.println(line);
            }
        } catch (IOException e) {
            throw new MediaException("Cannot write to file: " + outputFile.getAbsolutePath(), e);
        }
    }

    private String generateScriptTags(List<String> jsLibraries) {
        return jsLibraries.stream()
            .map(file -> "<script src=\"libraries/" + file + "\"></script>")
            .collect(Collectors.joining("\n"));
    }

    private String generateTimestampTag() {
        String timestamp = DateParser.format(new Date(), "yyyy-MM-dd HH:mm");
        return "<!-- Generated by Colorize MultimediaLib @ " + timestamp + " -->";
    }

    private String generateTextResourceFilesHTML(List<ResourceFile> files) {
        StringBuilder buffer = new StringBuilder();

        for (ResourceFile file : files) {
            String id = normalizeFileName(file).replace(".", "_");
            String contents = file.read(Charsets.UTF_8);

            buffer.append("<div id=\"" + id + "\">");
            buffer.append(XmlEscapers.xmlContentEscaper().escape(contents));
            buffer.append("</div>\n");
        }

        return buffer.toString();
    }

    private void copyBinaryResourceFile(ResourceFile file, File outputFile) {
        try (InputStream stream = file.openStream()) {
            byte[] contents = stream.readAllBytes();
            FileUtils.write(contents, outputFile);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot copy file " + file, e);
        }
    }

    private void copyBinaryResourceFile(ResourceFile file) {
        File outputFile = getOutputFile(file);
        copyBinaryResourceFile(file, outputFile);
    }

    private List<ResourceFile> gatherFrameworkResourceFiles() {
        return RESOURCES_LIST.readLines(Charsets.UTF_8).stream()
            .filter(line -> !line.isEmpty() && !line.startsWith("#"))
            .map(ResourceFile::new)
            .toList();
    }

    private List<ResourceFile> gatherApplicationResourceFiles() {
        try {
            return Files.walk(resourceDir.toPath())
                .map(path -> path.toFile())
                .filter(file -> !file.isDirectory() && !file.getName().startsWith("."))
                .filter(file -> !file.getAbsolutePath().contains("/lib/"))
                .map(ResourceFile::new)
                .toList();
        } catch (IOException e) {
            throw new MediaException("Cannot read resource files directory: " + resourceDir, e);
        }
    }

    private ResourceFile generateManifest(List<ResourceFile> resourceFiles) {
        try {
            File tempDir = Files.createTempDirectory("resource-file-manifest").toFile();
            File manifestFile = new File(tempDir, "resource-file-manifest");

            List<String> entries = resourceFiles.stream()
                .map(file -> normalizeFileName(file))
                .filter(file -> !file.endsWith(".js"))
                .distinct()
                .sorted()
                .toList();

            Files.write(manifestFile.toPath(), entries, Charsets.UTF_8);

            return new ResourceFile(manifestFile);
        } catch (IOException e) {
            throw new MediaException("Cannot generate resource file manifest", e);
        }
    }

    private File getOutputFile(ResourceFile file) {
        if (isFileType(file, RESOURCE_FILE_TYPES)) {
            File resourcesDir = new File(outputDir, "resources");
            resourcesDir.mkdir();
            return new File(resourcesDir, normalizeFileName(file));
        } else {
            return new File(outputDir, normalizeFileName(file));
        }
    }

    private String normalizeFileName(ResourceFile file) {
        return file.getName().replace("/", "_");
    }
}
