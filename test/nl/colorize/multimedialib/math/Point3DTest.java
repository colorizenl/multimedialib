//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import static nl.colorize.multimedialib.math.Point3D.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Point3DTest {

    @Test
    void distanceTo() {
        assertEquals(0, new Point3D(0, 0, 0).distanceTo(new Point3D(0, 0, 0)), EPSILON);
        assertEquals(2, new Point3D(0, 0, 0).distanceTo(new Point3D(2, 0, 0)), EPSILON);
        assertEquals(2.828, new Point3D(0, 0, 0).distanceTo(new Point3D(2, 2, 0)), EPSILON);
        assertEquals(3.464, new Point3D(0, 0, 0).distanceTo(new Point3D(2, 2, 2)), EPSILON);
    }

    @Test
    void interpolate() {
        Point3D a = new Point3D(2, 4, 0);
        Point3D b = new Point3D(12, 6, 8);

        assertEquals("(7, 5, 4)", a.findCenter(b).toString());
        assertEquals("(7, 5, 4)", a.interpolate(b, 0.5f).toString());
        assertEquals("(10, 6, 6)", a.interpolate(b, 0.75f).toString());
    }

    @Test
    void move() {
        assertEquals("(12, 21, 30)", new Point3D(2, 1, 0).move(10, 20, 30).toString());
        assertEquals("(12, 21, 30)", new Point3D(2, 1, 0).move(new Point3D(10, 20, 30)).toString());
    }

    @Test
    void negate() {
        Point3D point = new Point3D(10, 20, 30);

        assertEquals("(-10, -20, -30)", point.negate().toString());
        assertEquals("(10, 20, 30)", point.negate().negate().toString());
    }
}
