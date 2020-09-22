//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

/**
 * Shared interface for all two-dimensional shapes.
 */
public interface Shape {

    public static final float EPSILON = MathUtils.EPSILON;

    public boolean contains(Point2D p);
}
