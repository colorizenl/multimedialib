//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

/**
 * Describes a straight line between two points within a two-dimensional space.
 * {@link Line} instances can only be used to describe simple, straight lines.
 * Use {@link SegmentedLine} to describe more complex line shapes.
 */
public record Line(Point2D start, Point2D end) implements Shape {

    public Line(float startX, float startY, float endX, float endY) {
        this(new Point2D(startX, startY), new Point2D(endX, endY));
    }

    @Override
    public boolean contains(Point2D p) {
        return p.equals(start) || p.equals(end);
    }

    @Override
    public Rect getBoundingBox() {
        float x0 = Math.min(start.x(), end.x());
        float y0 = Math.min(start.y(), end.y());
        float x1 = Math.max(start.x(), end.x());
        float y1 = Math.max(start.y(), end.y());

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
