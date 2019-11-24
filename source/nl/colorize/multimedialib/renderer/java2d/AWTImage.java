//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.math.Rect;

import java.awt.image.BufferedImage;

/**
 * Represents image data using Java 2D's {@link java.awt.image.BufferedImage}.
 * Images can be loaded from files using ImageIO, and can also be created
 * programmatically.
 */
public class AWTImage implements Image {

    private BufferedImage image;
    
    public AWTImage(BufferedImage image) {
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
        BufferedImage subImage = image.getSubimage(Math.round(region.getX()), Math.round(region.getY()),
            Math.round(region.getWidth()), Math.round(region.getHeight()));
        return new AWTImage(subImage);
    }

    @Override
    public ColorRGB getColor(int x, int y) {
        Preconditions.checkArgument(x >= 0 && x < getWidth() && y >= 0 && y < getHeight(),
            "Invalid coordinate: " + x + ", " + y);

        int rgba = image.getRGB(x, y);
        return new ColorRGB(rgba);
    }

    @Override
    public int getAlpha(int x, int y) {
        Preconditions.checkArgument(x >= 0 && x < getWidth() && y >= 0 && y < getHeight(),
            "Invalid coordinate: " + x + ", " + y);

        int rgba = image.getRGB(x, y);
        int alpha = (rgba >> 24) & 0xFF;
        return Math.round(alpha / 2.55f);
    }
}
