//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import nl.colorize.util.animation.Interpolation;

/**
 * Immutable point within a three-dimensional space, with its X, Y, and Z
 * coordinates defined with float precision. Negative coordinates are
 * permitted.
 */
public record Point3D(float x, float y, float z) {

    public static final Point3D ORIGIN = new Point3D(0f, 0f, 0f);
    public static final float EPSILON = Point2D.EPSILON;

    public boolean isOrigin() {
        return Math.abs(x) < EPSILON && Math.abs(y) < EPSILON && Math.abs(z) < EPSILON;
    }

    /**
     * Returns the distance between this point and the specified other point.
     */
    public float distanceTo(Point3D other) {
        float deltaX = Math.abs(other.x - x);
        float deltaY = Math.abs(other.y - y);
        float deltaZ = Math.abs(other.z - z);
        return (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    /**
     * Returns a new point that is positioned in the center between this point
     * and the specified other point.
     */
    public Point3D findCenter(Point3D other) {
        float centerX = (x + other.x) / 2f;
        float centerY = (y + other.y) / 2f;
        float centerZ = (z + other.z) / 2f;
        return new Point3D(centerX, centerY, centerZ);
    }

    /**
     * Returns a new point with a position that is interpolated between this
     * point and the specified other point. The delta is represented by a number
     * between 0.0 (position of this point) and 1.0 (position of the other point).
     */
    public Point3D interpolate(Point3D other, float delta, Interpolation method) {
        float interpolatedX = method.interpolate(x, other.x, delta);
        float interpolatedY = method.interpolate(y, other.y, delta);
        float interpolatedZ = method.interpolate(z, other.z, delta);
        return new Point3D(interpolatedX, interpolatedY, interpolatedZ);
    }

    /**
     * Returns a new point with a position that uses linear interpolation between
     * this point and the specified other point. The delta is represented by a
     * number between 0.0 (position of this point) and 1.0 (position of the other
     * point).
     */
    public Point3D interpolate(Point3D other, float delta) {
        return interpolate(other, delta, Interpolation.LINEAR);
    }

    /**
     * Returns a new point that starts from this point and then adds the
     * specified offset.
     */
    public Point3D move(float deltaX, float deltaY, float deltaZ) {
        return new Point3D(x + deltaX, y + deltaY, z + deltaZ);
    }

    /**
     * Returns a new point that starts from this point and then adds the
     * specified offset.
     */
    public Point3D move(Point3D other) {
        return move(other.x, other.y, other.z);
    }

    @Override
    public String toString() {
        return String.format("(%d, %d, %d)", Math.round(x), Math.round(y), Math.round(z));
    }
}
