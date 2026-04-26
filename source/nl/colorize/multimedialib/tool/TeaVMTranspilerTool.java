//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.github.xpenatan.gdx.teavm.backends.shared.config.AssetFileHandle;
import com.github.xpenatan.gdx.teavm.backends.shared.config.compiler.TeaCompiler;
import com.github.xpenatan.gdx.teavm.backends.web.config.backend.WebBackend;
import com.google.common.base.CharMatcher;
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
import nl.colorize.util.swing.Utils2D;
import org.teavm.diagnostics.DefaultProblemTextConsumer;
import org.teavm.diagnostics.Problem;
import org.teavm.tooling.ConsoleTeaVMToolLog;
import org.teavm.tooling.TeaVMTargetType;
import org.teavm.tooling.TeaVMTool;
import org.teavm.tooling.TeaVMToolException;
import org.teavm.vm.TeaVMOptimizationLevel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Transpiles MultimediaLib applications to JavaScript using TeaVM. After transpilation
 * has been completed, the generated JavaScript code is combined with the application's
 * resource files and copied to the output directory.
 * <p>
 * Applications generated with this tool now follow the same directory structure as the
 * {@code gdx-teavm} library. However, this structure can still be used when using
 * other browser-based renderers.
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

    @Arg(usage = "Inserts <meta> tags into the HTML, passed as name=value.", required = false)
    protected String meta;

    @Arg(usage = "Primary application language, defaults to en-US.")
    protected String language = "en-US";

    @Arg(usage = "Overrides the application with the demo application, for testing purposes.")
    protected boolean demo;

    private static final List<String> FRAMEWORK_JS_FILES = List.of(
        "browser/multimedialib.js",
        "browser/browser-bridge.js",
        "browser/peerjs-bridge.js"
    );

    private static final List<String> FRAMEWORK_RESOURCE_FILES = List.of(
        "colorize-emblem-64.png",
        "colorize-emblem-180.png",
        "colorize-icon-256.png",
        "colorize-icon-32.png",
        "colorize-logo-180.png",
        "OpenSans-Regular.ttf",
        "browser/assets/favicon.png",
        "browser/assets/loading.gif",
        "browser/assets/multimedialib.css",
        "demo/crate.vox.mtl",
        "demo/crate.vox.obj",
        "demo/crate.vox.png",
        "demo/demo.png",
        "demo/demo-sound.ogg",
        "effects/particle-circle.png",
        "effects/particle-diamond.png"
    );

    private static final ResourceFile INDEX_FILE = new ResourceFile("browser/index.html");
    private static final ResourceFile FRAMEWORK_RESOURCES = new ResourceFile("browser/resources.txt");
    private static final ResourceFile JS_LIBS = new ResourceFile("browser/javascript-libraries.txt");
    private static final List<String> TEXT_EXTS = List.of(
        ".atlas", ".csv", ".fnt", ".glsl", ".json", ".md", ".properties", ".txt", ".yaml", ".yml");
    private static final Logger LOGGER = LogHelper.getLogger(TeaVMTranspilerTool.class);

    public static void main(String[] argv) {
        CommandLineArgumentParser argParser = new CommandLineArgumentParser("TeaVMTranspilerTool");
        TeaVMTranspilerTool transpiler = argParser.parse(argv, TeaVMTranspilerTool.class);
        transpiler.run();
    }

    protected void run() {
        Preconditions.checkArgument(resourceDir.exists(),
            "Resource directory not found: " + resourceDir.getAbsolutePath());

        Stopwatch timer = new Stopwatch();
        outputDir.mkdir();
        checkMainClass();

        try {
            cleanOutputDir();
            transpile();
            copyFrameworkFiles();
            copyResources();
            printSummary(timer);
        } catch (TeaVMToolException | IOException e) {
            LOGGER.log(Level.SEVERE, "Transpiling failed", e);
        }
    }

    @SuppressWarnings("ReturnValueIgnored")
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

    private void printSummary(Stopwatch timer) throws IOException {
        long htmlSize = new File(outputDir, "index.html").length();
        long jsSize = new File(outputDir, getScriptFileName()).length();

        LOGGER.info("HTML file size:                   " + FileUtils.formatFileSize(htmlSize));
        LOGGER.info("Transpiled JavaScript file size:  " + FileUtils.formatFileSize(jsSize));
        LOGGER.info("Time taken:                       " + (timer.tock() / 1000L) + "s");
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
        transpiler.setObfuscated(false);
        transpiler.setSourceMapsFileGenerated(true);
        transpiler.setTargetDirectory(outputDir);
        transpiler.setTargetType(TeaVMTargetType.JAVASCRIPT);
        transpiler.setTargetFileName(getScriptFileName());
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

    private void copyFrameworkFiles() throws IOException {
        for (String jsFilePath : FRAMEWORK_JS_FILES) {
            ResourceFile inputFile = new ResourceFile(jsFilePath);
            File outputFile = new File(outputDir, inputFile.getName());
            Files.writeString(outputFile.toPath(), inputFile.read(), UTF_8);
        }
    }

    private void copyResources() throws IOException {
        File tempDir = FileUtils.createTempDir();

        new TeaCompiler(new WebBackend())
            .addAssets(prepareAssetsDir())
            .setOptimizationLevel(TeaVMOptimizationLevel.SIMPLE)
            .setMainClass(mainClassName)
            .setObfuscated(false)
            .build(tempDir);

        File assetsDir = new File(outputDir, "assets");
        File scriptsDir = new File(outputDir, "scripts");

        FileUtils.copyDirectory(new File(tempDir, "webapp/assets"), assetsDir);
        FileUtils.copyDirectory(new File(tempDir, "webapp/scripts"), scriptsDir);

        replaceLoadingImage(assetsDir);

        List<File> textResourceFiles = FileUtils.walkFiles(assetsDir, this::isTextResourceFile);
        List<File> jsLibraries = copyJavaScriptLibraries(scriptsDir);

        rewriteHTML(textResourceFiles, jsLibraries);
    }

    /**
     * The {@code gdx-teavm} library only allows assets directories, meaning
     * it's not possible to load assets from the classpath. Therefore, we first
     * copy both the application resource files and the classpath resource
     * files to a temporary directory, which we then use as the asset
     * directory.
     */
    private AssetFileHandle prepareAssetsDir() throws IOException {
        File tempDir = FileUtils.createTempDir();
        File tempAssetsDir = new File(tempDir, "assets");

        FileUtils.copyDirectory(resourceDir, tempAssetsDir);

        for (String path : FRAMEWORK_RESOURCE_FILES) {
            ResourceFile frameworkResourceFile = new ResourceFile(path);
            try (InputStream stream = frameworkResourceFile.openStream()) {
                Path outputFile = tempAssetsDir.toPath().resolve(path);
                Files.createDirectories(outputFile.getParent());
                Files.copy(stream, outputFile);
            }
        }

        return new AssetFileHandle(tempAssetsDir.getAbsolutePath());
    }

    private List<File> copyJavaScriptLibraries(File scriptsDir) {
        return JS_LIBS.readLines(UTF_8).stream()
            .filter(line -> !line.isEmpty() && !line.startsWith("#"))
            .map(line -> copyJavaScriptLibrary(line, scriptsDir))
            .toList();
    }

    private File copyJavaScriptLibrary(String url, File scriptsDir) {
        try {
            LOGGER.info("Downloading " + url);
            HttpResponse<String> response = URLLoader.get(url);

            String fileName = Splitter.on("/").omitEmptyStrings().splitToList(url).getLast();
            File outputFile = new File(scriptsDir, fileName);
            Files.writeString(outputFile.toPath(), response.body(), UTF_8);
            return outputFile;
        } catch (IOException e) {
            throw new RuntimeException("Failed to download " + url, e);
        }
    }

    private boolean isTextResourceFile(File file) {
        return TEXT_EXTS.stream()
            .anyMatch(type -> file.getName().toLowerCase().endsWith(type));
    }

    private void replaceLoadingImage(File assetsDir) throws IOException {
        BufferedImage image = new BufferedImage(500, 83, BufferedImage.TYPE_INT_ARGB);
        File startupImageFile = new File(assetsDir, "startup-logo.png");
        Utils2D.savePNG(image, startupImageFile);
    }

    private void rewriteHTML(List<File> textFiles, List<File> jsLibraries) {
        File outputFile = new File(outputDir, "index.html");

        try (PrintWriter writer = new PrintWriter(outputFile, UTF_8)) {
            for (String line : INDEX_FILE.readLines(UTF_8)) {
                line = line.replace("{project}", projectName);
                line = line.replace("{js-libraries}", generateScriptTags(jsLibraries));
                line = line.replace("{teavm-js-file}", getScriptFileName());
                line = line.replace("{timestamp}", generateTimestampTag());
                line = line.replace("{build-id}", buildId);
                line = line.replace("{meta}", generateMetaTags());
                line = line.replace("{language}", language);
                if (line.trim().equals("{resources}")) {
                    line = generateTextResourceFilesHTML(textFiles);
                }
                writer.println(line);
            }
        } catch (IOException e) {
            throw new MediaException("Cannot write to file: " + outputFile.getAbsolutePath(), e);
        }
    }

    private String generateScriptTags(List<File> jsLibraries) {
        return jsLibraries.stream()
            .map(lib -> "<script src=\"scripts/" + lib.getName() + "\"></script>\n")
            .collect(Collectors.joining(""));
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

    private String generateTextResourceFilesHTML(List<File> files) throws IOException {
        StringBuilder buffer = new StringBuilder();

        for (File file : files) {
            String id = normalizeFileName(file).replace(".", "_");
            String contents = Files.readString(file.toPath(), UTF_8);

            buffer.append("<div id=\"" + id + "\">");
            buffer.append(XmlEscapers.xmlContentEscaper().escape(contents));
            buffer.append("</div>\n");
        }

        buffer.append("<div id=\"resource-file-manifest\">");
        buffer.append(XmlEscapers.xmlContentEscaper().escape(generateResourceFileManifest()));
        buffer.append("</div>\n");

        return buffer.toString();
    }

    private String generateResourceFileManifest() throws IOException {
        File assetsDir = new File(outputDir, "assets");

        return FileUtils.walkFiles(assetsDir, f -> true).stream()
            .filter(file -> !file.getName().endsWith(".js"))
            .filter(file -> !file.getName().endsWith(".DS_Store"))
            .map(file -> FileUtils.getRelativePath(file, assetsDir))
            .distinct()
            .sorted()
            .collect(Collectors.joining("\n"));
    }

    private String getScriptFileName() {
        return "script-" + buildId + ".js";
    }

    private String normalizeFileName(File file) {
        return file.getName().replace("/", "_");
    }

    private void cleanOutputDir() throws IOException {
        for (File file : FileUtils.walkFiles(outputDir, f -> f.getName().startsWith("script-"))) {
            FileUtils.delete(file);
        }

        for (String subDirName : List.of("assets", "scripts")) {
            File subDir = new File(outputDir, subDirName);
            if (subDir.exists()) {
                FileUtils.deleteDirectory(subDir);
            }
        }
    }
}
