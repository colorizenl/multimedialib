//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;

/**
 * Immutable representation of a size where the width and height value defined
 * with integer precision. Negative or zero-size values are <em>not</em>
 * permitted.
 */
public record Size(int width, int height) {

    public Size {
        Preconditions.checkArgument(width > 0, "Invalid width: " + width);
        Preconditions.checkArgument(height > 0, "Invalid height: " + height);
    }

    public Size multiply(double factor) {
        Preconditions.checkArgument(factor > 0f, "Invalid factor: " + factor);
        return new Size((int) Math.round(width * factor), (int) Math.round(height * factor));
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }
}
