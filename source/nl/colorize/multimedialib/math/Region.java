//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;

/**
 * Rectangular region within an image. This class exists alongside {@link Rect}
 * because it uses integer-precision coordinates, does not allow negative
 * coordinates, and is immutable.
 */
public record Region(int x, int y, int width, int height) {

    public Region {
        Preconditions.checkArgument(x >= 0, "Invalid X: " + x);
        Preconditions.checkArgument(y >= 0, "Invalid Y: " + y);
        Preconditions.checkArgument(width >= 1, "Invalid width: " + width);
        Preconditions.checkArgument(height >= 1, "Invalid height: " + height);
    }

    public int x1() {
        return x + width;
    }

    public int y1() {
        return y + height;
    }

    @Override
    public String toString() {
        return x + ", " + y + ", " + width + ", " + height;
    }
}
