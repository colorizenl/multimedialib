//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RectTest {

    @Test
    public void testContainsPoint() {
        Rect rect = new Rect(10f, 20f, 30f, 40f);

        assertTrue(rect.contains(new Point2D(10f, 20f)));
        assertTrue(rect.contains(new Point2D(25f, 20f)));
        assertTrue(rect.contains(new Point2D(40f, 20f)));
        assertTrue(rect.contains(new Point2D(10f, 40f)));
        assertTrue(rect.contains(new Point2D(25f, 40f)));
        assertTrue(rect.contains(new Point2D(40f, 40f)));
        assertTrue(rect.contains(new Point2D(10f, 60f)));
        assertTrue(rect.contains(new Point2D(25f, 60f)));
        assertTrue(rect.contains(new Point2D(40f, 60f)));

        assertFalse(rect.contains(new Point2D(9f, 40f)));
        assertFalse(rect.contains(new Point2D(41f, 40f)));
        assertFalse(rect.contains(new Point2D(25f, 19f)));
        assertFalse(rect.contains(new Point2D(25f, 61f)));
    }

    @Test
    public void testContainsRect() {
        Rect rect = new Rect(10f, 20f, 30f, 40f);

        assertTrue(rect.contains(new Rect(10f, 20f, 30f, 40f)));
        assertTrue(rect.contains(new Rect(20f, 30f, 10f, 10f)));

        assertFalse(rect.contains(new Rect(-5f, 30f, 10f, 10f)));
        assertFalse(rect.contains(new Rect(45f, 30f, 10f, 10f)));
        assertFalse(rect.contains(new Rect(20f, 5f, 10f, 10f)));
        assertFalse(rect.contains(new Rect(20f, 65f, 10f, 10f)));

        assertFalse(rect.contains(new Rect(5f, 30f, 10f, 10f)));
        assertFalse(rect.contains(new Rect(35f, 30f, 10f, 10f)));
        assertFalse(rect.contains(new Rect(20f, 15f, 10f, 10f)));
        assertFalse(rect.contains(new Rect(20f, 55f, 10f, 10f)));
    }

    @Test
    public void testIntersects() {
        Rect rect = new Rect(10f, 20f, 30f, 40f);

        assertTrue(rect.intersects(new Rect(10f, 20f, 30f, 40f)));
        assertTrue(rect.intersects(new Rect(20f, 30f, 10f, 10f)));

        assertFalse(rect.intersects(new Rect(-5f, 30f, 10f, 10f)));
        assertFalse(rect.intersects(new Rect(45f, 30f, 10f, 10f)));
        assertFalse(rect.intersects(new Rect(20f, 5f, 10f, 10f)));
        assertFalse(rect.intersects(new Rect(20f, 65f, 10f, 10f)));

        assertTrue(rect.intersects(new Rect(5f, 30f, 10f, 10f)));
        assertTrue(rect.intersects(new Rect(35f, 30f, 10f, 10f)));
        assertTrue(rect.intersects(new Rect(20f, 15f, 10f, 10f)));
        assertTrue(rect.intersects(new Rect(20f, 55f, 10f, 10f)));
    }

    @Test
    public void testAround() {
        assertEquals(new Rect(-150, -200, 300, 400), Rect.around(new Point2D(0, 0), 300, 400));
        assertEquals(new Rect(-50, 0, 300, 400), Rect.around(new Point2D(100, 200), 300, 400));
    }

    @Test
    void reposition() {
        Rect original = new Rect(10, 20, 30, 40);
        Rect result = original.reposition(new Point2D(10, 20));

        assertEquals("(10, 20, 30, 40)", original.toString());
        assertEquals("(20, 40, 30, 40)", result.toString());
    }

    @Test
    void fromPoints() {
        assertEquals("(10, 20, 20, 30)", Rect.fromPoints(10, 20, 30, 50).toString());
    }
}
