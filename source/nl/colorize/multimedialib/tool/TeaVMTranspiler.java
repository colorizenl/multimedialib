//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.util.FileUtils;
import nl.colorize.util.LoadUtils;
import nl.colorize.util.LogHelper;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.swing.Utils2D;
import nl.colorize.util.xml.XMLHelper;
import org.jdom2.Element;
import org.kohsuke.args4j.Option;
import org.teavm.diagnostics.Problem;
import org.teavm.tooling.ConsoleTeaVMToolLog;
import org.teavm.tooling.TeaVMTargetType;
import org.teavm.tooling.TeaVMTool;
import org.teavm.tooling.TeaVMToolException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Transpiles MultimediaLib applications to JavaScript using TeaVM. After transpilation
 * has been completed, the generated JavaScript code is combined with the application's
 * resource files and copied to the output directory.
 */
public class TeaVMTranspiler extends CommandLineTool {

    @Option(name = "-project", required = true, usage = "Project name for the application")
    public String projectName;

    @Option(name = "-renderer", required = true, usage = "One of 'canvas', 'webgl', 'three'")
    public String renderer;

    @Option(name = "-resources", required = true, usage = "Location of the application's resource files")
    public File resourceDir;

    @Option(name = "-out", required = true, usage = "Output directory for the generated filess")
    public File outputDir;

    @Option(name = "-main", required = true, usage = "Main class that acts as application entry point")
    public String mainClassName;

    private static final List<ResourceFile> WEB_RESOURCE_FILES = ImmutableList.of(
        new ResourceFile("browser/index.html"),
        new ResourceFile("browser/multimedialib.css"),
        new ResourceFile("browser/multimedialib.js"),
        new ResourceFile("browser/canvas-renderer.js"),
        new ResourceFile("browser/webgl2d-renderer.js"),
        new ResourceFile("browser/threejs-renderer.js"),
        new ResourceFile("browser/network.js"),
        new ResourceFile("browser/favicon.png"),
        new ResourceFile("browser/apple-icon.png"),
        new ResourceFile("browser/orientation-lock.png"),
        new ResourceFile("browser/loading.gif"),
        new ResourceFile("browser/OpenSans-Regular.ttf"),
        new ResourceFile("browser/lib/peerjs-1.2.0.min.js"),
        new ResourceFile("browser/lib/three.js"),
        new ResourceFile("browser/lib/FBXLoader.js"),
        new ResourceFile("browser/lib/inflate.min.js"),
        new ResourceFile("colorize-logo.png"),
        new ResourceFile("orientation-lock.png"),
        new ResourceFile("ui-widget-background.png"),
        new ResourceFile("transition-effect.png")
    );
    
    private static final List<String> TEXT_FILE_TYPES = ImmutableList.of(
        ".txt", ".md", ".json", ".yml", ".yaml", ".properties", ".fnt", ".csv", "-manifest");

    private static final List<String> IMAGE_FILES_TYPES = ImmutableList.of(".png", ".jpg");
    private static final List<String> SUPPORTED_RENDERS = ImmutableList.of("canvas", "webgl", "three");
    private static final List<String> KNOWN_MISSING_CLASSES = Collections.emptyList();
    private static final Logger LOGGER = LogHelper.getLogger(TeaVMTranspiler.class);

    public static void main(String[] args) {
        TeaVMTranspiler tool = new TeaVMTranspiler();
        tool.start(args);
    }

    @Override
    public void run() {
        Preconditions.checkArgument(resourceDir.exists(),
            "Resource directory not found: " + resourceDir.getAbsolutePath());
        Preconditions.checkArgument(SUPPORTED_RENDERS.contains(renderer),
            "Invalid renderer: " + renderer);

        outputDir.mkdir();

        try {
            copyResources();
            transpile();
            LOGGER.info("Results saved to " + outputDir.getAbsolutePath());
        } catch (TeaVMToolException e) {
            LOGGER.log(Level.SEVERE, "Transpiling failed", e);
        }
    }

    private void transpile() throws TeaVMToolException {
        LOGGER.info("Transpiling " + projectName + " to JavaScript");

        TeaVMTool transpiler = new TeaVMTool();
        transpiler.setClassLoader(getClass().getClassLoader());
        transpiler.setDebugInformationGenerated(true);
        transpiler.setIncremental(false);
        transpiler.setLog(new ConsoleTeaVMToolLog(true));
        transpiler.setMainClass(mainClassName);
        transpiler.setMinifying(false);
        transpiler.setSourceMapsFileGenerated(true);
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

    private void copyResources() {
        List<ResourceFile> resourceFiles = gatherResourceFiles();
        resourceFiles.add(generateManifest(resourceFiles));
        
        List<ResourceFile> textFiles = new ArrayList<>();
        List<ResourceFile> imageFiles = new ArrayList<>();

        for (ResourceFile file : resourceFiles) {
            if (isFileType(file, TEXT_FILE_TYPES)) {
                LOGGER.info("Using text file " + file);
                textFiles.add(file);
            } else if (isFileType(file, IMAGE_FILES_TYPES)) {
                LOGGER.info("Using image file " + file);
                imageFiles.add(file);                
            } else {
                LOGGER.info("Copying resource file " + file);
                copyBinaryResourceFile(file);
            }
        }

        List<File> imageDataFiles = imageFiles.stream()
            .peek(imageFile -> LOGGER.info("Generating image data file for " + imageFile))
            .map(imageFile -> generateImageDataFile(imageFile))
            .collect(Collectors.toList());

        for (ResourceFile file : WEB_RESOURCE_FILES) {
            if (file.getName().endsWith(".html")) {
                LOGGER.info("Generating HTML file " + file);
                rewriteHTML(file, textFiles, imageDataFiles);
            } else {
                LOGGER.info("Generating file " + file);
                copyBinaryResourceFile(file);
            }
        }
    }

    private boolean isFileType(ResourceFile needle, List<String> haystack) {
        return haystack.stream()
            .anyMatch(type -> needle.getName().toLowerCase().endsWith(type));
    }

    private void rewriteHTML(ResourceFile file, List<ResourceFile> textResources, List<File> imageData) {
        File outputFile = getOutputFile(file);

        try (PrintWriter writer = new PrintWriter(outputFile, Charsets.UTF_8.displayName())) {
            for (String line : file.readLines(Charsets.UTF_8)) {
                line = line.replace("@@@PROJECT", projectName);
                line = line.replace("@@@RENDERER", renderer);
                if (line.trim().equals("@@@RESOURCES")) {
                    line = generateTextResourceFilesHTML(textResources);
                } else if (line.trim().equals("@@@IMAGE_DATA")) {
                    line = generateImageDataList(imageData) + "\n";
                }
                writer.println(line);
            }
        } catch (IOException e) {
            throw new MediaException("Cannot write to file: " + outputFile.getAbsolutePath(), e);
        }
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

    private String generateImageDataList(List<File> imageDataFiles) {
        return imageDataFiles.stream()
            .map(file -> "        <script src=\"images/" + file.getName() + "\" async></script>")
            .collect(Collectors.joining("\n"));
    }

    private void copyBinaryResourceFile(ResourceFile file) {
        try (InputStream stream = file.openStream()) {
            byte[] contents = LoadUtils.readToByteArray(stream);
            FileUtils.write(contents, getOutputFile(file));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot copy file " + file, e);
        }
    }

    private List<ResourceFile> gatherResourceFiles() {
        try {
            return Files.walk(resourceDir.toPath())
                .map(path -> path.toFile())
                .filter(file -> !file.isDirectory() && !file.getName().startsWith("."))
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
    
    private File generateImageDataFile(ResourceFile imageFile) {
        try {
            File imagesDir = new File(outputDir, "images");
            FileUtils.mkdir(imagesDir);

            File imageDataFile = new File(imagesDir, "image-" + normalizeFileName(imageFile) + ".js");
            BufferedImage image = Utils2D.loadImage(imageFile);
            String contents = "imageDataURLs[\"" + normalizeFileName(imageFile) + "\"] = \"" +
                Utils2D.toDataURL(image) + "\";";

            Files.write(imageDataFile.toPath(), contents.getBytes(Charsets.UTF_8));
            return imageDataFile;
        } catch (IOException e) {
            throw new MediaException("Unable to generate image data file", e);
        }
    }

    private File getOutputFile(ResourceFile file) {
        return new File(outputDir, normalizeFileName(file));
    }

    private String normalizeFileName(ResourceFile file) {
        return file.getName().replace("/", "_");
    }
}
