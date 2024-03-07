//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import static nl.colorize.multimedialib.math.Shape.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class VectorTest {

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
        assertEquals("[ 0\u00B0 0 ]", vector.toString());

        vector = Vector.fromPoint(new Point2D(10f, 0f));
        assertEquals("[ 0\u00B0 10 ]", vector.toString());

        vector = Vector.fromPoint(new Point2D(-10f, 0f));
        assertEquals("[ 180\u00B0 10 ]", vector.toString());

        vector = Vector.fromPoint(new Point2D(0f, 10f));
        assertEquals("[ 90\u00B0 10 ]", vector.toString());

        vector = Vector.fromPoint(new Point2D(0f, -10f));
        assertEquals("[ 270\u00B0 10 ]", vector.toString());

        vector = Vector.fromPoint(new Point2D(10f, 10f));
        assertEquals("[ 45\u00B0 14 ]", vector.toString());

        vector = Vector.fromPoint(new Point2D(-10f, -10f));
        assertEquals("[ 225\u00B0 14 ]", vector.toString());
    }

    @Test
    void withDirection() {
        Vector vector = new Vector(new Point2D(10, 20), new Angle(90), 20);
        Vector result = vector.withDirection(180);

        assertEquals(result, new Vector(new Point2D(10, 20), new Angle(180), 20));
    }

    @Test
    void withMagnitude() {
        Vector vector = new Vector(new Point2D(10, 20), new Angle(90), 20);
        Vector result = vector.withMagnitude(30);

        assertEquals(result, new Vector(new Point2D(10, 20), new Angle(90), 30));
    }
}
