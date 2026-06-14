//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;

/**
 * Immutable three-dimensional sphere defined by its center point and its
 * radius around the center point.
 */
public record Sphere(Point3D center, double radius) implements Shape3D {

    public Sphere {
        Preconditions.checkArgument(radius >= 0f, "Invalid radius: " + radius);
    }

    /**
     * Creates a sphere with its center point located at the origin (0, 0, 0).
     */
    public Sphere(double radius) {
        this(Point3D.ORIGIN, radius);
    }

    @Override
    public Box getBoundingBox() {
        return Box.around(center, radius * 2f, radius * 2f, radius * 2f);
    }

    @Override
    public Point3D getCenter() {
        return center;
    }

    @Override
    public boolean contains(Point3D point) {
        return center.distanceTo(point) <= radius;
    }

    @Override
    public Sphere reposition(Point3D offset) {
        return new Sphere(center.add(offset), radius);
    }
}
