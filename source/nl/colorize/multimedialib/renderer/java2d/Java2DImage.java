//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.math.Rect;

import java.awt.image.BufferedImage;

/**
 * Represents image data using Java 2D's {@link java.awt.image.BufferedImage}.
 * Images can be loaded from files using ImageIO, and can also be created
 * programmatically.
 */
public class Java2DImage implements Image {

    private BufferedImage image;
    
    public Java2DImage(BufferedImage image) {
        this.image = image;
    }
    
    public BufferedImage getImage() {
        return image;
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    @Override
    public int getHeight() {
        return image.getHeight();
    }

    @Override
    public Image getRegion(Rect region) {
        BufferedImage subImage = image.getSubimage(region.getX(), region.getY(),
            region.getWidth(), region.getHeight());
        return new Java2DImage(subImage);
    }
}
