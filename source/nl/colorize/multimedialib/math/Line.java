//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import java.util.Optional;

/**
 * Immutable straight line between two points within a two-dimensional space.
 * {@link Line} instances can only be used to describe simple, straight lines.
 * Use {@link SegmentedLine} to describe more complex line shapes.
 */
public record Line(Point2D start, Point2D end) implements Shape {

    public Line(double startX, double startY, double endX, double endY) {
        this(new Point2D(startX, startY), new Point2D(endX, endY));
    }

    @Override
    public boolean contains(Point2D p) {
        return p.equals(start) || p.equals(end);
    }

    /**
     * Returns the intersection point between this line and the specified
     * other line, if any. Note this does <em>not</em> return an intersection
     * point if the two lines are overlapping but not intersecting.
     */
    public Optional<Point2D> intersect(Line other) {
        return intersect(start, end, other.start, other.end);
    }

    private Optional<Point2D> intersect(Point2D p1, Point2D p2, Point2D p3, Point2D p4) {
        double a1 = p2.y() - p1.y();
        double b1 = p1.x() - p2.x();
        double c1 = a1 * p1.x() + b1 * p1.y();

        double a2 = p4.y() - p3.y();
        double b2 = p3.x() - p4.x();
        double c2 = a2 * p3.x() + b2 * p3.y();

        double determinant = a1 * b2 - a2 * b1;

        if (determinant == 0.0) {
            return Optional.empty();
        }

        double x = (b2 * c1 - b1 * c2) / determinant;
        double y = (a1 * c2 - a2 * c1) / determinant;
        return Optional.of(new Point2D(x, y));
    }

    /**
     * Returns true if this line intersects with the specified other line.
     * Note this returns false if the two lines are overlapping but not
     * intersecting.
     */
    public boolean intersects(Line other) {
        return intersect(other).isPresent();
    }

    @Override
    public Rect getBoundingBox() {
        double x0 = Math.min(start.x(), end.x());
        double y0 = Math.min(start.y(), end.y());
        double x1 = Math.max(start.x(), end.x());
        double y1 = Math.max(start.y(), end.y());

        return Rect.fromPoints(x0, y0, x1, y1);
    }

    @Override
    public Point2D getCenter() {
        return getBoundingBox().getCenter();
    }

    @Override
    public Line reposition(Point2D offset) {
        return new Line(
            start.x() + offset.x(),
            start.y() + offset.y(),
            end.x() + offset.x(),
            end.y() + offset.y()
        );
    }

    @Override
    public String toString() {
        return start + " -> " + end;
    }
}
