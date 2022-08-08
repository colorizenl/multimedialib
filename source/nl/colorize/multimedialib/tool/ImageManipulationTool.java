//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import nl.colorize.util.LogHelper;
import nl.colorize.util.cli.CommandLineArgumentParser;
import nl.colorize.util.swing.Utils2D;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Set of command line tools for manipulating images in various ways. This can be
 * applied to either individual images or to a directory containing images.
 * Multiple operations can be combined if needed. The following operations are
 * available:
 *
 * <ul>
 *   <li>Introduce an alpha channel based on a certain color in the image</li>
 * </ul>
 */
public class ImageManipulationTool {

    protected File inputDir;
    protected File outputDir;
    protected String alphaChannel;
    protected int fade;

    private static final Color REAL_ALPHA = new Color(0, 0, 0, 0);
    private static final Logger LOGGER = LogHelper.getLogger(ImageManipulationTool.class);

    public static void main(String[] args) {
        CommandLineArgumentParser argParser = new CommandLineArgumentParser("ImageManipulationTool")
            .add("--input", "Input directory containing the source images")
            .add("--output", "Output directory for generated results")
            .addOptional("--alpha", null, "Introduce alpha channel (color in the format #000000)");

        argParser.parseArgs(args);

        ImageManipulationTool tool = new ImageManipulationTool();
        tool.inputDir = argParser.getFile("input");
        tool.outputDir = argParser.getFile("output");
        tool.alphaChannel = argParser.get("alpha");
        tool.fade = argParser.getInt("fade");
        tool.run();
    }

    protected void run() {
        outputDir.mkdir();

        try {
            List<File> imageFiles = Files.walk(inputDir.toPath())
                .map(path -> path.toFile())
                .filter(file -> isImageFile(file))
                .collect(Collectors.toList());

            for (File imageFile : imageFiles) {
                LOGGER.info("Converting image " + imageFile.getName());
                BufferedImage result = processImage(imageFile);

                File outputFile = new File(outputDir, imageFile.getName());
                Utils2D.savePNG(result, outputFile);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while converting images", e);
        }
    }

    private boolean isImageFile(File file) {
        if (file.isDirectory()) {
            return false;
        }
        return file.getName().endsWith(".png") || file.getName().endsWith(".jpg");
    }

    private BufferedImage processImage(File file) throws IOException {
        BufferedImage image = Utils2D.loadImage(file);

        if (alphaChannel != null) {
            image = introduceAlphaChannel(image);
        }

        return image;
    }

    private BufferedImage introduceAlphaChannel(BufferedImage original) {
        BufferedImage result = new BufferedImage(original.getWidth(), original.getHeight(),
            BufferedImage.TYPE_INT_ARGB);
        Color alphaChannelColor = Utils2D.parseHexColor(alphaChannel);

        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                int rgb = original.getRGB(x, y);
                if (rgb == alphaChannelColor.getRGB()) {
                    rgb = REAL_ALPHA.getRGB();
                }

                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }
}
