//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
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
import nl.colorize.util.xml.XMLHelper;
import org.jdom2.Element;
import org.kohsuke.args4j.Option;
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
public class TeaVMTranspiler extends CommandLineTool {

    @Option(name = "-project", required = true, usage = "Project name for the application")
    public String projectName;

    @Option(name = "-resources", required = true, usage = "Location of the application's resource files")
    public File resourceDir;

    @Option(name = "-out", required = true, usage = "Output directory for the generated filess")
    public File outputDir;

    @Option(name = "-main", required = true, usage = "Main class that acts as application entry point")
    public String mainClassName;

    @Option(name = "-incremental", usage = "Enable to preserveexisting resource files and not overwrite them")
    public boolean incremental = false;

    private static final List<ResourceFile> WEB_RESOURCE_FILES = ImmutableList.of(
        new ResourceFile("web/index.html"),
        new ResourceFile("web/main.js"),
        new ResourceFile("web/favicon.png"),
        new ResourceFile("web/loading.gif"),
        new ResourceFile("web/OpenSans-Regular.ttf")
    );

    private static final List<String> TEXT_RESOURCE_FILE_TYPES = ImmutableList.of(
        ".txt", ".md", ".json", ".yml", ".yaml", ".properties", "-manifest");

    private static final Logger LOGGER = LogHelper.getLogger(TeaVMTranspiler.class);

    public static void main(String[] args) {
        TeaVMTranspiler tool = new TeaVMTranspiler();
        tool.start(args);
    }

    @Override
    public void run() {
        Preconditions.checkArgument(resourceDir.exists(), "Resource directory not found");

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
            LOGGER.log(Level.WARNING, "Error while transpiling: " + format(problem));
        }
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

        List<ResourceFile> textResourceFiles = new ArrayList<>();

        for (ResourceFile file : resourceFiles) {
            if (isTextResourceFile(file)) {
                textResourceFiles.add(file);
            } else if (shouldWrite(file)) {
                LOGGER.info("Copying resource file " + file.getPath());
                copyBinaryResourceFile(file);
            }
        }

        for (ResourceFile file : WEB_RESOURCE_FILES) {
            if (file.getName().endsWith(".html")) {
                LOGGER.info("Generating HTML file " + file.getPath());
                rewriteHTML(file, textResourceFiles);
            } else if (shouldWrite(file)) {
                LOGGER.info("Generating file " + file.getPath());
                copyBinaryResourceFile(file);
            }
        }
    }

    private boolean isTextResourceFile(ResourceFile file) {
        return TEXT_RESOURCE_FILE_TYPES.stream()
            .anyMatch(type -> file.getName().toLowerCase().endsWith(type));
    }

    private boolean shouldWrite(ResourceFile file) {
        File outputFile = getOutputFile(file);
        return !incremental || !outputFile.exists();
    }

    private void rewriteHTML(ResourceFile file, List<ResourceFile> textResourceFiles) {
        File outputFile = getOutputFile(file);

        try (PrintWriter writer = new PrintWriter(outputFile, Charsets.UTF_8.displayName())) {
            for (String line : file.readLines(Charsets.UTF_8)) {
                line = line.replace("@@@PROJECT", projectName);
                if (line.trim().equals("@@@RESOURCES")) {
                    line = generateTextResourceFilesHTML(textResourceFiles);
                }
                writer.println(line);
            }
        } catch (IOException e) {
            throw new MediaException("Cannot write to file: " + outputFile.getAbsolutePath(), e);
        }
    }

    private String generateTextResourceFilesHTML(List<ResourceFile> files) throws IOException {
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
                .collect(Collectors.toList());

            Files.write(manifestFile.toPath(), entries, Charsets.UTF_8);

            return new ResourceFile(manifestFile);
        } catch (IOException e) {
            throw new MediaException("Cannot generate resource file manifest", e);
        }
    }

    private File getOutputFile(ResourceFile file) {
        return new File(outputDir, normalizeFileName(file));
    }

    private String normalizeFileName(ResourceFile file) {
        return file.getName().replace("/", "_");
    }
}
