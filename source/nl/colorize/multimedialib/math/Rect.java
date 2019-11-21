//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;

import java.util.Objects;

/**
 * A mutable two-dimensional rectangle. The coordinates are defined with float
 * precision.
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
    public boolean contains(Point p) {
        return p.getX() >= x && p.getX() <= x + width &&
            p.getY() >= y && p.getY() <= y + height;
    }

    public boolean contains(Rect r) {
        return r.x >= x && r.x + r.width <= x + width &&
            r.y >= y && r.y + r.height <= y + height;
    }

    public boolean intersects(Rect r) {
        return !(r.x + r.width < x || r.x > x + width ||
            r.y + r.height < y || r.y > y + height);
    }

    public Rect copy() {
        return new Rect(x, y, width, height);
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
        return String.format("(%.1f, %.1f, %.1f, %.1f)", x, y, width, height);
    }
}
