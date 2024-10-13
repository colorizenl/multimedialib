//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

/**
 * Simple data structure for representing (X, Y) coordinates with integer
 * precision. Negative coordinates are permitted.
 */
public record Coordinate(int x, int y) {

    public static final Coordinate ORIGIN = new Coordinate(0, 0);

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
