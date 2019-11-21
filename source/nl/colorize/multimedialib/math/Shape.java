//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

/**
 * Shared interface for all two dimensional shapes with float precision
 * coordinates.
 */
public interface Shape {

    public static final float EPSILON = 0.001f;

    public boolean contains(Point point);
}
