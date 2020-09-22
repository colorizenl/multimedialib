//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a color built out of red, green, and blue components. The color
 * components have a value between 0 and 255, where (0, 0, 0) is black and
 * (255, 255, 255) is white.
 */
public final class ColorRGB {

    private int r;
    private int g;
    private int b;
    
    public static final ColorRGB BLACK = new ColorRGB(0, 0, 0);
    public static final ColorRGB WHITE = new ColorRGB(255, 255, 255);
    public static final ColorRGB GRAY = new ColorRGB(127, 127, 127);
    public static final ColorRGB RED = new ColorRGB(255, 0, 0);
    public static final ColorRGB GREEN = new ColorRGB(0, 255, 0);
    public static final ColorRGB BLUE = new ColorRGB(0, 0, 255);
    public static final ColorRGB YELLOW = new ColorRGB(255, 255, 0);
    
    /**
     * Creates a color from the specified red, green, and blue components.
     * @throws IllegalArgumentException if one of the color components is outside
     *         the range 0-255.
     */
    public ColorRGB(int r, int g, int b) {
        this.r = parseColorComponent(r);
        this.g = parseColorComponent(g);
        this.b = parseColorComponent(b);
    }

    /**
     * Creates a color from a single RGBA value. The value's alpha component
     * will be ignored.
     */
    public ColorRGB(int rgba) {
        this.r = (rgba >> 16) & 0xFF;
        this.g = (rgba >> 8) & 0xFF;
        this.b = rgba & 0xFF;
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public int getRGB() {
        int rgb = r;
        rgb = (rgb << 8) + g;
        rgb = (rgb << 8) + b;
        return rgb;
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
    public boolean equals(Object o) {
        if (o instanceof ColorRGB) {
            ColorRGB other = (ColorRGB) o;
            return r == other.r && g == other.g && b == other.b;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return ((0) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF));
    }
    
    @Override
    public String toString() {
        return "ColorRGB(" + r + ", " + g + ", " + b + ")";
    }
    
    /**
     * Returns this color in hexidecimal notation. For example, the color red
     * (255, 0, 0) will return "#FF0000".
     */
    public String toHex() {
        StringBuilder hex = new StringBuilder(7);
        hex.append('#');
        hex.append(toHex(r));
        hex.append(toHex(g));
        hex.append(toHex(b));
        return hex.toString().toUpperCase();
    }
    
    private String toHex(int component) {
        String str = Integer.toHexString(component);
        return (str.length() > 1) ? str : "0" + str;
    }

    /**
     * Parses a color from hexadecimal notation. For example, parsing the string
     * "#FF0000" will return red (255, 0, 0).
     * @throws IllegalArgumentException if {@code hex} is not a valid color.
     */
    public static ColorRGB parseHex(String hex) {
        if (hex.length() != 6 && hex.length() != 7) {
            throw new IllegalArgumentException("Invalid hexadecimal color: " + hex);
        }
        
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        
        return new ColorRGB(Integer.parseInt(hex.substring(0, 2), 16),
                Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16));
    }
    
    private static int parseColorComponent(int colorComponent) {
        Preconditions.checkArgument(colorComponent >= 0 && colorComponent <= 255,
                "Color component out of range: " + colorComponent);
        return colorComponent;
    }
}
