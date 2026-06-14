//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

/**
 * Immutable two-dimensional vector expressed using an origin, a direction,
 * and a magnitude.
 * <p>
 * The direction of the vector is defined by an angle in degrees. An angle
 * of 0 degrees represents the vector (1, 0). Angles greater than 0 rotate
 * in a clockwise direction, so an angle of 180 degress would represent the
 * vector (-1, 0).
 */
public record Vector(Point2D origin, Angle direction, double magnitude) {

    public Vector(Angle direction, double magnitude) {
        this(Point2D.ORIGIN, direction, magnitude);
    }

    public Vector(double direction, double magnitude) {
        this(Point2D.ORIGIN, new Angle(direction), magnitude);
    }

    public double getX() {
        return origin.x() + (magnitude * Math.cos(direction.getRadians()));
    }

    public double getY() {
        return origin.y() + (magnitude * Math.sin(direction.getRadians()));
    }

    public Point2D toPoint() {
        return new Point2D(getX(), getY());
    }

    public Vector withDirection(Angle newDirection) {
        return new Vector(origin, newDirection, magnitude);
    }

    public Vector withDirection(double newDirection) {
        return new Vector(origin, new Angle(newDirection), magnitude);
    }

    public Vector withMagnitude(double newMagnitude) {
        return new Vector(origin, direction, newMagnitude);
    }

    @Override
    public String toString() {
        String result = String.format("[ %s %d ]", direction, Math.round(magnitude));
        if (!origin.equals(Point2D.ORIGIN)) {
            result = origin + " " + result;
        }
        return result;
    }

    /**
     * Creates a new vector with a direction and magnitude so that the vector
     * points from {@link Point2D#ORIGIN} to the specified point.
     */
    public static Vector fromPoint(Point2D p) {
        if (p.isOrigin()) {
            return new Vector(0f, 0f);
        }

        double direction = Math.toDegrees(Math.atan2(p.y(), p.x()));
        double magnitude = Point2D.ORIGIN.distanceTo(p);
        return new Vector(direction, magnitude);
    }
}
