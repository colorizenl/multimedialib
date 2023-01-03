//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

/**
 * Shared interface for all two-dimensional shapes.
 */
public interface Shape {

    public static final float EPSILON = MathUtils.EPSILON;

    public boolean contains(Point2D p);

    /**
     * Returns the smallest possible rectanglular bounding box capable of
     * containing this shape.
     */
    public Rect getBoundingBox();

    /**
     * Creates a deep copy of the shape, so that any modifications made to the
     * copy will not affect the original instance.
     */
    public Shape copy();

    /**
     * Returns a new instance that is repositioned by the specified X and Y
     * offset. Any changes made to the new instance will not affect the
     * original.
     */
    public Shape reposition(Point2D offset);
}
