//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import static nl.colorize.multimedialib.math.Shape3D.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoxTest {

    @Test
    void coordinates() {
        Box box = new Box(10, 20, 30, 40, 50, 60);

        assertEquals(50, box.getEndX(), EPSILON);
        assertEquals(70, box.getEndY(), EPSILON);
        assertEquals(90, box.getEndZ(), EPSILON);
        assertEquals(new Point3D(30, 45, 60), box.getCenter());
    }

    @Test
    void around() {
        Box atOrigin = Box.around(Point3D.ORIGIN, 40, 50, 60);
        Box atPosition = Box.around(new Point3D(10, 20, 30), 40, 50, 60);

        assertEquals("(-20, -25, -30, 40, 50, 60)", atOrigin.toString());
        assertEquals("(-10, -5, 0, 40, 50, 60)", atPosition.toString());
    }

    @Test
    void contains() {
        Box box = new Box(10, 20, 30, 40, 50, 60);

        assertTrue(box.contains(new Point3D(10, 20, 30)));
        assertTrue(box.contains(new Point3D(50, 20, 30)));

        assertFalse(box.contains(new Point3D(9, 20, 30)));
        assertFalse(box.contains(new Point3D(51, 20, 30)));
    }

    @Test
    void containsBox() {
        Box box = new Box(10, 20, 30, 40, 50, 60);

        assertTrue(box.contains(box));
        assertTrue(box.contains(new Box(20, 20, 30, 20, 50, 60)));
        assertFalse(box.contains(new Box(20, 20, 30, 40, 50, 60)));
        assertFalse(box.contains(new Box(-50, 20, 30, 40, 50, 60)));
    }

    @Test
    void intersects() {
        Box box = new Box(10, 20, 30, 40, 50, 60);

        assertTrue(box.intersects(box));
        assertTrue(box.intersects(new Box(20, 20, 30, 40, 50, 60)));
        assertTrue(box.intersects(new Box(-10, 20, 30, 40, 50, 60)));
        assertFalse(box.intersects(new Box(-60, 20, 30, 40, 50, 60)));
    }

    @Test
    void combine() {
        Box box = new Box(10, 20, 30, 40, 50, 60);
        Box other = new Box(20, 30, 40, 50, 60, 70);
        Box combined = box.combine(other);

        assertEquals("(10, 20, 30, 60, 70, 80)", combined.toString());
    }
}
