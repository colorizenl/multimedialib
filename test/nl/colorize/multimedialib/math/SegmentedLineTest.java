//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SegmentedLineTest {

    @Test
    void getSegments() {
        List<Point2D> points = List.of(
            new Point2D(10f, 20f),
            new Point2D(20f, 30f),
            new Point2D(30f, 10f)
        );
        SegmentedLine line = new SegmentedLine(points);
        List<Line> segments = line.getSegments();

        assertEquals(2, segments.size());
        assertEquals("(10, 20) -> (20, 30)", segments.get(0).toString());
        assertEquals("(20, 30) -> (30, 10)", segments.get(1).toString());
    }

    @Test
    void getHeadAndTail() {
        SegmentedLine line = new SegmentedLine(List.of(
            new Point2D(10f, 20f),
            new Point2D(20f, 30f),
            new Point2D(30f, 10f)
        ));

        assertEquals(new Point2D(10f, 20f), line.getHead());
        assertEquals(new Point2D(30f, 10f), line.getTail());
    }

    @Test
    void getSegmentsForSimpleLine() {
        List<Point2D> points = List.of(new Point2D(10f, 20f), new Point2D(20f, 30f));
        SegmentedLine line = new SegmentedLine(points);
        List<Line> segments = line.getSegments();

        assertEquals(1, segments.size());
        assertEquals("(10, 20) -> (20, 30)", segments.get(0).toString());
    }

    @Test
    void getBoundingBox() {
        List<Point2D> points = List.of(
            new Point2D(10f, 20f),
            new Point2D(20f, 30f),
            new Point2D(30f, 10f)
        );
        SegmentedLine line = new SegmentedLine(points);

        assertEquals("(10, 10, 20, 20)", line.getBoundingBox().toString());
    }

    @Test
    void extend() {
        List<Point2D> points = List.of(
            new Point2D(10f, 20f),
            new Point2D(20f, 30f),
            new Point2D(30f, 10f)
        );
        SegmentedLine line = new SegmentedLine(points);
        SegmentedLine extendedLine = line.extend(new Point2D(40, 20));

        assertEquals("(10, 20) -> (20, 30) -> (30, 10) -> (40, 20)", extendedLine.toString());
    }

    @Test
    void toDirectLine() {
        List<Point2D> points = List.of(
            new Point2D(10f, 20f),
            new Point2D(20f, 30f),
            new Point2D(30f, 10f)
        );

        SegmentedLine segmented = new SegmentedLine(points);
        Line direct = segmented.toDirectLine();

        assertEquals("(10, 20) -> (30, 10)", direct.toString());
    }
}
