//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import static nl.colorize.multimedialib.math.Shape.EPSILON;

/**
 * Describes a point with X, Y, and Z coordinates within a three-dimensional
 * space. Point coordinates have float precision, and point instances are
 * immutable.
 */
public record Point3D(float x, float y, float z) {

    public static final Point3D ORIGIN = new Point3D(0f, 0f, 0f);

    public boolean isOrigin() {
        return Math.abs(x) < EPSILON && Math.abs(y) < EPSILON && Math.abs(z) < EPSILON;
    }

    public Point3D normalize() {
        float length = (float) Math.sqrt(x * x + y * y + z * z);
        return new Point3D(x / length, y / length, z / length);
    }

    @Override
    public String toString() {
        return String.format("(%d, %d, %d)", Math.round(x), Math.round(y), Math.round(z));
    }
}
