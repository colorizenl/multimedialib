//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;

/**
 * Immutable two-dimensional rectangle with float precision. Rectangles are
 * specified using their top-left coordinate, width, and height. The
 * rectangle's <em>location</em> permits negative coordinates, but its width
 * and height cannot be negative.
 */
public record Rect(float x, float y, float width, float height) implements Shape {

    public Rect {
        Preconditions.checkArgument(width >= 0f, "Invalid width: %s", width);
        Preconditions.checkArgument(height >= 0f, "Invalid height: %s", height);
    }

    public float getEndX() {
        return x + width;
    }

    public float getEndY() {
        return y + height;
    }

    public float getCenterX() {
        return x + width / 2f;
    }

    public float getCenterY() {
        return y + height / 2f;
    }

    public Point2D getCenter() {
        return new Point2D(getCenterX(), getCenterY());
    }

    @Override
    public boolean contains(Point2D p) {
        return contains(p.x(), p.y());
    }

    public boolean contains(float px, float py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }

    public boolean contains(Rect r) {
        return r.x >= x && r.x + r.width <= x + width && r.y >= y && r.y + r.height <= y + height;
    }

    public boolean intersects(Rect r) {
        return !(r.x + r.width < x || r.x > x + width || r.y + r.height < y || r.y > y + height);
    }

    @Override
    public Rect getBoundingBox() {
        return this;
    }

    @Override
    public Rect reposition(Point2D offset) {
        return new Rect(x + offset.x(), y + offset.y(), width, height);
    }

    /**
     * Expands this rectangle by the specified amount, and returns the
     * resulting new rectangle. The rectangle is expanded around its center,
     * i.e. {@code r.getCenter().equals(r.expand(...).getCenter())}.
     * Using a negative value for {@code amount} is possible and will result
     * in a rectangle that is smaller than the original.
     */
    public Rect expand(float amount) {
        return around(getCenter(), width + amount, height + amount);
    }

    public Polygon toPolygon() {
        return new Polygon(
            x, y,
            x + width, y,
            x + width, y + height,
            x, y + height
        );
    }

    @Override
    public String toString() {
        return String.format("(%d, %d, %d, %d)",
            Math.round(x), Math.round(y), Math.round(width), Math.round(height));
    }

    /**
     * Created a rectangle from 4 points (x0, y0, x1, y1) instead of requiring
     * its width and height.
     */
    public static Rect fromPoints(float x0, float y0, float x1, float y1) {
        return new Rect(x0, y0, x1 - x0, y1 - y0);
    }

    /**
     * Returns a rectangle that has X and Y coordinates so that the specified
     * point becomes its center.
     */
    public static Rect around(float x, float y, float width, float height) {
        return around(new Point2D(x, y), width, height);
    }

    /**
     * Returns a rectangle that has X and Y coordinates so that the specified
     * point becomes its center.
     */
    public static Rect around(Point2D center, float width, float height) {
        return new Rect(center.x() - width / 2f, center.y() - height / 2f, width, height);
    }

    /**
     * Returns a rectangle that has the specified width and height, with its
     * X and Y coordinates positioned so that the origin {@code (0, 0)} ends
     * up as the center of the rectangle.
     */
    public static Rect around(float width, float height) {
        return new Rect(-width / 2f, -height / 2f, width, height);
    }
}
