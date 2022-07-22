//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.util.FileUtils;
import nl.colorize.util.Formatting;
import nl.colorize.util.LoadUtils;
import nl.colorize.util.LogHelper;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.cli.CommandLineArgumentParser;
import nl.colorize.util.http.URLLoader;
import nl.colorize.util.http.URLResponse;
import nl.colorize.util.xml.XMLHelper;
import org.jdom2.Element;
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
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Transpiles MultimediaLib applications to JavaScript using TeaVM. After transpilation
 * has been completed, the generated JavaScript code is combined with the application's
 * resource files and copied to the output directory.
 */
public class TeaVMTranspilerTool {

    protected String projectName;
    protected File resourceDir;
    protected String mainClassName;
    protected String renderer;
    protected File outputDir;
    protected boolean minify;
    protected File manifestFile;

    private static final List<String> FRAMEWORK_RESOURCE_FILE_PATHS = ImmutableList.of(
        "browser/index.html",
        "browser/multimedialib.js",
        "browser/pixi-renderer.js",
        "browser/service-worker.js",
        "browser/assets/multimedialib.css",
        "browser/assets/favicon.png",
        "browser/assets/apple-icon.png",
        "browser/assets/orientation-lock.png",
        "browser/assets/loading.gif",
        "browser/assets/OpenSans-Regular.ttf",
        "colorize-logo.png",
        "orientation-lock.png",
        "ui-widget-background.png",
        "transition-effect.png"
    );

    private static final Map<String, String> NPM_VERSIONS = Maps.fromProperties(
        LoadUtils.loadProperties(new ResourceFile("npm-versions.properties"), Charsets.UTF_8));

    private static final List<String> JAVASCRIPT_URLS = ImmutableList.of(
        "https://pixijs.download/v" + NPM_VERSIONS.get("pixi.js") + "/pixi.min.js",
        "https://pixijs.download/v" + NPM_VERSIONS.get("pixi.js") + "/pixi.min.js.map",
        "https://cdn.jsdelivr.net/npm/pixi-heaven@" + NPM_VERSIONS.get("pixi-heaven") + "/dist/pixi-heaven.umd.min.js",
        "https://cdn.jsdelivr.net/npm/pixi-heaven@" + NPM_VERSIONS.get("pixi-heaven") + "/dist/pixi-heaven.umd.min.js.map",
        "https://unpkg.com/three@" + NPM_VERSIONS.get("three") + "/build/three.min.js",
        "https://unpkg.com/three@" + NPM_VERSIONS.get("three") + "/examples/js/loaders/GLTFLoader.js",
        "https://unpkg.com/peerjs@" + NPM_VERSIONS.get("peerjs") + "/dist/peerjs.min.js",
        "https://unpkg.com/peerjs@" + NPM_VERSIONS.get("peerjs") + "/dist/peerjs.min.cjs.map"
    );

    private static final List<String> TEXT_FILE_TYPES = ImmutableList.of(
        ".txt", ".md", ".json", ".yml", ".yaml", ".properties", ".fnt", ".csv", "-manifest");

    private static final List<String> RESOURCE_FILE_TYPES = ImmutableList.of(
        ".css", ".png", ".jpg", ".svg", ".gif", ".ttf", ".wav", ".mp3", ".ogg", ".gltf", ".fbx", ".g3db");
        
    private static final List<String> KNOWN_MISSING_CLASSES = ImmutableList.of(
        "[java.lang.System.exit(I)V]",
        "[java.lang.reflect.TypeVariable]",
        "[java.lang.Class.getGenericSuperclass()Ljava/lang/reflect/Type;]"
    );

    private static final List<String> SUPPORTED_RENDERERS = ImmutableList.of("canvas", "pixi", "three");
    private static final Logger LOGGER = LogHelper.getLogger(TeaVMTranspilerTool.class);

    public static void main(String[] args) {
        CommandLineArgumentParser argParser = new CommandLineArgumentParser("TeaVMTranspiler")
            .add("-project", "Project name for the application")
            .add("-main", "Main class that acts as application entry point")
            .add("-resources", "Location of the application's resource files")
            .add("-renderer", "One of 'canvas', 'pixi', 'three'")
            .add("-out", "Output directory for the generated files")
            .addOptional("-manifest", null, "PWA manifest.json file location")
            .addFlag("-minify", "Minifies the generated JavaScript, off by default");

        argParser.parseArgs(args);

        TeaVMTranspilerTool tool = new TeaVMTranspilerTool();
        tool.projectName = argParser.get("project");
        tool.mainClassName = argParser.get("main");
        tool.resourceDir = argParser.getDir("resources");
        tool.manifestFile = argParser.getFile("manifest");
        tool.renderer = argParser.get("renderer");
        tool.outputDir = argParser.getFile("out");
        tool.minify = argParser.getBool("minify");
        tool.run();
    }

    protected void run() {
        Preconditions.checkArgument(resourceDir.exists(),
            "Resource directory not found: " + resourceDir.getAbsolutePath());
        Preconditions.checkArgument(SUPPORTED_RENDERERS.contains(renderer),
            "Invalid renderer: " + renderer);

        outputDir.mkdir();
        checkMainClass();

        try {
            copyResources();
            JAVASCRIPT_URLS.forEach(this::downloadJavaScriptLibrary);
            transpile();
            if (manifestFile != null) {
                copyManifest();
                generateServiceWorker();
            }
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
        long libSize = Arrays.stream(new File(outputDir, "libraries").listFiles())
            .mapToLong(File::length)
            .sum();

        LOGGER.info("HTML file size:                     " + Formatting.memoryFormat(htmlSize, 1));
        LOGGER.info("Transpiled JavaScript file size:    " + Formatting.memoryFormat(jsSize, 1));
        LOGGER.info("JavaScript library file size:       " + Formatting.memoryFormat(libSize, 1));
        LOGGER.info("Results saved to " + outputDir.getAbsolutePath());
    }

    private void transpile() throws TeaVMToolException {
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
        List<ResourceFile> resourceFiles = gatherResourceFiles();
        resourceFiles.add(generateManifest(resourceFiles));

        LOGGER.info("Copying " + resourceFiles.size() + " resource files");
        
        List<ResourceFile> textFiles = new ArrayList<>();

        for (ResourceFile file : resourceFiles) {
            if (isFileType(file, TEXT_FILE_TYPES)) {
                textFiles.add(file);
            } else {
                copyBinaryResourceFile(file);
            }
        }

        LOGGER.info("Generating " + FRAMEWORK_RESOURCE_FILE_PATHS.size() + " files");

        for (String path : FRAMEWORK_RESOURCE_FILE_PATHS) {
            if (path.endsWith(".html")) {
                rewriteHTML(new ResourceFile(path), textFiles);
            } else {
                copyBinaryResourceFile(new ResourceFile(path));
            }
        }
    }

    private boolean isFileType(ResourceFile needle, List<String> haystack) {
        return haystack.stream()
            .anyMatch(type -> needle.getName().toLowerCase().endsWith(type));
    }

    private void rewriteHTML(ResourceFile file, List<ResourceFile> textResources) {
        File outputFile = getOutputFile(file);

        try (PrintWriter writer = new PrintWriter(outputFile, Charsets.UTF_8.displayName())) {
            for (String line : file.readLines(Charsets.UTF_8)) {
                line = line.replace("{project}", projectName);
                line = line.replace("{renderer}", renderer);
                line = line.replace("{manifest}", generateManifestHTML());
                if (line.trim().equals("{resources}")) {
                    line = generateTextResourceFilesHTML(textResources);
                }
                writer.println(line);
            }
        } catch (IOException e) {
            throw new MediaException("Cannot write to file: " + outputFile.getAbsolutePath(), e);
        }
    }

    private String generateManifestHTML() {
        if (manifestFile == null) {
            return "";
        }
        return "<link rel=\"manifest\" href=\"manifest.json\" />";
    }

    private String generateTextResourceFilesHTML(List<ResourceFile> files) {
        StringBuilder buffer = new StringBuilder();

        for (ResourceFile file : files) {
            Element element = new Element("div");
            element.setAttribute("id", normalizeFileName(file).replace(".", "_"));
            element.setText(file.read(Charsets.UTF_8));

            String xml = XMLHelper.toString(element);
            buffer.append(xml);
            buffer.append("\n");
        }

        return buffer.toString();
    }

    private void copyBinaryResourceFile(ResourceFile file) {
        File outputFile = getOutputFile(file);

        try (InputStream stream = file.openStream()) {
            byte[] contents = LoadUtils.readToByteArray(stream);
            FileUtils.write(contents, outputFile);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot copy file " + file, e);
        }
    }

    private void downloadJavaScriptLibrary(String url) {
        File librariesDir = new File(outputDir, "libraries");
        librariesDir.mkdir();

        String outputFileName = url.substring(url.lastIndexOf("/") + 1);
        File outputFile = new File(librariesDir, outputFileName);

        if (!outputFile.exists()) {
            LOGGER.info("Downloading " + url);
            try {
                URLResponse response = URLLoader.get(url).sendRequest();
                FileUtils.write(response.getBody(), Charsets.UTF_8, outputFile);
            } catch (IOException e) {
                throw new RuntimeException("Cannot download " + url, e);
            }
        }
    }

    private List<ResourceFile> gatherResourceFiles() {
        try {
            return Files.walk(resourceDir.toPath())
                .map(path -> path.toFile())
                .filter(file -> !file.isDirectory() && !file.getName().startsWith("."))
                .filter(file -> !file.getAbsolutePath().contains("/lib/"))
                .map(file -> new ResourceFile(file))
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());

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

    private void copyManifest() {
        try {
            File outputFile = new File(outputDir, "manifest.json");
            Files.copy(manifestFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new MediaException("Cannot copy manifest", e);
        }
    }

    /**
     * Updates the {@code service-worker.js} template for the application. This
     * will recreate the cache every time, since it is assumed that a new build
     * also means the previous cache should be invalidated.
     */
    private void generateServiceWorker() {
        try {
            File outputFile = new File(outputDir, "service-worker.js");
            String template = Files.readString(outputFile.toPath(), Charsets.UTF_8);

            String resourceFilePaths = gatherServiceWorkerFilePaths()
                .map(path -> "    \"" + path + "\",")
                .collect(Collectors.joining("\n"));

            String generated = template
                .replace("{cachename}", projectName + "-" + System.currentTimeMillis())
                .replace("{resourcefiles}", resourceFilePaths);

            Files.write(outputFile.toPath(), generated.getBytes(Charsets.UTF_8));
        } catch (IOException e) {
            throw new MediaException("Cannot create service worker", e);
        }
    }

    private Stream<String> gatherServiceWorkerFilePaths() throws IOException {
        return Files.walk(outputDir.toPath())
            .map(path -> outputDir.toPath().relativize(path))
            .sorted()
            .map(path -> "/" + path);
    }
}
