//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
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
public final class TTFont {

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

    public String getFamily() {
        return family;
    }

    public int getSize() {
        return size;
    }

    public ColorRGB getColor() {
        return color;
    }

    public boolean isBold() {
        return bold;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TTFont) {
            TTFont other = (TTFont) o;
            return family.equals(other.family) &&
                size == other.size &&
                color.equals(other.color) &&
                bold == other.bold;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return family.hashCode();
    }

    @Override
    public String toString() {
        return size + "px " + family + " (" + color + ")";
    }
}
