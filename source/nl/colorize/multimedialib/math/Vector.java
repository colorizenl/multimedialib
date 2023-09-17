//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import lombok.Value;

/**
 * A two-dimensional vector with a direction and a magnitude, both defined with
 * float precision. The direction of the vector is defined by an angle in
 * degrees. An angle of 0 degrees represents the vector (1, 0). Angles greater
 * than 0 rotate in a clockwise direction, so an angle of 180 degress would
 * represent the vector (-1, 0).
 */
@Value
public class Vector {

    private float direction;
    private float magnitude;

    public Vector(float direction, float magnitude) {
        this.direction = direction;
        this.magnitude = Math.max(magnitude, 0f);
    }

    public float getX() {
        return magnitude * (float) Math.cos(Math.toRadians(direction));
    }

    public float getY() {
        return magnitude * (float) Math.sin(Math.toRadians(direction));
    }

    public boolean isOrigin() {
        return magnitude < MathUtils.EPSILON;
    }

    public Vector withDirection(float newDirection) {
        return new Vector(newDirection, magnitude);
    }

    public Vector withMagnitude(float newMagnitude) {
        return new Vector(direction, newMagnitude);
    }

    @Override
    public String toString() {
        return String.format("[ %d %d ]", Math.round(direction), Math.round(magnitude));
    }

    /**
     * Creates a new vector with a direction and magnitude so that
     * {@link #getX()} and {@link #getY()} will refer to the specified
     * point.
     */
    public static Vector fromPoint(Point2D p) {
        if (p.isOrigin()) {
            return new Vector(0f, 0f);
        }

        float direction = (float) Math.toDegrees(Math.atan2(p.getY(), p.getX()));
        float magnitude = new Point2D(0f, 0f).distanceTo(p);
        return new Vector(direction, magnitude);
    }
}
