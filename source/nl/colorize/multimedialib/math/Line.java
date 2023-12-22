//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import lombok.Value;

/**
 * Describes a straight line between two points within a two-dimensional space.
 * {@link Line} instances can only be used to describe simple, straight lines.
 * Use {@link SegmentedLine} to describe more complex line shapes.
 */
@Value
public class Line implements Shape {

    private Point2D start;
    private Point2D end;

    public Line(Point2D start, Point2D end) {
        this.start = start;
        this.end = end;
    }

    public Line(float startX, float startY, float endX, float endY) {
        this(new Point2D(startX, startY), new Point2D(endX, endY));
    }

    @Override
    public boolean contains(Point2D p) {
        return p.equals(start) || p.equals(end);
    }

    @Override
    public Rect getBoundingBox() {
        float x0 = Math.min(start.getX(), end.getX());
        float y0 = Math.min(start.getY(), end.getY());
        float x1 = Math.max(start.getX(), end.getX());
        float y1 = Math.max(start.getY(), end.getY());

        return Rect.fromPoints(x0, y0, x1, y1);
    }

    @Override
    public Line reposition(Point2D offset) {
        return new Line(
            start.getX() + offset.getX(),
            start.getY() + offset.getY(),
            end.getX() + offset.getX(),
            end.getY() + offset.getY()
        );
    }

    @Override
    public String toString() {
        return start + " -> " + end;
    }
}
