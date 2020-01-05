//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;

/**
 * Circle that is defined by its center point and a radius, defined with float
 * precision.
 */
public class Circle implements Shape {

    private Point center;
    private float radius;

    public Circle(Point center, float radius) {
        Preconditions.checkArgument(radius > 0f, "Invalid radius: " + radius);

        this.center = center;
        this.radius = radius;
    }

    public Circle(float x, float y, float radius) {
        this(new Point(x, y), radius);
    }

    public Point getCenter() {
        return center;
    }

    public float getCenterX() {
        return center.getX();
    }

    public float getCenterY() {
        return center.getY();
    }

    public float getRadius() {
        return radius;
    }

    public float getDiameter() {
        return radius * 2f;
    }

    @Override
    public boolean contains(Point p) {
        return calculateDistance(p) <= radius;
    }

    public boolean intersects(Circle other) {
        return calculateDistance(other) <= radius + other.radius;
    }

    /**
     * Returns the distance between the center of this circle and the specified
     * point.
     */
    public float calculateDistance(Point p) {
        return center.calculateDistance(p);
    }

    /**
     * Returns the distance between the center of this circle and the center of
     * the specified other circle.
     */
    public float calculateDistance(Circle other) {
        return center.calculateDistance(other.center);
    }
}
