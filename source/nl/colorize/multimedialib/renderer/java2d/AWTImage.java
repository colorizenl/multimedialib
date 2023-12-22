//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.google.common.base.Preconditions;
import lombok.Getter;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Image;

import java.awt.image.BufferedImage;

/**
 * Implements the {@link Image} interface using an {@link BufferedImage},
 * which is part of the standard library.
 */
@Getter
public class AWTImage implements Image {

    private BufferedImage image;
    private Region region;
    private FilePointer origin;

    public AWTImage(BufferedImage image, FilePointer origin) {
        Preconditions.checkArgument(image != null, "Image is null");

        this.image = image;
        this.region = new Region(0, 0, image.getWidth(), image.getHeight());
        this.origin = origin;
    }

    @Override
    public Image extractRegion(Region subRegion) {
        BufferedImage subImage = image.getSubimage(
            subRegion.x(), subRegion.y(), subRegion.width(), subRegion.height());
        return new AWTImage(subImage, origin);
    }

    @Override
    public ColorRGB getColor(int x, int y) {
        int rgba = image.getRGB(x, y);
        return new ColorRGB(rgba);
    }

    @Override
    public int getAlpha(int x, int y) {
        int rgba = image.getRGB(x, y);
        int alpha = (rgba >> 24) & 0xFF;
        return Math.round(alpha / 2.55f);
    }

    @Override
    public String toString() {
        if (origin == null) {
            return "AWTImage";
        }
        return origin.toString();
    }
}
