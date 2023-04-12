//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.renderer.MediaAsset;
import nl.colorize.multimedialib.renderer.MediaLoader;

/**
 * Represents an image based on raster graphics. Images be loaded from PNG
 * or JPEG files using a {@link MediaLoader}.
 * <p>
 * Instances of an {@code Image} may represent either the entire underlying
 * image, or a rectangular region within the source image. That is, instances
 * do not always map directly to an image file. This aligns with the common
 * practice of including multiple images within a larger image, either as a
 * sprite sheet (for 2D graphics) or a texture (for 3D graphics).
 */
public interface Image extends MediaAsset {

    public Region getRegion();

    default int getWidth() {
        return getRegion().width();
    }

    default int getHeight() {
        return getRegion().height();
    }

    /**
     * Returns a new {@code Image} instance that is based on the same source
     * image, but only contains the specified rectangular region within the
     * source image.
     */
    public Image extractRegion(Region region);

    /**
     * Returns the RGB color value of a pixel within the image. This does not
     * include the pixel's alpha value even if the image does support
     * transparency. The alpha value can be retrieved separately using
     * {@link #getAlpha(int, int)}.
     */
    public ColorRGB getColor(int x, int y);

    /**
     * Returns the alpha of a pixel within the image. The returned value is
     * between 0 (fully transparent) and 100 (fully opaque).
     */
    public int getAlpha(int x, int y);
}
