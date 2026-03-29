//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.google.common.base.Preconditions;
import lombok.Getter;
import nl.colorize.multimedialib.math.Region;
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

    public AWTImage(BufferedImage image) {
        Preconditions.checkArgument(image != null, "Image is null");

        this.image = image;
        this.region = new Region(0, 0, image.getWidth(), image.getHeight());
    }

    @Override
    public Image extractRegion(Region subRegion) {
        BufferedImage subImage = image.getSubimage(
            subRegion.x(), subRegion.y(), subRegion.width(), subRegion.height());
        return new AWTImage(subImage);
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
}
