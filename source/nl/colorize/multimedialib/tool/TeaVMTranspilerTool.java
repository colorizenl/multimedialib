//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.xml.XmlEscapers;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.util.DateParser;
import nl.colorize.util.FileUtils;
import nl.colorize.util.LogHelper;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.Stopwatch;
import nl.colorize.util.cli.Arg;
import nl.colorize.util.cli.CommandLineArgumentParser;
import nl.colorize.util.cli.CommandLineInterfaceException;
import nl.colorize.util.http.URLLoader;
import nl.colorize.util.http.URLResponse;
import org.teavm.diagnostics.DefaultProblemTextConsumer;
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

    @Arg(name = "project", usage = "Project display name")
    protected String projectName;

    @Arg(name = "main", usage = "Main class that acts as application entry point")
    protected String mainClassName;

    @Arg(name = "resources", usage = "Location of the application's resource files")
    protected File resourceDir;

    @Arg(name = "out", usage = "Output directory for transpiled application")
    protected File outputDir;

    @Arg(usage = "Build ID used for caching resource files, default is random")
    protected String buildId = String.valueOf(System.currentTimeMillis());

    @Arg(usage = "Minifies the generated JavaScript, off by default")
    protected boolean minify;

    @Arg(usage = "Inserts <meta> tags into the HTML, passed as name=value.", required = false)
    protected String meta;

    @Arg(usage = "Overrides the application with the demo application, for testing purposes.")
    protected boolean demo;

    private static final ResourceFile INDEX_FILE = new ResourceFile("browser/index.html");
    private static final ResourceFile RESOURCES_LIST = new ResourceFile("browser/browser-resources.txt");
    private static final ResourceFile JS_LIST = new ResourceFile("browser/javascript-libraries.txt");
    private static final String SCRIPT_FILE_NAME = "script-" + System.currentTimeMillis() + ".js";
    private static final List<String> EXPECTED_RESOURCES = List.of("favicon.png", "apple-favicon.png");
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

    public static void main(String[] argv) {
        CommandLineArgumentParser argParser = new CommandLineArgumentParser("TeaVMTranspilerTool");
        TeaVMTranspilerTool transpiler = argParser.parse(argv, TeaVMTranspilerTool.class);
        transpiler.run();
    }

    protected void run() {
        Preconditions.checkArgument(resourceDir.exists(),
            "Resource directory not found: " + resourceDir.getAbsolutePath());

        outputDir.mkdir();
        checkMainClass();

        try {
            cleanOldScripts();
            copyResources();
            transpile();
            printSummary();
        } catch (TeaVMToolException | IOException e) {
            LOGGER.log(Level.SEVERE, "Transpiling failed", e);
        }
    }

    private void checkMainClass() {
        if (demo) {
            mainClassName = TeaDemoLauncher.class.getName();
        }

        try {
            Class<?> mainClass = Class.forName(mainClassName);
            mainClass.getDeclaredMethod("main", String[].class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid main class: " + mainClassName, e);
        }
    }

    private void printSummary() throws IOException {
        long htmlSize = new File(outputDir, "index.html").length();
        long jsSize = getScriptFile().length();
        long resourceSize = FileUtils.countDirectorySize(new File(outputDir, "resources"));

        LOGGER.info("HTML file size:                   " + FileUtils.formatFileSize(htmlSize));
        LOGGER.info("Transpiled JavaScript file size:  " + FileUtils.formatFileSize(jsSize));
        LOGGER.info("Resource file size:               " + FileUtils.formatFileSize(resourceSize));
        LOGGER.info("Results saved to " + outputDir.getAbsolutePath());
    }

    private void transpile() throws TeaVMToolException {
        Stopwatch timer = new Stopwatch();
        LOGGER.info("Transpiling " + projectName + " to JavaScript");

        TeaVMTool transpiler = new TeaVMTool();
        transpiler.setClassLoader(getClass().getClassLoader());
        transpiler.setIncremental(false);
        transpiler.setLog(new ConsoleTeaVMToolLog(true));
        transpiler.setMainClass(mainClassName);
        transpiler.setEntryPointName("main");
        transpiler.setObfuscated(minify);
        transpiler.setSourceMapsFileGenerated(!minify);
        transpiler.setTargetDirectory(outputDir);
        transpiler.setTargetType(TeaVMTargetType.JAVASCRIPT);
        transpiler.setTargetFileName(SCRIPT_FILE_NAME);
        transpiler.generate();

        checkTranspilerOutput(transpiler);

        LOGGER.info("Transpilation took " + Math.round(timer.tock() / 1000f) + "s");
    }

    private void checkTranspilerOutput(TeaVMTool transpiler) {
        for (Problem problem : transpiler.getProblemProvider().getProblems()) {
            String details = format(problem);
            LOGGER.warning("TeaVM transpiler warning:\n" + details);
        }

        for (Problem problem : transpiler.getProblemProvider().getSevereProblems()) {
            String details = format(problem);
            throw new UnsupportedOperationException("TeaVM transpiler error:\n" + details);
        }
    }

    private String format(Problem problem) {
        DefaultProblemTextConsumer textRenderer = new DefaultProblemTextConsumer();
        problem.render(textRenderer);

        String text = "    " + textRenderer.getText();
        if (problem.getLocation() != null) {
            text += "\n    (" + problem.getLocation().getSourceLocation() + ")";
        }
        return text;
    }

    protected void copyResources() {
        List<ResourceFile> applicationResourceFiles = gatherApplicationResourceFiles();
        inspectApplicationResourceFiles(applicationResourceFiles);

        List<ResourceFile> resourceFiles = new ArrayList<>();
        resourceFiles.addAll(gatherFrameworkResourceFiles());
        resourceFiles.addAll(applicationResourceFiles);
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

        rewriteHTML(textFiles, jsLibraries);
    }

    private List<String> copyJavaScriptLibraries() {
        return JS_LIST.readLines(Charsets.UTF_8).stream()
            .filter(line -> !line.isEmpty() && !line.startsWith("#"))
            .map(line -> copyJavaScriptLibrary(line).getName())
            .toList();
    }

    private File copyJavaScriptLibrary(String url) {
        File libDir = new File(outputDir, "libraries");
        libDir.mkdir();

        try {
            URLLoader request = URLLoader.get(url);
            URLResponse response = request.send();

            File outputFile = new File(libDir, url.substring(url.lastIndexOf("/") + 1));
            Files.write(outputFile.toPath(), response.getBody());
            return outputFile;
        } catch (IOException e) {
            throw new RuntimeException("Failed to download " + url, e);
        }
    }

    private boolean isFileType(ResourceFile needle, List<String> haystack) {
        return haystack.stream()
            .anyMatch(type -> needle.getName().toLowerCase().endsWith(type));
    }

    private void rewriteHTML(List<ResourceFile> textFiles, List<String> jsLibraries) {
        File outputFile = getOutputFile(INDEX_FILE);

        try (PrintWriter writer = new PrintWriter(outputFile, Charsets.UTF_8.displayName())) {
            for (String line : INDEX_FILE.readLines(Charsets.UTF_8)) {
                line = line.replace("{project}", projectName);
                line = line.replace("{js-libraries}", generateScriptTags(jsLibraries));
                line = line.replace("{teavm-js-file}", SCRIPT_FILE_NAME);
                line = line.replace("{timestamp}", generateTimestampTag());
                line = line.replace("{build-id}", buildId);
                line = line.replace("{meta}", generateMetaTags());
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

    private String generateMetaTags() {
        if (meta == null || meta.isEmpty()) {
            return "";
        }

        return Splitter.on(",").splitToStream(meta)
            .map(this::generateMetaTag)
            .collect(Collectors.joining("\n"));
    }

    private String generateMetaTag(String entry) {
        List<String> parts = Splitter.on(CharMatcher.anyOf("=:")).splitToList(entry);
        if (parts.size() != 2) {
            throw new CommandLineInterfaceException("Invalid meta tag: " + entry);
        }
        return String.format("<meta name=\"%s\" content=\"%s\" />", parts.get(0), parts.get(1));
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
            Files.write(outputFile.toPath(), contents);
        } catch (IOException e) {
            throw new RuntimeException("Cannot copy file: " + file, e);
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

    protected File getScriptFile() {
        return new File(outputDir, SCRIPT_FILE_NAME);
    }

    private String normalizeFileName(ResourceFile file) {
        return file.getName().replace("/", "_");
    }

    private void cleanOldScripts() throws IOException {
        for (File file : FileUtils.walkFiles(outputDir, f -> f.getName().startsWith("script-"))) {
            FileUtils.delete(file);
        }
    }

    private void inspectApplicationResourceFiles(List<ResourceFile> resourceFiles) {
        List<String> fileNames = resourceFiles.stream()
            .map(ResourceFile::getName)
            .toList();

        for (String expected : EXPECTED_RESOURCES) {
            if (!fileNames.contains(expected)) {
                LOGGER.warning("Missing resource file " + expected);
            }
        }
    }
}
