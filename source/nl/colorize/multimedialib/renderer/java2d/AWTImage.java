//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Image;

import java.awt.image.BufferedImage;

/**
 * Represents image data using Java 2D's {@link java.awt.image.BufferedImage}.
 * Images can be loaded from files using ImageIO, and can also be created
 * programmatically.
 */
public class AWTImage implements Image {

    private BufferedImage image;
    private FilePointer origin;
    
    public AWTImage(BufferedImage image, FilePointer origin) {
        Preconditions.checkArgument(image != null,
            "Null image originating from " + origin);
        
        this.image = image;
        this.origin = origin;
    }
    
    public AWTImage(BufferedImage image) {
        this(image, null);
    }
    
    public BufferedImage getImage() {
        return image;
    }

    @Override
    public Region getRegion() {
        return new Region(0, 0, image.getWidth(), image.getHeight());
    }

    @Override
    public Image extractRegion(Region region) {
        BufferedImage subImage = image.getSubimage(region.x(), region.y(),
            region.width(), region.height());
        return new AWTImage(subImage, origin);
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
