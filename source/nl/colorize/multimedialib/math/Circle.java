//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;

/**
 * Circle that is defined by its center point and a radius. Circles are
 * immutable and are defined with float precision.
 */
public record Circle(Point2D center, float radius) implements Shape {

    public Circle {
        Preconditions.checkArgument(radius >= 0f, "Invalid radius: " + radius);
    }

    public Circle(float x, float y, float radius) {
        this(new Point2D(x, y), radius);
    }

    @Override
    public Point2D getCenter() {
        return center;
    }

    @Override
    public boolean contains(Point2D p) {
        return calculateDistance(p) <= radius;
    }

    public boolean intersects(Circle other) {
        return calculateDistance(other) <= radius + other.radius;
    }

    /**
     * Returns the distance between the center of this circle and the specified
     * point.
     */
    public float calculateDistance(Point2D p) {
        return center.distanceTo(p);
    }

    /**
     * Returns the distance between the center of this circle and the center of
     * the specified other circle.
     */
    public float calculateDistance(Circle other) {
        return center.distanceTo(other.center);
    }

    @Override
    public Rect getBoundingBox() {
        return new Rect(center.x() - radius, center.y() - radius, 2f * radius, 2f * radius);
    }

    @Override
    public Circle reposition(Point2D offset) {
        Point2D newCenter = center.move(offset.x(), offset.y());
        return new Circle(newCenter, radius);
    }

    @Override
    public String toString() {
        return "Circle";
    }
}
