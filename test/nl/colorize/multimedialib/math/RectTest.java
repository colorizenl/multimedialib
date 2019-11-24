//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.Test;

import static org.junit.Assert.*;

public class RectTest {

    @Test
    public void testContainsPoint() {
        Rect rect = new Rect(10f, 20f, 30f, 40f);

        assertTrue(rect.contains(new Point(10f, 20f)));
        assertTrue(rect.contains(new Point(25f, 20f)));
        assertTrue(rect.contains(new Point(40f, 20f)));
        assertTrue(rect.contains(new Point(10f, 40f)));
        assertTrue(rect.contains(new Point(25f, 40f)));
        assertTrue(rect.contains(new Point(40f, 40f)));
        assertTrue(rect.contains(new Point(10f, 60f)));
        assertTrue(rect.contains(new Point(25f, 60f)));
        assertTrue(rect.contains(new Point(40f, 60f)));

        assertFalse(rect.contains(new Point(9f, 40f)));
        assertFalse(rect.contains(new Point(41f, 40f)));
        assertFalse(rect.contains(new Point(25f, 19f)));
        assertFalse(rect.contains(new Point(25f, 61f)));
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
}
