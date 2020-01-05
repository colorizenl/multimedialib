//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.base.Preconditions;
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
 * Command line tool for performing color correction operations on all images
 * within a directory.
 */
public class ColorConversionTool extends CommandLineTool {

    @Option(name = "-input", required = true, usage = "Input directory containing the source images")
    public File inputDir;

    @Option(name = "-output", required = true, usage = "Output directory for generated results")
    public File outputDir;

    @Option(name = "-alpha", required = true, usage = "Alpha channel color in the format #000000")
    public String alphaChannel;

    private static final Color REAL_ALPHA = new Color(0, 0, 0, 0);
    private static final Logger LOGGER = LogHelper.getLogger(ColorConversionTool.class);

    public static void main(String[] args) {
        ColorConversionTool tool = new ColorConversionTool();
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
                convertImage(imageFile);
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

    private void convertImage(File file) throws IOException {
        File outputFile = new File(outputDir, file.getName());
        Preconditions.checkState(!outputFile.exists(),
            "Output file already exists: " + outputFile.getAbsolutePath());

        BufferedImage originalImage = Utils2D.loadImage(file);
        BufferedImage result = convertImage(originalImage);

        Utils2D.savePNG(result, outputFile);
    }

    private BufferedImage convertImage(BufferedImage original) {
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
