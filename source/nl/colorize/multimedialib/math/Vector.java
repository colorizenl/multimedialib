//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import java.util.Objects;

/**
 * A two-dimensional vector with a direction and a magnitude, both defined with
 * float precision. The direction of the vector is defined by an angle in
 * degrees. An angle of 0 degrees represents the vector (1, 0). Angles greater
 * than 0 rotate in a clockwise direction, so an angle of 180 degress would
 * represent the vector (-1, 0).
 */
public class Vector {

    private float direction;
    private float magnitude;

    public static final float EPSILON = Shape.EPSILON;

    public Vector(float direction, float magnitude) {
        set(direction, magnitude);
    }

    public void set(float direction, float magnitude) {
        setDirection(direction);
        setMagnitude(magnitude);
    }

    public void setDirection(float direction) {
        this.direction = direction;
    }

    public void addDirection(float delta) {
        setDirection(direction + delta);
    }

    public float getDirection() {
        return direction;
    }

    public void setMagnitude(float magnitude) {
        this.magnitude = Math.max(magnitude, 0f);
    }

    public void addMagnitude(float delta) {
        setMagnitude(magnitude + delta);
    }

    public float getMagnitude() {
        return magnitude;
    }

    public float getX() {
        return magnitude * (float) Math.cos(Math.toRadians(direction));
    }

    public float getY() {
        return magnitude * (float) Math.sin(Math.toRadians(direction));
    }

    public Point2D toPoint() {
        return new Point2D(getX(), getY());
    }

    /**
     * Changes this vector's direction and magnitude so that {@link #getX()}
     * and {@link #getY()} will refer to the specified point.
     */
    public void setToPoint(Point2D p) {
        if (p.isOrigin()) {
            magnitude = 0f;
        } else {
            direction = (float) Math.toDegrees(Math.atan2(p.getY(), p.getX()));
            magnitude = new Point2D(0f, 0f).distanceTo(p);
        }
    }

    public boolean isOrigin() {
        return magnitude < EPSILON;
    }

    public Vector copy() {
        return new Vector(direction, magnitude);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Vector) {
            Vector other = (Vector) o;
            return Math.abs(direction - other.direction) < EPSILON &&
                Math.abs(magnitude - other.magnitude) < EPSILON;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(direction, magnitude);
    }

    @Override
    public String toString() {
        return "[ " + Math.round(direction) + " " + Math.round(magnitude) + " ]";
    }
}
