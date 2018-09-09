//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.Test;

import static org.junit.Assert.*;

public class Point2DTest {

    private static final float EPSILON = Point2D.EPSILON;

    @Test
    public void testEqualsConsidersEpsilon() {
        assertTrue(new Point2D(0f, 1f).equals(new Point2D(0f, 1f)));
        assertTrue(new Point2D(0f, 1f).equals(new Point2D(0f, 1.00001f)));
        assertFalse(new Point2D(0f, 1f).equals(new Point2D(0f, 1.01f)));
    }

    @Test
    public void testCalculateDistance() {
        assertEquals(0f, new Point2D(1f, 1f).calculateDistance(new Point2D(1f, 1f)), EPSILON);
        assertEquals(1f, new Point2D(1f, 1f).calculateDistance(new Point2D(2f, 1f)), EPSILON);
        assertEquals(1.414f, new Point2D(1f, 1f).calculateDistance(new Point2D(2f, 2f)), EPSILON);
    }

    @Test
    public void testCalculateAngle() {
        assertEquals(0f, new Point2D(1f, 1f).calculateAngle(new Point2D(1f, 1f)), EPSILON);
        assertEquals(0f, new Point2D(1f, 1f).calculateAngle(new Point2D(2f, 1f)), EPSILON);
        assertEquals(180f, new Point2D(1f, 1f).calculateAngle(new Point2D(-2f, 1f)), EPSILON);
        assertEquals(45f, new Point2D(1f, 1f).calculateAngle(new Point2D(2f, 2f)), EPSILON);
    }
}