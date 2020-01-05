//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import nl.colorize.util.CommandRunner;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;
import nl.colorize.util.swing.Utils2D;
import org.kohsuke.args4j.Argument;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppleIconTool extends CommandLineTool {

    @Argument(index = 0, metaVar = "image", required = true)
    public File inputImageFile;

    @Argument(index = 1, metaVar = "outputFile", required = true)
    public File outputFile;

    private static final List<Integer> SIZE_VARIANTS = ImmutableList.of(16, 32, 128, 256, 512);
    private static final Logger LOGGER = LogHelper.getLogger(AppleIconTool.class);

    public static void main(String[] args) {
        AppleIconTool tool = new AppleIconTool();
        tool.start(args);
    }

    @Override
    public void run() {
        Preconditions.checkArgument(outputFile.getName().endsWith(".icns"),
            "Output file must be an ICNS icon");

        try {
            BufferedImage sourceImage = loadImage(inputImageFile);
            Map<String, BufferedImage> iconSet = createIconSet(sourceImage);
            File iconSetDir = getIconSetDir();
            saveIconSet(iconSet, iconSetDir);

            if (Platform.isMac()) {
                convertIconSetToICNS(iconSetDir, outputFile);
                LOGGER.info("Done, wrote ICNS icon to " + outputFile.getAbsolutePath());
            } else {
                LOGGER.warning("Creating ICNS icons is only possible on Mac OS");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create ICNS icon", e);
        }
    }

    private File getIconSetDir() throws IOException {
        File iconset = new File(outputFile.getParentFile(), "icon.iconset");
        if (iconset.exists()) {
            throw new IOException("Icon set already exists: " + iconset.getAbsolutePath());
        }
        iconset.mkdir();
        iconset.deleteOnExit();
        return iconset;
    }

    private BufferedImage loadImage(File sourceImageFile) {
        try {
            BufferedImage image = Utils2D.loadImage(sourceImageFile);
            if (image.getWidth() != image.getHeight()) {
                throw new RuntimeException("Image must be square to be used as icon");
            }
            return image;
        } catch (IOException e) {
            throw new RuntimeException("Cannot load image: " + sourceImageFile.getAbsolutePath());
        }
    }

    private Map<String, BufferedImage> createIconSet(BufferedImage sourceImage) {
        Map<String, BufferedImage> iconSet = new LinkedHashMap<String, BufferedImage>();
        for (int variant : SIZE_VARIANTS) {
            iconSet.put("icon_" + variant + "x" + variant + ".png",
                scaleIconImage(sourceImage, variant));
            iconSet.put("icon_" + variant + "x" + variant + "@2x.png",
                scaleIconImage(sourceImage, 2 * variant));
        }
        return iconSet;
    }

    private BufferedImage scaleIconImage(BufferedImage sourceImage, int size) {
        if (sourceImage.getWidth() == size && sourceImage.getHeight() == size) {
            return sourceImage;
        } else {
            return Utils2D.scaleImage(sourceImage, size, size, true);
        }
    }

    private void saveIconSet(Map<String, BufferedImage> iconSet, File outputDir) throws IOException {
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        for (Map.Entry<String, BufferedImage> entry : iconSet.entrySet()) {
            File imageFile = new File(outputDir, entry.getKey());
            LOGGER.info("Creating icon " + imageFile.getAbsolutePath());
            Utils2D.savePNG(entry.getValue(), imageFile);
        }
    }

    private void convertIconSetToICNS(File iconSetDir, File icnsFile) throws IOException {
        CommandRunner commandRunner = new CommandRunner("iconutil", "-c", "icns",
            iconSetDir.getAbsolutePath(), "-o", icnsFile.getAbsolutePath());
        commandRunner.setShellMode(true);
        commandRunner.setLoggingEnabled(true);
        try {
            commandRunner.execute();
        } catch (TimeoutException e) {
            throw new IOException("iconutil timeout");
        }
    }
}
