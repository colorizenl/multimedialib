//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;

import java.util.Objects;

/**
 * Two-dimensional rectangle with coordinates defined with float precision.
 */
public class Rect implements Shape {

    private float x;
    private float y;
    private float width;
    private float height;

    public Rect(float x, float y, float width, float height) {
        set(x, y, width, height);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        Preconditions.checkArgument(width >= 0f, "Invalid width: %s", width);
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        Preconditions.checkArgument(height >= 0f, "Invalid height: %s", height);
        this.height = height;
    }

    public float getCenterX() {
        return x + width / 2f;
    }

    public float getCenterY() {
        return y + height / 2f;
    }

    public float getEndX() {
        return x + width;
    }

    public float getEndY() {
        return y + height;
    }

    public Point2D getTopLeft() {
        return new Point2D(x, y);
    }

    public Point2D getTopRight() {
        return new Point2D(getEndX(), y);
    }

    public Point2D getBottomLeft() {
        return new Point2D(x, getEndY());
    }

    public Point2D getBottomRight() {
        return new Point2D(getEndX(), getEndY());
    }

    public Point2D getCenter() {
        return new Point2D(getCenterX(), getCenterY());
    }

    public void set(float x, float y, float width, float height) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
    }

    public void set(Rect r) {
        set(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    public void move(Point2D delta) {
        x += delta.getX();
        y += delta.getY();
    }

    @Override
    public boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
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
        return copy();
    }

    @Override
    public Rect copy() {
        return new Rect(x, y, width, height);
    }

    @Override
    public Rect reposition(Point2D offset) {
        Rect result = copy();
        result.x += offset.getX();
        result.y += offset.getY();
        return result;
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
    public boolean equals(Object o) {
        if (o instanceof Rect other) {
            return Math.abs(x - other.x) < EPSILON &&
                Math.abs(y - other.y) < EPSILON &&
                Math.abs(width - other.width) < EPSILON &&
                Math.abs(height - other.height) < EPSILON;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height);
    }

    @Override
    public String toString() {
        return Math.round(x) + ", " + Math.round(y) + ", " +
            Math.round(width) + ", " + Math.round(height);
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
        return new Rect(center.getX() - width / 2f, center.getY() - height / 2f, width, height);
    }
}
