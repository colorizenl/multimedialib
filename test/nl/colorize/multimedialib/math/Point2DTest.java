//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Point2DTest {

    private static final float EPSILON = Point2D.EPSILON;

    @Test
    public void testEqualsConsidersEpsilon() {
        assertTrue(new Point2D(0f, 1f).equals(new Point2D(0f, 1f)));
        assertTrue(new Point2D(0f, 1f).equals(new Point2D(0f, 1.00001f)));
        assertFalse(new Point2D(0f, 1f).equals(new Point2D(0f, 1.01f)));
    }

    @Test
    public void testAdd() {
        Point2D point = new Point2D(1f, 1f);
        point.add(2f, 1f);

        assertEquals(new Point2D(3f, 2f), point);

        point.add(-4f, 0f);

        assertEquals(new Point2D(-1f, 2f), point);
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
        
        Point2D center = origin.calculateCenter(new Point2D(10, 20));
        assertEquals(10f, center.getX(), EPSILON);
        assertEquals(20f, center.getY(), EPSILON);
        
        center = origin.calculateCenter(new Point2D(40, 20));
        assertEquals(25f, center.getX(), EPSILON);
        assertEquals(20f, center.getY(), EPSILON);
        
        center = origin.calculateCenter(new Point2D(-20, -20));
        assertEquals(-5f, center.getX(), EPSILON);
        assertEquals(0f, center.getY(), EPSILON);
    }
    
    @Test
    void interpolate() {
        Point2D a = new Point2D(10f, 20f);
        Point2D b = new Point2D(30f, 30f);
        
        assertEquals(10f, a.interpolate(b, 0f).getX(), EPSILON);
        assertEquals(20f, a.interpolate(b, 0f).getY(), EPSILON);
        
        assertEquals(20f, a.interpolate(b, 0.5f).getX(), EPSILON);
        assertEquals(25f, a.interpolate(b, 0.5f).getY(), EPSILON);
        
        assertEquals(25f, a.interpolate(b, 0.75f).getX(), EPSILON);
        assertEquals(27.5f, a.interpolate(b, 0.75f).getY(), EPSILON);
        
        assertEquals(30f, a.interpolate(b, 1f).getX(), EPSILON);
        assertEquals(30f, a.interpolate(b, 1f).getY(), EPSILON);
    }
}
