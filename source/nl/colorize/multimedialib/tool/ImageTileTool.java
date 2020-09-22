//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.args4j.Option;

import com.google.common.base.Preconditions;

import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;
import nl.colorize.util.swing.Utils2D;

/**
 * Command line tool that performs image manipulation on images that are intended
 * to use used in tiled graphics.
 */
public class ImageTileTool extends CommandLineTool {

    @Option(name = "-input", required = true, usage = "Input image file")
    public File inputFile;

    @Option(name = "-horizontal", usage = "Times to tile the image horizontally")
    public int horizontal;

    @Option(name = "-vertical", usage = "Times to tile the image vertically")
    public int vertical;
    
    @Option(name = "-horizontalsplit", usage = "Splits the image using the specified offset")
    public int horizontalSplit = 0;
    
    @Option(name = "-verticalsplit", usage = "Splits the image using the specified offset")
    public int verticalSplit = 0;

    private static final Logger LOGGER = LogHelper.getLogger(ImageTileTool.class);

    public static void main(String[] args) {
        ImageTileTool tool = new ImageTileTool();
        tool.start(args);
    }

    @Override
    public void run() {
        try {
            BufferedImage image = Utils2D.loadImage(inputFile);
            List<BufferedImage> processed = processImage(image);
            
            File outputDir = new File(Platform.getUserDesktopDir(), "sprites");
            outputDir.mkdir();
            
            for (int i = 0; i < processed.size(); i++) {
                File outputFile = new File(outputDir,
                    inputFile.getName().replaceAll("-\\d*[.]png", "") + "-" + (i + 1) + ".png");

                Preconditions.checkState(!outputFile.exists(),
                    "The file " + outputFile.getAbsolutePath() + " already exists");

                LOGGER.info("Generating " + outputFile.getAbsolutePath());
                Utils2D.savePNG(processed.get(i), outputFile);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot tile image", e);
        }
    }

    private List<BufferedImage> processImage(BufferedImage image) {
        List<BufferedImage> processed = new ArrayList<>();
        
        if (horizontal > 0 || vertical > 0) {
            processed.add(tileImage(image));
        } else if (horizontalSplit > 0 || verticalSplit > 0) {
            processed.addAll(splitImage(image));
        }
        
        return processed;
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
    
    private List<BufferedImage> splitImage(BufferedImage image) {
        List<BufferedImage> processed = new ArrayList<>();
        
        if (horizontalSplit > 0) {
            for (int x = 0; x < image.getWidth(); x += horizontalSplit) {
                processed.add(splitImage(image, x, 0));
            }
        }
        
        if (verticalSplit > 0) {
            for (int y = 0; y < image.getHeight(); y += verticalSplit) {
                processed.add(splitImage(image, 0, y));
            }
        }
        
        return processed;
    }
    
    private BufferedImage splitImage(BufferedImage image, int xOffset, int yOffset) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(),
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = Utils2D.createGraphics(result, false, false);
        
        if (xOffset > 0) {
            g2.drawImage(image, -image.getWidth() + xOffset, 0, null);
            g2.drawImage(image, xOffset, 0, null);
        } else if (yOffset > 0) {
            g2.drawImage(image, 0, -image.getHeight() + yOffset, null);
            g2.drawImage(image, 0, yOffset, null);
        } else {
            g2.drawImage(image, 0, 0, null);
        }
        
        g2.dispose();
        
        return result;
    }
}
