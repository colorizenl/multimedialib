//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;

/**
 * Immutable three-dimensional box with float precision. The box is specified
 * based on the position of its top-left corner and its size.
 */
public record Box(
    float x,
    float y,
    float z,
    float width,
    float height,
    float depth
) implements Shape3D {

    public Box {
        Preconditions.checkArgument(width >= 0f, "Invalid width: " + width);
        Preconditions.checkArgument(height >= 0f, "Invalid height: " + height);
        Preconditions.checkArgument(depth >= 0f, "Invalid depth: " + depth);
    }

    public float getEndX() {
        return x + width;
    }

    public float getEndY() {
        return y + height;
    }

    public float getEndZ() {
        return z + depth;
    }

    @Override
    public Box getBoundingBox() {
        return this;
    }

    @Override
    public Point3D getCenter() {
        return new Point3D(x + width / 2f, y + height / 2f, z + depth / 2f);
    }

    /**
     * Returns true if the specified point is located within this box.
     */
    @Override
    public boolean contains(Point3D point) {
        return point.x() >= x && point.x() <= x + width &&
            point.y() >= y && point.y() <= y + height &&
            point.z() >= z && point.z() <= z + depth;
    }

    /**
     * Returns true if the specified other box is entirely or partially located
     * within this box.
     */
    public boolean contains(Box other) {
        return other.x >= x &&
            other.x + other.width <= x + width &&
            other.y >= y &&
            other.y + other.height <= y + height &&
            other.z >= z &&
            other.z + other.depth <= z + depth;
    }

    /**
     * Returns true if the specified other box intersects with this box.
     */
    public boolean intersects(Box other) {
        boolean outside = other.x + other.width < x ||
            other.x > x + width ||
            other.y + other.height < y ||
            other.y > y + height ||
            other.z + other.depth < z ||
            other.z > z + depth;

        return !outside;
    }

    @Override
    public Box reposition(Point3D offset) {
        return new Box(x + offset.x(), y + offset.y(), z + offset.z(), width, height, depth);
    }

    /**
     * Returns a new box that encompasses both this box and the specified
     * other box.
     */
    public Box combine(Box other) {
        return fromPoints(
            Math.min(x, other.x),
            Math.min(y, other.y),
            Math.min(z, other.z),
            Math.max(getEndX(), other.getEndX()),
            Math.max(getEndY(), other.getEndY()),
            Math.max(getEndZ(), other.getEndZ())
        );
    }

    @Override
    public String toString() {
        return String.format("(%d, %d, %d, %d, %d, %d)",
            Math.round(x), Math.round(y), Math.round(z),
            Math.round(width), Math.round(height), Math.round(depth));
    }

    /**
     * Factory method that creates a box based on the points (x0, y0, z0) and
     * (x1, y1, z1).
     */
    public static Box fromPoints(float x0, float y0, float z0, float x1, float y1, float z1) {
        return new Box(x0, y0, z0, x1 - x0, y1 - y0, z1 - z0);
    }

    /**
     * Factory method that creates a box based on the location of its center
     * point.
     */
    public static Box around(Point3D center, float width, float height, float depth) {
        return new Box(
            center.x() - width / 2f,
            center.y() - height / 2f,
            center.z() - depth / 2f,
            width,
            height,
            depth
        );
    }

    /**
     * Factory method that creates a box with its center point located at the
     * origin (0, 0, 0).
     */
    public static Box aroundOrigin(float width, float height, float depth) {
        return around(Point3D.ORIGIN, width, height, depth);
    }
}
