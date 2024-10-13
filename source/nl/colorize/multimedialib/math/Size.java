//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;

/**
 * Simple data structure for representing width and height in integer
 * precision. Negative or zero-size values are <em>not</em> permitted.
 */
public record Size(int width, int height) {

    public Size {
        Preconditions.checkArgument(width > 0, "Invalid width: " + width);
        Preconditions.checkArgument(height > 0, "Invalid height: " + height);
    }

    public Size multiply(float factor) {
        Preconditions.checkArgument(factor > 0f, "Invalid factor: " + factor);
        return new Size(Math.round(width * factor), Math.round(height * factor));
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }
}
