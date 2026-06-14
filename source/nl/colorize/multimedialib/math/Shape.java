//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

/**
 * Shared interface for all two-dimensional shapes. All shapes are defined
 * with {@code double} precision and are immutable.
 */
public interface Shape {

    public static final double EPSILON = Point2D.EPSILON;

    /**
     * Returns the smallest possible rectangular bounding box that can fit
     * this shape.
     */
    public Rect getBoundingBox();

    public Point2D getCenter();

    public boolean contains(Point2D p);

    /**
     * Returns a new {@link Shape} instance that is repositioned by the
     * specified offset.
     */
    public Shape reposition(Point2D offset);

    /**
     * Returns a new {@link Shape} instance that is repositioned by the
     * specified X and Y offset.
     */
    default Shape reposition(double offsetX, double offsetY) {
        return reposition(new Point2D(offsetX, offsetY));
    }
}
