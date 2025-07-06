//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import static nl.colorize.multimedialib.math.Shape.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Point2DTest {

    @Test
    public void testCalculateDistance() {
        assertEquals(0f, new Point2D(1f, 1f).distanceTo(new Point2D(1f, 1f)), EPSILON);
        assertEquals(1f, new Point2D(1f, 1f).distanceTo(new Point2D(2f, 1f)), EPSILON);
        assertEquals(1.414f, new Point2D(1f, 1f).distanceTo(new Point2D(2f, 2f)), EPSILON);
    }

    @Test
    public void testCalculateAngle() {
        assertEquals(0f, new Point2D(1f, 1f).angleTo(new Point2D(1f, 1f)).degrees(), EPSILON);
        assertEquals(0f, new Point2D(1f, 1f).angleTo(new Point2D(2f, 1f)).degrees(), EPSILON);
        assertEquals(180f, new Point2D(1f, 1f).angleTo(new Point2D(-2f, 1f)).degrees(), EPSILON);
        assertEquals(45f, new Point2D(1f, 1f).angleTo(new Point2D(2f, 2f)).degrees(), EPSILON);
        assertEquals(26.565f, new Point2D(1f, 1f).angleTo(new Point2D(3f, 2f)).degrees(), EPSILON);
    }

    @Test
    void isOrigin() {
        assertTrue(new Point2D(0f, 0f).isOrigin());
        assertTrue(new Point2D(0.0005f, 0f).isOrigin());
        assertTrue(new Point2D(0f, -0.0005f).isOrigin());

        assertFalse(new Point2D(0.1f, 0f).isOrigin());
        assertFalse(new Point2D(0f, -0.1f).isOrigin());
        assertFalse(new Point2D(1f, 1f).isOrigin());
    }
    
    @Test
    void calculateCenter() {
        Point2D origin = new Point2D(10, 20);
        
        Point2D center = origin.findCenter(new Point2D(10, 20));
        assertEquals(10f, center.x(), EPSILON);
        assertEquals(20f, center.y(), EPSILON);
        
        center = origin.findCenter(new Point2D(40, 20));
        assertEquals(25f, center.x(), EPSILON);
        assertEquals(20f, center.y(), EPSILON);
        
        center = origin.findCenter(new Point2D(-20, -20));
        assertEquals(-5f, center.x(), EPSILON);
        assertEquals(0f, center.y(), EPSILON);
    }
    
    @Test
    void interpolate() {
        Point2D a = new Point2D(10f, 20f);
        Point2D b = new Point2D(30f, 30f);
        
        assertEquals(10f, a.interpolate(b, 0f).x(), EPSILON);
        assertEquals(20f, a.interpolate(b, 0f).y(), EPSILON);
        
        assertEquals(20f, a.interpolate(b, 0.5f).x(), EPSILON);
        assertEquals(25f, a.interpolate(b, 0.5f).y(), EPSILON);
        
        assertEquals(25f, a.interpolate(b, 0.75f).x(), EPSILON);
        assertEquals(27.5f, a.interpolate(b, 0.75f).y(), EPSILON);
        
        assertEquals(30f, a.interpolate(b, 1f).x(), EPSILON);
        assertEquals(30f, a.interpolate(b, 1f).y(), EPSILON);
    }

    @Test
    void move() {
        assertEquals("(40, 60)", new Point2D(10, 20).move(30, 40).toString());
        assertEquals("(40, 60)", new Point2D(10, 20).move(new Point2D(30, 40)).toString());
    }

    @Test
    void multiply() {
        assertEquals("(10, 20)", new Point2D(10, 20).multiply(1f).toString());
        assertEquals("(20, 40)", new Point2D(10, 20).multiply(2f).toString());
        assertEquals("(5, 10)", new Point2D(10, 20).multiply(0.5f).toString());
    }

    @Test
    void negate() {
        Point2D point = new Point2D(10, 20);

        assertEquals("(-10, -20)", point.negate().toString());
        assertEquals("(10, 20)", point.negate().negate().toString());
    }
}
