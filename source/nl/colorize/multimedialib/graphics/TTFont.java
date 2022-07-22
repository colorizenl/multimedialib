//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.renderer.MediaLoader;

/**
 * Represents a TrueType font that can be used to draw text. Fonts can be loaded
 * from {@code .ttf} using the {@link MediaLoader}, which will convert the font
 * into a format that can be used by the renderer.
 */
public class TTFont {

    //TODO this class uses the naming convention for Java records, but
    //     is unable to actually use records until they are supported
    //     by TeaVM.

    private String family;
    private int size;
    private ColorRGB color;
    private boolean bold;

    public TTFont(String family, int size, ColorRGB color, boolean bold) {
        Preconditions.checkArgument(family.length() >= 1, "Invalid font family: " + family);
        Preconditions.checkArgument(size >= 4, "Invalid font size: " + size);

        this.family = family;
        this.size = size;
        this.color = color;
        this.bold = bold;
    }

    public String family() {
        return family;
    }

    public int size() {
        return size;
    }

    public ColorRGB color() {
        return color;
    }

    public boolean bold() {
        return bold;
    }

    public int getLineHeight() {
        return Math.round(size * 1.8f);
    }
}
