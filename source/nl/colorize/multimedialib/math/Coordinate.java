//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

/**
 * Very simple data structure for representing 2D coordinates with integer
 * precision.
 */
public record Coordinate(int x, int y) {

    public static final Coordinate ORIGIN = new Coordinate(0, 0);

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
