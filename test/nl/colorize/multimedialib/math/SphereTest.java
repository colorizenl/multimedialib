//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SphereTest {

    @Test
    void sphereContains() {
        Sphere sphere = new Sphere(new Point3D(10, 20, 30), 40);

        assertTrue(sphere.contains(new Point3D(-30, 20, 30)));
        assertTrue(sphere.contains(new Point3D(10, 20, 30)));
        assertTrue(sphere.contains(new Point3D(30, 20, 30)));

        assertFalse(sphere.contains(new Point3D(-31, 20, 30)));
        assertFalse(sphere.contains(new Point3D(-31, 20, 30)));
        assertFalse(sphere.contains(new Point3D(-30, 10, 30)));
    }
}