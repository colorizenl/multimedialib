//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.Test;

import static org.junit.Assert.*;

public class PointTest {

    private static final float EPSILON = Point.EPSILON;

    @Test
    public void testEqualsConsidersEpsilon() {
        assertTrue(new Point(0f, 1f).equals(new Point(0f, 1f)));
        assertTrue(new Point(0f, 1f).equals(new Point(0f, 1.00001f)));
        assertFalse(new Point(0f, 1f).equals(new Point(0f, 1.01f)));
    }

    @Test
    public void testAdd() {
        Point point = new Point(1f, 1f);
        point.add(2f, 1f);

        assertEquals(new Point(3f, 2f), point);

        point.add(-4f, 0f);

        assertEquals(new Point(-1f, 2f), point);
    }

    @Test
    public void testCalculateDistance() {
        assertEquals(0f, new Point(1f, 1f).calculateDistance(new Point(1f, 1f)), EPSILON);
        assertEquals(1f, new Point(1f, 1f).calculateDistance(new Point(2f, 1f)), EPSILON);
        assertEquals(1.414f, new Point(1f, 1f).calculateDistance(new Point(2f, 2f)), EPSILON);
    }

    @Test
    public void testCalculateAngle() {
        assertEquals(0f, new Point(1f, 1f).calculateAngle(new Point(1f, 1f)), EPSILON);
        assertEquals(0f, new Point(1f, 1f).calculateAngle(new Point(2f, 1f)), EPSILON);
        assertEquals(180f, new Point(1f, 1f).calculateAngle(new Point(-2f, 1f)), EPSILON);
        assertEquals(45f, new Point(1f, 1f).calculateAngle(new Point(2f, 2f)), EPSILON);
    }
}
