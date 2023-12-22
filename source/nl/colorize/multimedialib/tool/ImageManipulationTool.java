//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
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
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Command line tool for various forms of image manipulation that are not
 * available at runtime due to lack of platform support. This tool "bakes"
 * the effects into a new image, which can then be loaded and displayed at
 * runtime.
 * <p>
 * The following effects are supported:
 * <ul>
 *   <li>Generate variants with a horizontal offset to simulate texture animation.</li>
 *   <li>Generate variants with a vertical offset to simulate texture animation.</li>
 *   <li>Mirror the image horizontally and vertically so it can be used for tiling.</li>
 * </ul>
 */
public class ImageManipulationTool {

    @Arg(name = "input") protected File inputDir;
    @Arg(name = "output") protected File outputDir;
    @Arg(defaultValue = "") protected String outputPrefix;
    @Arg protected int horizontalOffset;
    @Arg protected int verticalOffset;
    @Arg protected boolean mirror;

    private int counter;

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

        if (mirror) {
            mirrorImage(original);
        }
    }

    private void applyHorizontalOffset(BufferedImage original) {
        for (int x = 0; x <= original.getWidth(); x += horizontalOffset) {
            int offset = x;

            generateVariant(original, g2 -> {
                g2.drawImage(original, offset, 0, null);
                g2.drawImage(original, offset - original.getWidth(), 0, null);
            });
        }
    }

    private void applyVerticalOffset(BufferedImage original) {
        for (int y = 0; y <= original.getHeight(); y += verticalOffset) {
            int offset = y;

            generateVariant(original, g2 -> {
                g2.drawImage(original, 0, offset, null);
                g2.drawImage(original, 0, offset - original.getHeight(), null);
            });
        }
    }

    private void mirrorImage(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();

        generateVariant(original, g2 -> {
            g2.drawImage(original, 0, 0, width / 2, height / 2, 0, 0, width, height, null);
            g2.drawImage(original, width, 0, width / 2, height / 2, 0, 0, width, height, null);
            g2.drawImage(original, 0, height, width / 2, height / 2, 0, 0, width, height, null);
            g2.drawImage(original, width, height, width / 2, height / 2, 0, 0, width, height, null);
        });
    }

    private void generateVariant(BufferedImage original, Consumer<Graphics2D> drawCallback) {
        BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(),
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = Utils2D.createGraphics(image, true, true);
        drawCallback.accept(g2);
        g2.dispose();
        writePNG(image);
    }

    private void writePNG(BufferedImage image) {
        counter++;
        File outputFile = new File(outputDir, outputPrefix + counter + ".png");

        if (outputFile.exists()) {
            LOGGER.warning(outputFile.getAbsolutePath() + " already exists");
            return;
        }

        try {
            FileUtils.mkdir(outputDir);
            Utils2D.savePNG(image, outputFile);
            LOGGER.info("Created " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Unable to write image: " + outputFile.getAbsolutePath());
        }
    }
}
