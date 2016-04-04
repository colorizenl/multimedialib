//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

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

	public int getR() {
		return r;
	}

	public int getG() {
		return g;
	}

	public int getB() {
		return b;
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
		return ((0 & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
	}
	
	@Override
	public String toString() {
		return String.format("ColorRGB(%d, %d, %d)", r, g, b);
	}
	
	/**
	 * Returns this color in hexidecimal notation. For example, the color red
	 * (255, 0, 0) will return "#FF0000".
	 */
	public String toHex() {
		StringBuilder hex = new StringBuilder(7);
		hex.append('#');
		hex.append(toHexString(r));
		hex.append(toHexString(g));
		hex.append(toHexString(b));
		return hex.toString().toUpperCase();
	}
	
	private String toHexString(int component) {
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
		if (colorComponent < 0 || colorComponent > 255) {
			throw new IllegalArgumentException("Color component out of range: " + colorComponent);
		}
		return colorComponent;
	}
}
