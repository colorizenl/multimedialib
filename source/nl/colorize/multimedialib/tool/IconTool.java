//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.base.Preconditions;
import nl.colorize.util.FileUtils;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;
import nl.colorize.util.cli.Arg;
import nl.colorize.util.cli.CommandLineArgumentParser;
import nl.colorize.util.cli.CommandLineInterfaceException;
import nl.colorize.util.swing.Utils2D;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

/**
 * Generates application icons for various platforms, based on a PNG image.
 * The following platforms are supported:
 *
 * <pre>
 *   | Platform          | File format | Icon size(s)                    |
 *   |-------------------|-------------|---------------------------------|
 *   | Mac               | .icns       | 1024, 512, 256, 128, 64, 32, 16 |
 *   | Windows           | .png        | 48                              |
 *   | iOS               | .png        | 1024, 180, 167, 152, 120        |
 *   | Browser favicon   | .png        | 32                              |
 *   | Apple favicon     | .png        | 180                             |
 *   | PWA               | .png        | 512, 192                        |
 * </pre>
 */
public class IconTool {

    @Arg(name = "input", usage = "PNG file that acts as input for the icon")
    protected File inputFile;

    @Arg(name = "output", usage = "Directory where the ICNS file will be generated")
    protected File outputDir;

    @Arg(name = "mask", usage = "Adds a rounded rectangle image mask for supported platforms")
    protected boolean mask;

    private static final List<IconVariant> MAC_ICONS = List.of(
        new IconVariant("icon_16x16.png", 16, true),
        new IconVariant("icon_16x16@2x.png", 32, true),
        new IconVariant("icon_32x32.png", 32, true),
        new IconVariant("icon_32x32@2x.png", 64, true),
        new IconVariant("icon_128x128.png", 128, true),
        new IconVariant("icon_128x128@2x.png", 256, true),
        new IconVariant("icon_256x256.png", 256, true),
        new IconVariant("icon_256x256@2x.png", 512, true),
        new IconVariant("icon_512x512.png", 512, true),
        new IconVariant("icon_512x512@2x.png", 1024, true)
    );

    private static final List<IconVariant> IOS_ICONS = List.of(
        new IconVariant("icon-120.png", 120, false),
        new IconVariant("icon-152.png", 152, false),
        new IconVariant("icon-167.png", 167, false),
        new IconVariant("icon-180.png", 180, false),
        new IconVariant("icon-1024.png", 1024, false)
    );

    private static final List<IconVariant> PWA_ICONS = List.of(
        new IconVariant("icon-192.png", 192, true),
        new IconVariant("icon-512.png", 512, true)
    );

    private static final IconVariant WINDOWS_ICON = new IconVariant("icon-48.png", 48, true);
    private static final IconVariant FAVICON = new IconVariant("favicon.png", 32, false);
    private static final IconVariant APPLE_FAVICON = new IconVariant("apple-favicon.png", 180, false);

    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 80);
    private static final int SHADOW_OFFSET = 1;
    private static final int SHADOW_BLUR = 4;
    private static final Logger LOGGER = LogHelper.getLogger(IconTool.class);

    public static void main(String[] argv) {
        CommandLineArgumentParser argParser = new CommandLineArgumentParser("MacIconTool");
        IconTool tool = argParser.parse(argv, IconTool.class);
        tool.run();
    }

    protected void run() {
        try {
            BufferedImage original = Utils2D.loadImage(inputFile);

            if (original.getWidth() != original.getHeight()) {
                throw new CommandLineInterfaceException("Application icon must be square");
            }

            FileUtils.mkdir(outputDir);
            generateIcons(original);
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Failed to generate application icon", e);
        }
    }

    private void generateIcons(BufferedImage original) throws IOException, InterruptedException {
        generateMacIconSet(original);
        generateAppIconSet(original);
        for (IconVariant pwaIcon : PWA_ICONS) {
            generateIcon(original, pwaIcon);
        }
        generateIcon(original, WINDOWS_ICON);
        generateIcon(original, FAVICON);
        generateIcon(original, APPLE_FAVICON);
    }

    private void generateMacIconSet(BufferedImage original) throws IOException, InterruptedException {
        File iconSet = new File(outputDir, "icon.iconset");
        FileUtils.mkdir(iconSet);

        for (IconVariant variant : MAC_ICONS) {
            BufferedImage image = generateIconVariant(original, variant);
            Utils2D.savePNG(image, new File(iconSet, variant.name));
        }

        if (!Platform.isMac()) {
            LOGGER.warning("ICNS files can only be generated on Mac");
            return;
        }

        int exitCode = new ProcessBuilder("iconutil", "-c", "icns", "icon.iconset")
            .directory(outputDir)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
            .waitFor();

        Preconditions.checkState(exitCode == 0, "iconutil failed with exit code " + exitCode);
    }

    private void generateAppIconSet(BufferedImage original) throws IOException {
        File iconSet = new File(outputDir, "AppIcon.appiconset");
        FileUtils.mkdir(iconSet);

        for (IconVariant variant : IOS_ICONS) {
            BufferedImage image = generateIconVariant(original, variant);
            Utils2D.savePNG(image, new File(iconSet, variant.name));
        }
    }

    private BufferedImage generateIconVariant(BufferedImage original, IconVariant variant) {
        if (mask && variant.maskable) {
            return generateMaskIcon(original, variant);
        } else {
            return generateRegularIcon(original, variant);
        }
    }

    private BufferedImage generateRegularIcon(BufferedImage original, IconVariant variant) {
        return Utils2D.scaleImage(original, variant.size, variant.size, true);
    }

    private BufferedImage generateMaskIcon(BufferedImage original, IconVariant variant) {
        float factor = variant.size / 512f;
        int inset = Math.round(50 * factor);
        int size = Math.round(412 * factor);
        int radius = Math.round(128 * factor);

        BufferedImage image = new BufferedImage(variant.size, variant.size, TYPE_INT_ARGB);
        Graphics2D g2 = Utils2D.createGraphics(image, true, true);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(inset, inset, size, size, radius, radius);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 1f));
        g2.drawImage(Utils2D.scaleImage(original, size, size, true), inset, inset, null);
        g2.dispose();

        return Utils2D.applyDropShadow(image, SHADOW_COLOR, SHADOW_OFFSET, SHADOW_BLUR);
    }

    private void generateIcon(BufferedImage original, IconVariant variant) throws IOException {
        BufferedImage image = generateIconVariant(original, variant);
        Utils2D.savePNG(image, new File(outputDir, variant.name));
    }

    /**
     * Describes an icon for platforms that require application icons to
     * support multiple variants. Apple platforms use a slightly obscure
     * notation, where {@code 32x32} and {@code 16x16@2} both indicate an
     * icon that is 32x32 pixels in size.
     */
    private record IconVariant(String name, int size, boolean maskable) {
    }
}
