//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import nl.colorize.multimedialib.renderer.MediaLoader;

/**
 * Represents a TrueType font that can be used to draw text.
 * <p>
 * There are two ways to obtain a font. The first is to create an instance of
 * this class manually, which will use the local system fonts. If no font with
 * the requested name is available, this will revert to the system's default font.
 * The second approach is to load a font from a {@code .ttf} file using the
 * {@link MediaLoader} class. This ensures that the requested font is available.
 */
public class TrueTypeFont {

    private String family;
    private int size;
    private ColorRGB color;

    public static final int DEFAULT_SIZE = 12;

    public TrueTypeFont(String family, int size, ColorRGB color) {
        this.family = family;
        this.size = size;
        this.color = color;
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

    /**
     * Returns a version of this font that uses the same font family and color,
     * but has a different size.
     */
    public TrueTypeFont derive(int newSize) {
        return new TrueTypeFont(family, newSize, color);
    }

    /**
     * Returns a version of this font which uses the same font family and size,
     * but has a different color.
     */
    public TrueTypeFont derive(ColorRGB newColor) {
        return new TrueTypeFont(family, size, newColor);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TrueTypeFont) {
            TrueTypeFont other = (TrueTypeFont) o;
            return family.equals(other.family) && size == other.size;
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
        return family;
    }
}
