//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

/**
 * Shared interface for all two-dimensional shapes. Shapes use coordinates
 * with float precision, and instances are immutable.
 */
public interface Shape {

    public static final float EPSILON = MathUtils.EPSILON;

    /**
     * Returns whether this shape contains the specified point.
     */
    public boolean contains(Point2D p);

    /**
     * Returns the smallest possible rectangular bounding box that can fit
     * this shape.
     */
    public Rect getBoundingBox();

    /**
     * Returns a new {@link Shape} instance that is repositioned by the
     * specified X and Y offset.
     */
    public Shape reposition(Point2D offset);

    /**
     * Returns a new {@link Shape} instance that is repositioned by the
     * specified X and Y offset.
     */
    default Shape reposition(float offsetX, float offsetY) {
        return reposition(new Point2D(offsetX, offsetY));
    }
}
