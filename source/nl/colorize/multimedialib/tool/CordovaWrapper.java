//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import nl.colorize.util.LogHelper;
import nl.colorize.util.ResourceFile;
import org.kohsuke.args4j.Option;
import org.teavm.apachecommons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Embeds the browser version of the application, transpiled using TeaVM, into
 * native iOS and Android applications using Cordova.
 */
public class CordovaWrapper extends CommandLineTool {

    @Option(name = "-webapp", required = true, usage = "Directory containing the TeaVM version of the application")
    public File webAppDir;

    @Option(name = "-out", required = true, usage = "Output directory for the generated apps")
    public File outputDir;

    @Option(name = "-platforms", usage = "Comma-separated list of platforms, default is ios/android/osx")
    public String platforms = "ios,android,osx";
    
    @Option(name = "-appid", required = true, usage = "Application identifier, e.g. nl.colorize.test")
    public String appID;
    
    @Option(name = "-appname", required = true, usage = "Application display name")
    public String appName;
    
    @Option(name = "-version", required = true, usage = "Application version in the format x.y.z.")
    public String version;
    
    @Option(name = "-icon", required = true, usage = "Application icon, should be a 1024x1024 PNG image")
    public File iconFile;
    
    @Option(name = "-buildjson", required = true, usage = "Location of the Cordova build.json file")
    public File buildJSON;
    
    @Option(name = "-dist", usage = "Build distribution type, either 'release' (default) or 'debug'")
    public String dist = "release";

    private static final ResourceFile CONFIG_TEMPLATE = new ResourceFile("cordova/config.xml");
    private static final Splitter ARG_SPLITTER = Splitter.on(",").trimResults().omitEmptyStrings();
    private static final Joiner COMMAND_JOINER = Joiner.on(" ");
    private static final Logger LOGGER = LogHelper.getLogger(CordovaWrapper.class);

    public static void main(String[] args) {
        CordovaWrapper tool = new CordovaWrapper();
        tool.start(args);
    }

    @Override
    public void run() {
        try {
            if (!outputDir.exists()) {
                LOGGER.info("Creating Cordova app in " + outputDir.getAbsolutePath());
                createCordovaApp();
                generateIcon();
            }

            LOGGER.info("Building Cordova app");
            updateConfig();
            buildCordovaApp();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to build Cordova app", e);
        }
    }

    private void createCordovaApp() throws IOException {
        outputDir.mkdir();

        runCordova("cordova", "create", outputDir.getAbsolutePath(), appID, appName);

        Files.copy(iconFile, new File(outputDir, "icon.png"));
        Files.copy(buildJSON, new File(outputDir, "build.json"));

        List<String> platformList = ARG_SPLITTER.splitToList(platforms);
        runCordova(Iterables.concat(ImmutableList.of("cordova", "platform", "add"), platformList));
        
        runCordova("cordova", "plugin", "add", "cordova-plugin-wkwebview-engine");

        File android = new File(outputDir.getAbsolutePath() + "/platforms/android/app/build.gradle");
        Files.append("android.lintOptions.checkReleaseBuilds = false\n", android, Charsets.UTF_8);
    }

    private void generateIcon() {
        AppleIconTool iconTool = new AppleIconTool();
        iconTool.inputImageFile = iconFile;
        iconTool.location = getOutput("platforms/ios/" + appName + "/Images.xcassets/AppIcon.appiconset");
        iconTool.platform = "ios";
        iconTool.run();
    }

    private void updateConfig() throws IOException {
        String config = CONFIG_TEMPLATE.read(Charsets.UTF_8);
        config = config.replace("@@@ID", appID);
        config = config.replace("@@@NAME", appName);
        config = config.replace("@@@VERSION", version);

        File configFile = new File(outputDir, "config.xml");
        Files.write(config, configFile, Charsets.UTF_8);
    }

    private void buildCordovaApp() throws IOException {
        File appRoot = new File(outputDir, "www");
        FileUtils.deleteDirectory(appRoot);
        FileUtils.copyDirectory(webAppDir, appRoot);

        List<String> command = new ArrayList<>();
        command.add("cordova");
        command.add("build");
        command.addAll(ARG_SPLITTER.splitToList(platforms));
        if (dist.equals("release")) {
            command.add("--release");
            command.add("--device");
        }

        runCordova(command);
    }

    private void runCordova(String... command) {
        LOGGER.info(COMMAND_JOINER.join(command));

        ProcessBuilder processBuilder = new ProcessBuilder(command)
            .directory(outputDir)
            .inheritIO();

        String java8Home = System.getenv("JAVA8_HOME");

        Preconditions.checkState(java8Home != null,
            "Cordova requires environment variable JAVA8_HOME that refers to Java 8");

        Map<String, String> env = processBuilder.environment();
        env.put("JAVA_HOME", java8Home);

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IOException("Process failed with exit code " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Process failed");
        }
    }

    private void runCordova(Iterable<String> command) {
        runCordova(ImmutableList.copyOf(command).toArray(new String[0]));
    }

    private File getOutput(String path) {
        return new File(outputDir.getAbsolutePath() + "/" + path);
    }
}
