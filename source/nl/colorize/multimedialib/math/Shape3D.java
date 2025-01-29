//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

/**
 * Shared interface for all three-dimensional shapes. Shapes use coordinates
 * with float precision, and instances are immutable. Sister interface to
 * {@link Shape}, which targets 2D shapes.
 */
public interface Shape3D {

    public static final float EPSILON = Shape.EPSILON;

    /**
     * Returns the smallest possible bounding box that can fit this shape.
     */
    public Box getBoundingBox();

    public Point3D getCenter();

    public boolean contains(Point3D point);

    /**
     * Returns a new {@link Shape3D} instance that is repositioned by the
     * specified offset.
     */
    public Shape3D reposition(Point3D offset);
}
