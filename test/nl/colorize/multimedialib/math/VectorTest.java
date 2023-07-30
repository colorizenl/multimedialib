//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
    void setToPoint() {
        Vector vector = Vector.fromPoint(new Point2D(0f, 0f));
        assertEquals("[ 0 0 ]", vector.toString());

        vector = Vector.fromPoint(new Point2D(10f, 0f));
        assertEquals("[ 0 10 ]", vector.toString());

        vector = Vector.fromPoint(new Point2D(-10f, 0f));
        assertEquals("[ 180 10 ]", vector.toString());

        vector = Vector.fromPoint(new Point2D(0f, 10f));
        assertEquals("[ 90 10 ]", vector.toString());

        vector = Vector.fromPoint(new Point2D(0f, -10f));
        assertEquals("[ -90 10 ]", vector.toString());

        vector = Vector.fromPoint(new Point2D(10f, 10f));
        assertEquals("[ 45 14 ]", vector.toString());

        vector = Vector.fromPoint(new Point2D(-10f, -10f));
        assertEquals("[ -135 14 ]", vector.toString());
    }
}
