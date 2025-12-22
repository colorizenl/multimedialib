//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a color built out of red, green, and blue components. The color
 * components have a value between 0 and 255, where (0, 0, 0) is black and
 * (255, 255, 255) is white.
 */
public record ColorRGB(int r, int g, int b) {
    
    public static final ColorRGB BLACK = new ColorRGB(0, 0, 0);
    public static final ColorRGB WHITE = new ColorRGB(255, 255, 255);
    public static final ColorRGB GRAY = new ColorRGB(127, 127, 127);
    public static final ColorRGB RED = new ColorRGB(255, 0, 0);
    public static final ColorRGB GREEN = new ColorRGB(0, 255, 0);
    public static final ColorRGB BLUE = new ColorRGB(0, 0, 255);
    public static final ColorRGB YELLOW = new ColorRGB(255, 255, 0);
    public static final ColorRGB ORANGE = new ColorRGB(255, 127, 0);
    public static final ColorRGB PURPLE = new ColorRGB(127, 64, 255);
    public static final ColorRGB PINK = new ColorRGB(255, 107, 228);
    
    /**
     * Creates a color from the specified red, green, and blue components.
     *
     * @throws IllegalArgumentException if one of the color components is outside
     *         the range 0-255.
     */
    public ColorRGB {
        Preconditions.checkArgument(r >= 0 && r <= 255, "Invalid red: " + r);
        Preconditions.checkArgument(g >= 0 && g <= 255, "Invalid green: " + g);
        Preconditions.checkArgument(b >= 0 && b <= 255, "Invalid blue: " + b);
    }

    /**
     * Creates a color from a single RGBA value. The value's alpha component
     * will be ignored.
     */
    public ColorRGB(int rgba) {
        this((rgba >> 16) & 0xFF, (rgba >> 8) & 0xFF, rgba & 0xFF);
    }

    public int getRGB() {
        int rgb = r;
        rgb = (rgb << 8) + g;
        rgb = (rgb << 8) + b;
        return rgb;
    }

    /**
     * Returns the hexadecimal color string describing this color. For
     * example, the color red will produce "#FF0000".
     */
    public String toHex() {
        String hex = "#" + toHex(r) + toHex(g) + toHex(b);
        return hex.toUpperCase();
    }

    private String toHex(int component) {
        String str = Integer.toHexString(component);
        return str.length() > 1 ? str : "0" + str;
    }

    /**
     * Returns a new color that adds the specified delta to this color's RGB
     * components. The resulting RGB values will be clamped to the range
     * (0, 255), so using this method cannot lead to invalid colors.
     */
    public ColorRGB alter(int deltaR, int deltaG, int deltaB) {
        return new ColorRGB(
            Math.clamp(r + deltaR, 0, 255),
            Math.clamp(g + deltaG, 0, 255),
            Math.clamp(b + deltaB, 0, 255)
        );
    }

    /**
     * Creates a list of colors that interpolate linearly between this color and
     * the target color.
     *
     * @throws IllegalArgumentException if the number of steps is not at least 2,
     *         or if the source and target colors are already identical.
     */
    public List<ColorRGB> interpolate(ColorRGB target, int steps) {
        Preconditions.checkArgument(!equals(target), "Cannot interpolate between identical colors");
        Preconditions.checkArgument(steps >= 2, "Need at least 2 colors for interpolation");

        int stepR = (target.r - r) / (steps - 1);
        int stepG = (target.g - g) / (steps - 1);
        int stepB = (target.b - b) / (steps - 1);

        List<ColorRGB> colors = new ArrayList<>();
        colors.add(this);

        for (int i = 1; i < steps - 1; i++) {
            colors.add(new ColorRGB(r + i * stepR, g + i * stepG, b + i * stepB));
        }
        colors.add(target);

        return colors;
    }

    @Override
    public String toString() {
        return toHex();
    }

    /**
     * Parses an RGB color from a hexadecimal color string. For example,
     * the string "#FF0000" returns red.
     *
     * @throws IllegalArgumentException if {@code hexColor} is not a valid
     *         hexadecimal color in the supported notation.
     */
    public static ColorRGB parseHex(String hexColor) {
        String hex = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;

        Preconditions.checkArgument(hex.length() == 3 || hex.length() == 6,
            "Invalid hexadecimal color string: " + hexColor);

        if (hex.length() == 3) {
            hex = ""
                + hex.charAt(0) + hex.charAt(0)
                + hex.charAt(1) + hex.charAt(1)
                + hex.charAt(2) + hex.charAt(2);
        }
        
        return new ColorRGB(
            Integer.parseInt(hex.substring(0, 2), 16),
            Integer.parseInt(hex.substring(2, 4), 16),
            Integer.parseInt(hex.substring(4, 6), 16)
        );
    }
}
