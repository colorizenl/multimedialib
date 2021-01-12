//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;
import nl.colorize.util.animation.Interpolation;

import java.util.Objects;

/**
 * Point with X and Y coordinates, defined with float precision.
 */
public class Point2D {

    private float x;
    private float y;

    public static final float EPSILON = Shape.EPSILON;

    public Point2D(float x, float y) {
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

    public void set(Point2D p) {
        set(p.getX(), p.getY());
    }

    public void move(float deltaX, float deltaY) {
        x += deltaX;
        y += deltaY;
    }

    public void add(float deltaX, float deltaY) {
        move(deltaX, deltaY);
    }

    public void add(Point2D p) {
        add(p.getX(), p.getY());
    }

    public void add(Vector vector) {
        add(vector.getX(), vector.getY());
    }

    public void multiply(float deltaX, float deltaY) {
        x *= deltaX;
        y *= deltaY;
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

        double radians = Math.atan2(other.y - y, other.x - x);
        float angle = (float) Math.toDegrees(radians);

        if (angle < 0f) {
            angle += 360f;
        }

        return angle;
    }
    
    /**
     * Returns a new point that is positioned in the center between this point
     * and the specified other point.
     */
    public Point2D calculateCenter(Point2D other) {
        float averageX = (x + other.x) / 2f;
        float averageY = (y + other.y) / 2f;
        
        return new Point2D(averageX, averageY);
    }
    
    /**
     * Returns a new point with a position that is interpolated between this
     * point and the specified other point. The delta is represented by a number
     * between 0.0 (position of this point) and 1.0 (position of the other point).
     */
    public Point2D interpolate(Point2D other, float delta, Interpolation method) {
        Preconditions.checkArgument(delta >= 0f && delta <= 1f,
            "Delta value out of range: " + delta);
        
        return new Point2D(method.interpolate(x, other.x, delta),
            method.interpolate(y, other.y, delta));
    }
    
    /**
     * Returns a new point with a position that uses linear interpolation between
     * this point and the specified other point. The delta is represented by a
     * number between 0.0 (position of this point) and 1.0 (position of the other
     * point).
     */
    public Point2D interpolate(Point2D other, float delta) {
        return interpolate(other, delta, Interpolation.LINEAR);
    }

    public boolean isOrigin() {
        return Math.abs(x) < EPSILON && Math.abs(y) < EPSILON;
    }

    public Point2D copy() {
        return new Point2D(x, y);
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
        return "(" + Math.round(x) + ", " + Math.round(y) + ")";
    }
}
