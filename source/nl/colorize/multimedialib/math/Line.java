//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

/**
 * Represents a line between two points with float precision.
 */
public class Line implements Shape {

    private Point2D start;
    private Point2D end;
    private int thickness;

    public Line(Point2D start, Point2D end, int thickness) {
        this.start = start;
        this.end = end;
        this.thickness = thickness;
    }

    public Line(float startX, float startY, float endX, float endY, int thickness) {
        this(new Point2D(startX, startY), new Point2D(endX, endY), thickness);
    }

    public Point2D getStart() {
        return start;
    }

    public Point2D getEnd() {
        return end;
    }

    public int getThickness() {
        return thickness;
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
    public Line copy() {
        return new Line(start.copy(), end.copy(), thickness);
    }

    @Override
    public Line reposition(Point2D offset) {
        Line result = copy();
        result.start.add(offset);
        result.end.add(offset);
        return result;
    }

    @Override
    public String toString() {
        return start + ", " + end;
    }
}
