//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import nl.colorize.multimedialib.math.Rect;

/**
 * Represents an image based on raster graphics. Images be loaded from one of
 * the common file formats (for example PNG or JPEG), but can also be created
 * programmatically.
 */
public interface Image {

    public int getWidth();
    
    public int getHeight();

    /**
     * Returns an {@code Image} instance that only contains the specified
     * region from within this image.
     */
    public Image getRegion(Rect region);
}
