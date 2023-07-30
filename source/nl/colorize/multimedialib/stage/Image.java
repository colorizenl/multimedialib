//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.math.Region;

/**
 * Describes image data for an image that has been loaded by the renderer. It
 * is not possible to add {@link Image} instances directly to the stage. This
 * class represents the image contents, a {@link Sprite} can then be used to
 * add an <em>instance</em> of the image to the stage.
 * <p>
 * Instances of this class can represent either the entire underlying image,
 * or a rectangular region within the source image. This class allows both
 * situations to be used interchangeably.
 */
public interface Image {

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
