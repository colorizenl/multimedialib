//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.MediaLoader;

/**
 * Represents an image based on raster graphics. Images be loaded from one of
 * the common file formats (for example PNG or JPEG), but can also be created
 * programmatically. Images stored in files are loaded using a
 * {@link MediaLoader}.
 */
public interface Image {

    public int getWidth();
    
    public int getHeight();

    /**
     * Returns an {@code Image} instance that only contains the specified
     * region from within this image.
     */
    public Image getRegion(Rect region);

    public ColorRGB getColor(int x, int y);

    /**
     * Returns the alpha of a pixel within the image. The returned value is
     * between 0 (fully transparent) and 100 (fully opaque).
     */
    public int getAlpha(int x, int y);
}
