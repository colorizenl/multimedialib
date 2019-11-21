//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PolygonTest {

    private static final float EPSILON = 0.001f;

    @Test
    public void testPolygonPoints() {
        float[] points = {1, 2, 3, 4, 5, 6};
        Polygon polygon = new Polygon(points);
        assertEquals(3, polygon.getNumPoints());

        polygon.setPoints(new float[] {1, 2, 3, 4, 5, 6, 7, 8});
        assertEquals(4, polygon.getNumPoints());
        assertEquals(8, polygon.getPoints().length);
        assertEquals(1, polygon.getPointX(0), EPSILON);
        assertEquals(2, polygon.getPointY(0), EPSILON);
        assertEquals(5, polygon.getPointX(2), EPSILON);
        assertEquals(6, polygon.getPointY(2), EPSILON);
    }
    
    @Test
    public void testMove() {
        float[] points = {100, 100, 200, 150, 100, 200};
        Polygon polygon = new Polygon(points);
        polygon.move(100, 50);
        assertEquals("[200.0, 150.0, 300.0, 200.0, 200.0, 250.0]", polygon.toString());
    }

    @Test
    public void testContainsPoint() {
        float[] points = {100, 100, 200, 100, 200, 200, 100, 200};
        Polygon polygon = new Polygon(points);
        
        assertTrue(polygon.contains(new Point(150, 150)));
        assertFalse(polygon.contains(new Point(250, 150)));
        assertFalse(polygon.contains(new Point(200, 300)));
        assertTrue(polygon.contains(new Point(100, 100)));
        assertTrue(polygon.contains(new Point(200, 200)));
        assertTrue(polygon.contains(new Point(100, 150)));
    }

    @Test
    public void testCreateCircle() {
        Polygon circle = Polygon.createCircle(0, 0, 1f, 4);

        assertEquals(4, circle.getNumPoints());
        assertEquals(1f, circle.getPointX(0), Shape.EPSILON);
        assertEquals(0f, circle.getPointY(0), Shape.EPSILON);
        assertEquals(0f, circle.getPointX(1), Shape.EPSILON);
        assertEquals(1f, circle.getPointY(1), Shape.EPSILON);
        assertEquals(-1f, circle.getPointX(2), Shape.EPSILON);
        assertEquals(0f, circle.getPointY(2), Shape.EPSILON);
        assertEquals(0f, circle.getPointX(3), Shape.EPSILON);
        assertEquals(-1f, circle.getPointY(3), Shape.EPSILON);
    }
}
