//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import nl.colorize.multimedialib.math.MathUtils;
import nl.colorize.util.LogHelper;
import nl.colorize.util.swing.Utils2D;
import org.kohsuke.args4j.Option;

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
 *   <li>Fade the image's edges</li>
 * </ul>
 */
public class ImageManipulationTool extends CommandLineTool {

    @Option(name = "-input", required = true, usage = "Input directory containing the source images")
    public File inputDir;

    @Option(name = "-output", required = true, usage = "Output directory for generated results")
    public File outputDir;

    @Option(name = "-alpha", usage = "Introduce alpha channel (color in the format #000000)")
    public String alphaChannel = null;

    @Option(name = "-fade", usage = "Fade image edge alpha using the specified edge size")
    public int fade = 0;

    private static final Color REAL_ALPHA = new Color(0, 0, 0, 0);
    private static final Logger LOGGER = LogHelper.getLogger(ImageManipulationTool.class);

    public static void main(String[] args) {
        ImageManipulationTool tool = new ImageManipulationTool();
        tool.start(args);
    }

    @Override
    public void run() {
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

        if (fade > 0) {
            image = fadeEdges(image);
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

    private BufferedImage fadeEdges(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(),
            BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color oldColor = new Color(image.getRGB(x, y));
                int edgeDistance = getEdgeDistance(image, x, y);
                int alpha = MathUtils.clamp(Math.round(edgeDistance * (255f / fade)), 0, 255);
                Color newColor = new Color(oldColor.getRed(), oldColor.getGreen(),
                    oldColor.getBlue(), alpha);
                result.setRGB(x, y, newColor.getRGB());
            }
        }

        return result;
    }

    private int getEdgeDistance(BufferedImage image, int x, int y) {
        int right = image.getWidth() - 1 - x;
        int bottom = image.getHeight() - 1 - y;
        return Math.min(Math.min(Math.min(x, y), right), bottom);
    }
}
