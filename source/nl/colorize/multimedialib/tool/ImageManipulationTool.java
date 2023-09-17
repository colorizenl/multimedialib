//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import nl.colorize.util.FileUtils;
import nl.colorize.util.LogHelper;
import nl.colorize.util.cli.Arg;
import nl.colorize.util.cli.CommandLineArgumentParser;
import nl.colorize.util.swing.Utils2D;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

/**
 * Command line tool for various forms of image manipulation that are not
 * available at runtime due to lack of platform support. This tool "bakes"
 * the effects into a new image, which can then be loaded and displayed at
 * runtime.
 */
public class ImageManipulationTool {

    @Arg(name = "input")
    protected File inputDir;

    @Arg(name = "output")
    protected File outputDir;

    @Arg
    protected int horizontalOffset;

    @Arg
    protected int verticalOffset;

    private static final Logger LOGGER = LogHelper.getLogger(ImageManipulationTool.class);

    public static void main(String[] argv) {
        CommandLineArgumentParser argParser = new CommandLineArgumentParser("ImageManipulationTool");
        ImageManipulationTool tool = argParser.parse(argv, ImageManipulationTool.class);

        try {
            tool.run();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "I/O error during image manipulation", e);
        }
    }

    protected void run() throws IOException {
        for (File imageFile : FileUtils.walkFiles(inputDir, this::isImageFile)) {
            LOGGER.info("Processing " + imageFile.getName());
            BufferedImage image = Utils2D.loadImage(imageFile);
            processImage(image);
        }
    }

    private boolean isImageFile(File file) {
        String ext = FileUtils.getFileExtension(file);
        return SpriteAtlasPacker.IMAGE_FILE_EXTENSIONS.contains(ext);
    }

    private void processImage(BufferedImage original) {
        if (horizontalOffset > 0) {
            applyHorizontalOffset(original);
        }

        if (verticalOffset > 0) {
            applyVerticalOffset(original);
        }
    }

    private void applyHorizontalOffset(BufferedImage original) {
        for (int x = 0; x < original.getWidth(); x += horizontalOffset) {
            BufferedImage image = createImage(original);
            Graphics2D g2 = Utils2D.createGraphics(image, true, true);
            g2.drawImage(original, x, 0, null);
            g2.drawImage(original, x - original.getWidth(), 0, null);
            g2.dispose();
            writePNG(image, "h-" + x + ".png");
        }
    }

    private void applyVerticalOffset(BufferedImage original) {
        for (int y = 0; y < original.getHeight(); y += verticalOffset) {
            BufferedImage image = createImage(original);
            Graphics2D g2 = Utils2D.createGraphics(image, true, true);
            g2.drawImage(original, 0, y, null);
            g2.drawImage(original, 0, y - original.getHeight(), null);
            g2.dispose();
            writePNG(image, "v-" + y + ".png");
        }
    }

    private BufferedImage createImage(BufferedImage original) {
        return new BufferedImage(original.getWidth(), original.getHeight(), TYPE_INT_ARGB);
    }

    private void writePNG(BufferedImage image, String name) {
        File outputFile = new File(outputDir, name.endsWith(".png") ? name : name + ".png");
        LOGGER.info("Creating " + outputFile.getAbsolutePath());

        try {
            Utils2D.savePNG(image, outputFile);
        } catch (IOException e) {
            throw new RuntimeException("Unable to write image: " + outputFile.getAbsolutePath());
        }
    }
}
