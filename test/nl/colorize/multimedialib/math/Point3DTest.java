//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Point3DTest {

    private static final float EPSILON = 0.001f;

    @Test
    public void testNormalize() {
        Point3D point = new Point3D(10f, 2f, 0f);
        Point3D normalized = point.normalize();

        assertEquals(0.98f, normalized.getX(), EPSILON);
        assertEquals(0.196f, normalized.getY(), EPSILON);
        assertEquals(0f, normalized.getZ(), EPSILON);
    }
}
