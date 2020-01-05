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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Command line tool that creates an image by tiling an existing image both
 * horizontally and vertically.
 */
public class ImageTileTool extends CommandLineTool {

    @Option(name = "-input", required = true, usage = "Location of the input image")
    public File inputFile;

    @Option(name = "-horizontal", required = true, usage = "Times to tile the image horizontally")
    public int horizontal;

    @Option(name = "-vertical", required = true, usage = "Times to tile the image vertically")
    public int vertical;

    private static final Logger LOGGER = LogHelper.getLogger(ImageTileTool.class);

    public static void main(String[] args) {
        ImageTileTool tool = new ImageTileTool();
        tool.start(args);
    }

    @Override
    public void run() {
        try {
            BufferedImage image = Utils2D.loadImage(inputFile);
            BufferedImage result = tileImage(image);

            File outputFile = new File(inputFile.getParentFile(),
                inputFile.getName().replace(".png", "") + "-" + horizontal + "x" + vertical + ".png");

            Preconditions.checkState(!outputFile.exists(),
                "The file " + outputFile.getAbsolutePath() + " already exists");

            Utils2D.savePNG(result, outputFile);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot tile image", e);
        }
    }

    private BufferedImage tileImage(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth() * horizontal,
            image.getHeight() * vertical, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = Utils2D.createGraphics(result, false, false);

        for (int i = 0; i < horizontal; i++) {
            for (int j = 0; j < vertical; j++) {
                g2.drawImage(image, i * image.getWidth(), j * image.getHeight(), null);
            }
        }

        g2.dispose();

        return result;
    }
}
