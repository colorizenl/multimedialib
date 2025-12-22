//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------


package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoordinateTest {

    @Test
    void add() {
        Coordinate base = new Coordinate(1, 2);

        assertEquals("(4, 6)", base.add(3, 4).toString());
        assertEquals("(0, 2)", base.add(-1, 0).toString());
    }
}
