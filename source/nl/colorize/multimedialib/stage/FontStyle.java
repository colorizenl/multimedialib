//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;

/**
 * Describes a font's visual appearance.
 */
public record FontStyle(int size, boolean bold, ColorRGB color) {

    public FontStyle {
        Preconditions.checkArgument(size >= 1, "Invalid font size: " + size);
    }

    public FontStyle(int size, ColorRGB color) {
        this(size, false, color);
    }
}
