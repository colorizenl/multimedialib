//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.base.Preconditions;
import nl.colorize.util.FileUtils;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;
import nl.colorize.util.cli.Arg;
import nl.colorize.util.cli.CommandLineArgumentParser;
import nl.colorize.util.swing.Utils2D;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates a Mac {@code .icns} icon file from a PNG image. The icon will
 * include multiple variants for different sizes.
 */
public class MacIconTool {

    @Arg(name = "input", usage = "PNG file that acts as input for the icon")
    protected File inputFile;

    @Arg(name = "output", usage = "Directory where the ICNS file will be generated")
    protected File outputDir;

    private static final List<IconVariant> MAC_ICON_VARIANTS = List.of(
        new IconVariant("icon_16x16.png", 16),
        new IconVariant("icon_16x16@2x.png", 32),
        new IconVariant("icon_32x32.png", 32),
        new IconVariant("icon_32x32@2x.png", 64),
        new IconVariant("icon_128x128.png", 128),
        new IconVariant("icon_128x128@2x.png", 256),
        new IconVariant("icon_256x256.png", 256),
        new IconVariant("icon_256x256@2x.png", 512),
        new IconVariant("icon_512x512.png", 512),
        new IconVariant("icon_512x512@2x.png", 1024)
    );

    private static final Logger LOGGER = LogHelper.getLogger(MacIconTool.class);

    public static void main(String[] argv) {
        if (Platform.isMac()) {
            CommandLineArgumentParser argParser = new CommandLineArgumentParser("MacIconTool");
            MacIconTool tool = argParser.parse(argv, MacIconTool.class);
            tool.run();
        } else {
            LOGGER.severe("This tool can only be used on Mac");
            System.exit(1);
        }
    }

    protected void run() {
        try {
            BufferedImage original = Utils2D.loadImage(inputFile);
            generateIconSet(original);
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Failed to generate ICNS icon", e);
        }
    }

    private void generateIconSet(BufferedImage original) throws IOException, InterruptedException {
        File iconSet = new File(outputDir, "icon.iconset");
        FileUtils.mkdir(iconSet);

        for (IconVariant variant : MAC_ICON_VARIANTS) {
            BufferedImage scaled = Utils2D.scaleImage(original, variant.size, variant.size, false);
            Utils2D.savePNG(scaled, new File(iconSet, variant.name));
        }

        int exitCode = new ProcessBuilder("iconutil", "-c", "icns", "icon.iconset")
            .directory(outputDir)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
            .waitFor();

        Preconditions.checkState(exitCode == 0, "iconutil failed with exit code " + exitCode);
    }

    /**
     * Represents one of the icon variants. Apple uses a slightly obscure
     * notation, where 32x32 and 16x16@2 both indicate an icon that is 32x32
     * pixels in size.
     */
    private record IconVariant(String name, int size) {
    }
}
