//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import nl.colorize.util.animation.Interpolation;

/**
 * Immutable point within a two-dimensional space, defined by its X and Y
 * coordinates. Negative coordinates are permitted.
 */
public record Point2D(double x, double y) {

    public static final Point2D ORIGIN = new Point2D(0f, 0f);
    public static final double EPSILON = 0.001f;

    public boolean isOrigin() {
        return Math.abs(x) < EPSILON && Math.abs(y) < EPSILON;
    }

    /**
     * Returns the distance between this point and the specified other point.
     */
    public double distanceTo(Point2D other) {
        double deltaX = Math.abs(other.x - x);
        double deltaY = Math.abs(other.y - y);
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    /**
     * Returns the angle in degrees from this point towards the specified other
     * point. If the points are identical this will return an angle of 0.
     */
    public Angle angleTo(Point2D other) {
        double radians = Math.atan2(other.y - y, other.x - x);
        double degrees = Math.toDegrees(radians);
        return new Angle(degrees);
    }
    
    /**
     * Returns a new point that is positioned in the center between this point
     * and the specified other point.
     */
    public Point2D findCenter(Point2D other) {
        double centerX = (x + other.x) / 2f;
        double centerY = (y + other.y) / 2f;
        return new Point2D(centerX, centerY);
    }
    
    /**
     * Returns a new point with a position that is interpolated between this
     * point and {@code other}. The {@code delta} value is a number between
     * zero (this point's position) and one (the other point's position).
     * The delta value is intentionally <em>not</em> clamped to the 0-1 range,
     * so that smaller or bigger values can be used to extrapolate.
     */
    public Point2D interpolate(Point2D other, double delta, Interpolation method) {
        double interpolatedX = method.interpolate(x, other.x, delta);
        double interpolatedY = method.interpolate(y, other.y, delta);
        return new Point2D(interpolatedX, interpolatedY);
    }

    /**
     * Returns a new point with a position based on linear interpolation
     * between this point and {@code other}. The {@code delta} value is a
     * number between zero (this point's position) and one (the other point's
     * position). The delta value is intentionally <em>not</em> clamped to
     * the 0-1 range, so that smaller or bigger values can be used to
     * extrapolate.
     */
    public Point2D interpolate(Point2D other, double delta) {
        return interpolate(other, delta, Interpolation.LINEAR);
    }

    /**
     * Returns a new point that starts from this point and then adds the
     * specified offset.
     */
    public Point2D add(double deltaX, double deltaY) {
        return new Point2D(x + deltaX, y + deltaY);
    }

    /**
     * Returns a new point that starts from this point and then adds the
     * specified offset.
     */
    public Point2D add(Point2D other) {
        return add(other.x, other.y);
    }

    /**
     * Returns a new point by multiplying this point's X and Y values with
     * the specified factor.
     */
    public Point2D multiply(double factor) {
        return new Point2D(x * factor, y * factor);
    }

    /**
     * Returns a new point that has X and Y values that are the inverse of
     * this point's X and Y values. Negating the result will result in the
     * original point, i.e. {@code point.negate().negate().equals(point)}.
     */
    public Point2D negate() {
        return new Point2D(-x, -y);
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", Math.round(x), Math.round(y));
    }
}
