//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
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
}
