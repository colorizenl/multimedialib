//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
