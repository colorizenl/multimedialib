//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import java.util.Objects;

/**
 * A mutable point describing two-dimensional coordinates. The coordinates
 * are defined with float precision.
 */
public class Point {

    private float x;
    private float y;

    public static final float EPSILON = Shape.EPSILON;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getX() {
        return x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getY() {
        return y;
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(Point p) {
        set(p.getX(), p.getY());
    }

    public void add(float deltaX, float deltaY) {
        x += deltaX;
        y += deltaY;
    }

    /**
     * Returns the distance between this point and the specified other point.
     */
    public float calculateDistance(Point other) {
        if (equals(other)) {
            return 0f;
        }

        float deltaX = Math.abs(other.x - x);
        float deltaY = Math.abs(other.y - y);

        return (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    /**
     * Returns the angle in degrees from this point towards the specified other
     * point. If the points are identical this will return an angle of 0.
     */
    public float calculateAngle(Point other) {
        if (equals(other)) {
            return 0f;
        }

        float deltaX = other.x - x;
        float deltaY = other.y - y;

        return (float) Math.toDegrees(Math.atan2(deltaY, deltaX));
    }

    public Point copy() {
        return new Point(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Point) {
            Point other = (Point) o;
            return Math.abs(x - other.x) < EPSILON && Math.abs(y - other.y) < EPSILON;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return Math.round(x) + ", " + Math.round(y);
    }
}
