//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import nl.colorize.util.animation.Interpolation;

/**
 * Immutable point within a two-dimensional space, with its X and Y coordinates
 * defined with float precision. Negative coordinates are permitted.
 */
public record Point2D(float x, float y) {

    public static final Point2D ORIGIN = new Point2D(0f, 0f);
    public static final float EPSILON = 0.001f;

    public boolean isOrigin() {
        return Math.abs(x) < EPSILON && Math.abs(y) < EPSILON;
    }

    /**
     * Returns the distance between this point and the specified other point.
     */
    public float distanceTo(Point2D other) {
        float deltaX = Math.abs(other.x - x);
        float deltaY = Math.abs(other.y - y);
        return (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    /**
     * Returns the angle in degrees from this point towards the specified other
     * point. If the points are identical this will return an angle of 0.
     */
    public Angle angleTo(Point2D other) {
        double radians = Math.atan2(other.y - y, other.x - x);
        float degrees = (float) Math.toDegrees(radians);
        return new Angle(degrees);
    }
    
    /**
     * Returns a new point that is positioned in the center between this point
     * and the specified other point.
     */
    public Point2D findCenter(Point2D other) {
        float centerX = (x + other.x) / 2f;
        float centerY = (y + other.y) / 2f;
        return new Point2D(centerX, centerY);
    }
    
    /**
     * Returns a new point with a position that is interpolated between this
     * point and the specified other point. The delta is represented by a number
     * between 0.0 (position of this point) and 1.0 (position of the other point).
     */
    public Point2D interpolate(Point2D other, float delta, Interpolation method) {
        float interpolatedX = method.interpolate(x, other.x, delta);
        float interpolatedY = method.interpolate(y, other.y, delta);
        return new Point2D(interpolatedX, interpolatedY);
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

    /**
     * Returns a new point that starts from this point and then adds the
     * specified offset.
     */
    public Point2D move(float deltaX, float deltaY) {
        return new Point2D(x + deltaX, y + deltaY);
    }

    /**
     * Returns a new point that starts from this point and then adds the
     * specified offset.
     */
    public Point2D move(Point2D other) {
        return move(other.x, other.y);
    }

    /**
     * Returns a new point by multiplying this point's X and Y values with
     * the specified factor.
     */
    public Point2D multiply(float factor) {
        return new Point2D(x * factor, y * factor);
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", Math.round(x), Math.round(y));
    }
}
