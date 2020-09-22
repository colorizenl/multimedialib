//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
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
        Preconditions.checkArgument(width >= 0f, "Invalid width: " + width);
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        Preconditions.checkArgument(height >= 0f, "Invalid height: " + height);
        this.height = height;
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

    public float getEndX() {
        return x + width;
    }

    public float getEndY() {
        return y + height;
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

    public Rect copy() {
        return new Rect(x, y, width, height);
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
        if (o instanceof Rect) {
            Rect other = (Rect) o;
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
