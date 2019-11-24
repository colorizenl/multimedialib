//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.Test;

import static org.junit.Assert.*;

public class CircleTest {

    @Test
    public void testContainsPoint() {
        Circle circle = new Circle(10f, 10f, 10f);

        assertTrue(circle.contains(new Point(10f, 10f)));
        assertTrue(circle.contains(new Point(5f, 10f)));
        assertTrue(circle.contains(new Point(15f, 10f)));

        assertFalse(circle.contains(new Point(0f, 0f)));
        assertFalse(circle.contains(new Point(20f, 20f)));
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
