//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;

/**
 * A circle that is defined by its center point and a radius. Instances of this
 * class are immutable and are defined with float precision.
 */
public record Circle(Point2D center, float radius) implements Shape {

    public Circle {
        Preconditions.checkArgument(radius >= 0f, "Invalid radius: " + radius);
    }

    public Circle(float x, float y, float radius) {
        this(new Point2D(x, y), radius);
    }

    public Circle(float radius) {
        this(Point2D.ORIGIN, radius);
    }

    @Override
    public Point2D getCenter() {
        return center;
    }

    @Override
    public boolean contains(Point2D p) {
        return center.distanceTo(p) <= radius;
    }

    /**
     * Returns true if this circle is either partially or entirely contained
     * within the other circle.
     */
    public boolean intersects(Circle other) {
        return center.distanceTo(other.center) <= radius + other.radius;
    }

    @Override
    public Rect getBoundingBox() {
        float diameter = 2f * radius;
        return new Rect(center.x() - radius, center.y() - radius, diameter, diameter);
    }

    @Override
    public Circle reposition(Point2D offset) {
        Point2D newCenter = center.add(offset.x(), offset.y());
        return new Circle(newCenter, radius);
    }

    @Override
    public String toString() {
        return "Circle";
    }
}
