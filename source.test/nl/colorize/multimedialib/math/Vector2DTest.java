//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.Test;

import static org.junit.Assert.*;

public class Vector2DTest {

    private static final float EPSILON = Vector2D.EPSILON;

    @Test
    public void testGetXY() {
        assertEquals(0f, new Vector2D(0f, 0f).getX(), EPSILON);
        assertEquals(0f, new Vector2D(0f, 0f).getY(), EPSILON);

        assertEquals(1f, new Vector2D(0f, 1f).getX(), EPSILON);
        assertEquals(0f, new Vector2D(0f, 1f).getY(), EPSILON);

        assertEquals(0.707f, new Vector2D(45f, 1f).getX(), EPSILON);
        assertEquals(0.707f, new Vector2D(45f, 1f).getY(), EPSILON);

        assertEquals(0f, new Vector2D(90f, 1f).getX(), EPSILON);
        assertEquals(1f, new Vector2D(90f, 1f).getY(), EPSILON);

        assertEquals(-1f, new Vector2D(180f, 1f).getX(), EPSILON);
        assertEquals(0f, new Vector2D(180f, 1f).getY(), EPSILON);

        assertEquals(0f, new Vector2D(270f, 1f).getX(), EPSILON);
        assertEquals(-1f, new Vector2D(270f, 1f).getY(), EPSILON);

        assertEquals(1f, new Vector2D(360f, 1f).getX(), EPSILON);
        assertEquals(0f, new Vector2D(360f, 1f).getY(), EPSILON);
    }

    @Test
    public void testEqualsConsidersEpsilon() {
        assertTrue(new Vector2D(0f, 1f).equals(new Vector2D(0f, 1f)));
        assertTrue(new Vector2D(0f, 1f).equals(new Vector2D(0f, 1.00001f)));
        assertFalse(new Vector2D(0f, 1f).equals(new Vector2D(0f, 1.01f)));
    }
}