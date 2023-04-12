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
import nl.colorize.util.FileUtils;
import nl.colorize.util.LogHelper;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.Stopwatch;
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

    protected String projectName;
    protected File resourceDir;
    protected String mainClassName;
    protected File outputDir;
    protected boolean minify;

    private static final ResourceFile INDEX_FILE = new ResourceFile("browser/index.html");
    private static final ResourceFile JS_LIBRARY_LIST = new ResourceFile("browser/lib/js-libraries.txt");
    private static final Logger LOGGER = LogHelper.getLogger(TeaVMTranspilerTool.class);

    private static final List<String> FRAMEWORK_RESOURCES = List.of(
        "colorize-logo.png",
        "colorize-icon-32.png",
        "colorize-icon-256.png",
        "browser/multimedialib.js",
        "browser/pixi-interface.js",
        "browser/three-interface.js",
        "browser/assets/multimedialib.css",
        "browser/assets/favicon.png",
        "browser/assets/apple-icon.png",
        "browser/assets/loading.gif",
        "browser/assets/OpenSans-Regular.ttf",
        "effects/particle-circle.png",
        "effects/particle-diamond.png",
        "demo/demo.png",
        "demo/demo.mp3",
        "demo/colorize-logo.gltf",
        "demo/sepia-vertex.glsl",
        "demo/sepia-fragment.glsl"
    );

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
        CommandLineArgumentParser args = new CommandLineArgumentParser(TeaVMTranspilerTool.class)
            .addRequired("--project", "Project name for the application")
            .addRequired("--main", "Main class that acts as application entry point")
            .addRequired("--resources", "Location of the application's resource files")
            .addRequired("--out", "Output directory for the generated files")
            .addFlag("--minify", "Minifies the generated JavaScript, off by default")
            .parseArgs(argv);

        TeaVMTranspilerTool tool = new TeaVMTranspilerTool();
        tool.projectName = args.get("project").getString();
        tool.mainClassName = args.get("main").getString();
        tool.resourceDir = args.get("resources").getDir();
        tool.outputDir = args.get("out").getFile();
        tool.minify = args.get("minify").getBool();
        tool.run();
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
        } catch (TeaVMToolException e) {
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

    private void printSummary() {
        long htmlSize = new File(outputDir, "index.html").length();
        long jsSize = new File(outputDir, "classes.js").length();

        LOGGER.info("HTML file size:                     " + FileUtils.formatFileSize(htmlSize));
        LOGGER.info("Transpiled JavaScript file size:    " + FileUtils.formatFileSize(jsSize));
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

        LOGGER.info("Transpilation took " + String.format("%.1f", timer.tock() / 1000f) + "s");
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
        FRAMEWORK_RESOURCES.forEach(path -> resourceFiles.add(new ResourceFile(path)));
        resourceFiles.addAll(gatherResourceFiles());
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

        for (String entry : JS_LIBRARY_LIST.readLines(Charsets.UTF_8)) {
            ResourceFile file = new ResourceFile("browser/lib/" + entry);
            File outputFile = new File(libDir, entry);
            copyBinaryResourceFile(file, outputFile);

            if (!entry.endsWith(".map")) {
                fileNames.add(entry);
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

    private List<ResourceFile> gatherResourceFiles() {
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
