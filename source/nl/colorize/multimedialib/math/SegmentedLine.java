//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * Line that consists of multiple segments, where each segment is a straight
 * line but the combination still allows for more complex line shapes. The
 * line is defined by a number of points, where each point except the "head"
 * and the "tail" acts as both the end of the previous segment and the start
 * of the next segment. Alternatively, segments can also be described by
 * {@link Line} instances using {@link #getSegments()}.
 */
@Value
public class SegmentedLine implements Shape {

    private List<Point2D> points;
    private List<Line> segments;

    public SegmentedLine(List<Point2D> points) {
        Preconditions.checkArgument(points.size() >= 2, "Insufficient points: " + points.size());

        this.points = points;
        this.segments = new ArrayList<>();

        for (int i = 1; i < points.size(); i++) {
            segments.add(new Line(points.get(i - 1), points.get(i)));
        }
    }

    public Point2D getHead() {
        return points.get(0);
    }

    public Point2D getTail() {
        return points.get(points.size() - 1);
    }

    @Override
    public boolean contains(Point2D p) {
        return points.contains(p);
    }

    @Override
    public Rect getBoundingBox() {
        float minX = points.get(0).getX();
        float maxX = points.get(0).getX();
        float minY = points.get(0).getY();
        float maxY = points.get(0).getY();

        for (int i = 1; i < points.size(); i++) {
            minX = Math.min(minX, points.get(i).getX());
            maxX = Math.max(maxX, points.get(i).getX());
            minY = Math.min(minY, points.get(i).getY());
            maxY = Math.max(maxY, points.get(i).getY());
        }

        return Rect.fromPoints(minX, minY, maxX, maxY);
    }

    @Override
    public SegmentedLine reposition(Point2D offset) {
        List<Point2D> pointsCopy = points.stream()
            .map(p -> new Point2D(p.getX() + offset.getX(), p.getY() + offset.getY()))
            .toList();

        return new SegmentedLine(pointsCopy);
    }

    /**
     * Returns a direct line that points from this segmented line's head to
     * its tail.
     */
    public Line toDirectLine() {
        return new Line(getHead(), getTail());
    }

    /**
     * Returns a new {@link SegmentedLine} that consists of the same segments
     * as this one plus one extra segment between this line's tail and the
     * specified point.
     */
    public SegmentedLine extend(Point2D p) {
        List<Point2D> extendedPoints = new ArrayList<>();
        extendedPoints.addAll(points);
        extendedPoints.add(p);

        return new SegmentedLine(extendedPoints);
    }

    @Override
    public String toString() {
        return Joiner.on(" -> ").join(points);
    }
}
