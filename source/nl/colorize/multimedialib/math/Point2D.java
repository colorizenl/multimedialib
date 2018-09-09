//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import java.util.Objects;

/**
 * A mutable point describing two-dimensional coordinates.
 */
public class Point2D {

    private float x;
    private float y;

    public static final float EPSILON = 0.001f;

    public Point2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(float x, float y) {
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

    /**
     * Returns the distance between this point and the specified other point.
     */
    public float calculateDistance(Point2D other) {
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
    public float calculateAngle(Point2D other) {
        if (equals(other)) {
            return 0f;
        }

        float deltaX = other.x - x;
        float deltaY = other.y - y;

        return (float) Math.toDegrees(Math.atan2(deltaY, deltaX));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Point2D) {
            Point2D other = (Point2D) o;
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
        return String.format("(%.1f, %.1f)", x, y);
    }
}
