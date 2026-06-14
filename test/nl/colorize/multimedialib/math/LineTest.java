//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LineTest {

    @Test
    void getBoundingBox() {
        Line line = new Line(10, 20, 30, 40);
        Rect boundingBox = line.getBoundingBox();

        assertEquals(new Rect(10, 20, 20, 20), boundingBox);
    }

    @Test
    void reposition() {
        Line line = new Line(10, 20, 30, 40);
        Line moved = (Line) line.reposition(30, 70);

        assertEquals(new Line(40, 90, 60, 110), moved);
    }

    @Test
    void intersect() {
        Line line = new Line(10, 20, 30, 20);

        assertEquals("(20, 20)", line.intersect(new Line(20, 10, 20, 30)).get().toString());
        assertEquals("(20, 20)", line.intersect(new Line(10, 10, 30, 30)).get().toString());
        assertEquals("(30, 20)", line.intersect(new Line(30, 10, 30, 30)).get().toString());

        assertFalse(line.intersect(new Line(10, 30, 30, 30)).isPresent());
        assertFalse(line.intersect(new Line(-10, 20, 0, 20)).isPresent());
        assertFalse(line.intersect(line).isPresent());
    }

    @Test
    void intersects() {
        Line line = new Line(10, 20, 30, 20);

        assertTrue(line.intersects(new Line(20, 10, 20, 30)));
        assertFalse(line.intersects(new Line(-10, 20, 0, 20)));
        assertFalse(line.intersects(line));
    }
}
