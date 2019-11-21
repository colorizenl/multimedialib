//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import java.util.Objects;

/**
 * A mutable two-dimensional vector with float precision. The vector is defined
 * by a magnitude, and a direction which is an angle in degrees. An angle of 0
 * degrees represents the vector (1, 0). Angles greater than 0 rotate in a
 * clockwise direction, so an angle of 180 degress would represent the vector
 * (-1, 0).
 */
public class Vector {

    private float direction;
    private float magnitude;

    public static final float EPSILON = Shape.EPSILON;

    public Vector(float direction, float magnitude) {
        this.direction = direction;
        this.magnitude = magnitude;
    }

    public void setDirection(float direction) {
        this.direction = direction;
    }

    public float getDirection() {
        return direction;
    }

    public void setMagnitude(float magnitude) {
        this.magnitude = magnitude;
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

    public Point toPoint() {
        return new Point(getX(), getY());
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
        return String.format("Vector2D(direction=%.0f, magnitude=%.1f", direction, magnitude);
    }
}
