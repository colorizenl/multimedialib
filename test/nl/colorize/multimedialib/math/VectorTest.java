//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.Test;

import static org.junit.Assert.*;

public class VectorTest {

    private static final float EPSILON = Vector.EPSILON;

    @Test
    public void testGetXY() {
        assertEquals(0f, new Vector(0f, 0f).getX(), EPSILON);
        assertEquals(0f, new Vector(0f, 0f).getY(), EPSILON);

        assertEquals(1f, new Vector(0f, 1f).getX(), EPSILON);
        assertEquals(0f, new Vector(0f, 1f).getY(), EPSILON);

        assertEquals(0.707f, new Vector(45f, 1f).getX(), EPSILON);
        assertEquals(0.707f, new Vector(45f, 1f).getY(), EPSILON);

        assertEquals(0f, new Vector(90f, 1f).getX(), EPSILON);
        assertEquals(1f, new Vector(90f, 1f).getY(), EPSILON);

        assertEquals(-1f, new Vector(180f, 1f).getX(), EPSILON);
        assertEquals(0f, new Vector(180f, 1f).getY(), EPSILON);

        assertEquals(0f, new Vector(270f, 1f).getX(), EPSILON);
        assertEquals(-1f, new Vector(270f, 1f).getY(), EPSILON);

        assertEquals(1f, new Vector(360f, 1f).getX(), EPSILON);
        assertEquals(0f, new Vector(360f, 1f).getY(), EPSILON);
    }

    @Test
    public void testEqualsConsidersEpsilon() {
        assertTrue(new Vector(0f, 1f).equals(new Vector(0f, 1f)));
        assertTrue(new Vector(0f, 1f).equals(new Vector(0f, 1.00001f)));
        assertFalse(new Vector(0f, 1f).equals(new Vector(0f, 1.01f)));
    }
}
