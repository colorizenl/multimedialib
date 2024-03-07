//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

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
public record SegmentedLine(List<Point2D> points) implements Shape {

    public SegmentedLine {
        Preconditions.checkArgument(points.size() >= 2, "Too few points: " + points.size());
    }

    public List<Line> getSegments() {
        List<Line> segments = new ArrayList<>();
        for (int i = 1; i < points.size(); i++) {
            segments.add(new Line(points.get(i - 1), points.get(i)));
        }
        return segments;
    }

    public Point2D getHead() {
        return points.getFirst();
    }

    public Point2D getTail() {
        return points.getLast();
    }

    @Override
    public boolean contains(Point2D p) {
        return points.contains(p);
    }

    @Override
    public Rect getBoundingBox() {
        float minX = points.getFirst().x();
        float maxX = points.getFirst().x();
        float minY = points.getFirst().y();
        float maxY = points.getFirst().y();

        for (int i = 1; i < points.size(); i++) {
            minX = Math.min(minX, points.get(i).x());
            maxX = Math.max(maxX, points.get(i).x());
            minY = Math.min(minY, points.get(i).y());
            maxY = Math.max(maxY, points.get(i).y());
        }

        return Rect.fromPoints(minX, minY, maxX, maxY);
    }

    @Override
    public SegmentedLine reposition(Point2D offset) {
        List<Point2D> pointsCopy = points.stream()
            .map(p -> new Point2D(p.x() + offset.x(), p.y() + offset.y()))
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
