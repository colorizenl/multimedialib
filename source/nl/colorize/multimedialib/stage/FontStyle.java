//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;

public record FontStyle(
    String family,
    int size,
    boolean bold,
    ColorRGB color
) {

    public FontStyle {
        Preconditions.checkArgument(!family.isEmpty(), "Invalid font family");
        Preconditions.checkArgument(size >= 1, "Invalid font size: " + size);
    }
}
