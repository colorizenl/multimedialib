//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;

import java.util.Objects;

/**
 * Circle that is defined by its center point and a radius, defined with float
 * precision.
 */
public class Circle implements Shape {

    private Point2D center;
    private float radius;

    public Circle(Point2D center, float radius) {
        Preconditions.checkArgument(radius > 0f, "Invalid radius: " + radius);

        this.center = center.copy();
        this.radius = radius;
    }

    public Circle(float x, float y, float radius) {
        this(new Point2D(x, y), radius);
    }

    public Point2D getCenter() {
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
        return new Rect(center.getX() - radius, center.getY() - radius, 2f * radius, 2f * radius);
    }

    @Override
    public Circle copy() {
        return new Circle(center.copy(), radius);
    }

    @Override
    public Circle reposition(Point2D offset) {
        Circle result = copy();
        result.center.add(offset);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Circle other) {
            return Math.abs(center.getX() - other.center.getX()) < EPSILON &&
                Math.abs(center.getY() - other.center.getY()) < EPSILON &&
                Math.abs(radius - other.radius) < EPSILON;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(center.getX(), center.getY(), radius);
    }
}
