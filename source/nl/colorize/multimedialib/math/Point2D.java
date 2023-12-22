//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;
import lombok.Value;
import nl.colorize.util.animation.Interpolation;

import static nl.colorize.multimedialib.math.Shape.EPSILON;

/**
 * Describes a point with X and Y coordinates within a two-dimensional space.
 * Point coordinates have float precision, and point instances are immutable.
 */
@Value
public class Point2D {

    private float x;
    private float y;

    public static final Point2D ORIGIN = new Point2D(0f, 0f);

    public Point2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

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
    public float angleTo(Point2D other) {
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
    public Point2D findCenter(Point2D other) {
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
        return move(other.getX(), other.getY());
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", Math.round(x), Math.round(y));
    }
}
