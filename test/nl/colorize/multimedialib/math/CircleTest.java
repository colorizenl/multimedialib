//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CircleTest {

    @Test
    public void testContainsPoint() {
        Circle circle = new Circle(10f, 10f, 10f);

        assertTrue(circle.contains(new Point2D(10f, 10f)));
        assertTrue(circle.contains(new Point2D(5f, 10f)));
        assertTrue(circle.contains(new Point2D(15f, 10f)));

        assertFalse(circle.contains(new Point2D(0f, 0f)));
        assertFalse(circle.contains(new Point2D(20f, 20f)));
    }

    @Test
    public void testIntersectsWithCircle() {
        Circle circle = new Circle(10f, 10f, 10f);

        assertTrue(circle.intersects(circle));
        assertTrue(circle.intersects(new Circle(5f, 10f, 10f)));
        assertTrue(circle.intersects(new Circle(15f, 10f, 10f)));
        assertTrue(circle.intersects(new Circle(30f, 10f, 10f)));

        assertFalse(circle.intersects(new Circle(-20f, 10f, 10f)));
        assertFalse(circle.intersects(new Circle(40f, 10f, 10f)));
    }
}
