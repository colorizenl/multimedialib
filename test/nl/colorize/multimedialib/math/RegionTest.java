//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegionTest {

    @Test
    void bottomRightCoordinates() {
        Region region = new Region(10, 20, 30, 40);

        assertEquals(40, region.x1());
        assertEquals(60, region.y1());
    }

    @Test
    void move() {
        Region region = new Region(10, 20, 30, 40);
        Region moved = region.move(20, 30);

        assertEquals(30, moved.x());
        assertEquals(50, moved.y());
        assertEquals(30, moved.width());
        assertEquals(40, moved.height());
    }
}
