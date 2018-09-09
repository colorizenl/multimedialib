//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for implementations of the {@code Shape} interface.
 */
public class ShapesTest {
    
    @Test
    public void testRectDimensions() {
        Rect rect = new Rect(10, 20, 30, 40);
        assertEquals(10, rect.getX());
        assertEquals(20, rect.getY());
        assertEquals(25, rect.getCenterX());
        assertEquals(40, rect.getCenterY());
        assertEquals(40, rect.getEndX());
        assertEquals(60, rect.getEndY());
    }
        
    @Test
    public void testPolygonPoints() {
        int[] points = {1, 2, 3, 4, 5, 6};
        Polygon polygon = new Polygon(points);
        assertEquals(3, polygon.getNumPoints());

        polygon.setPoints(new int[] {1, 2, 3, 4, 5, 6, 7, 8});
        assertEquals(4, polygon.getNumPoints());
        assertEquals(8, polygon.getPoints().length);
        assertEquals(1, polygon.getPointX(0));
        assertEquals(2, polygon.getPointY(0));
        assertEquals(5, polygon.getPointX(2));
        assertEquals(6, polygon.getPointY(2));
    }
    
    @Test
    public void testMove() {
        int[] points = {100, 100, 200, 150, 100, 200};
        Polygon polygon = new Polygon(points);
        polygon.move(100, 50);
        assertEquals("[200, 150, 300, 200, 200, 250]", polygon.toString());
    }
    
    @Test
    public void testRotate() {
        int[] points = {100, 100, 200, 100, 200, 200, 100, 200};
        Polygon polygon = new Polygon(points);
        polygon.rotateRadians(0.5 * Math.PI, 150, 150);
        assertEquals("[200, 100, 200, 200, 100, 200, 100, 100]", polygon.toString());
    }

    @Test
    public void testContainsPoint() {
        int[] points = {100, 100, 200, 100, 200, 200, 100, 200};
        Polygon polygon = new Polygon(points);
        
        assertTrue(polygon.contains(150, 150));
        assertFalse(polygon.contains(250, 150));
        assertFalse(polygon.contains(200, 300));
        assertTrue(polygon.contains(100, 100));
        assertTrue(polygon.contains(200, 200));
        assertTrue(polygon.contains(100, 150));
    }
    
    @Test
    public void testContainsPolygon() {
        Polygon first = new Polygon(new int[] {100, 100, 200, 100, 200, 200, 100, 200});
        Polygon second = new Polygon(new int[] {300, 300, 400, 300, 400, 400, 300, 400});
        Polygon third = new Polygon(new int[] {150, 150, 170, 150, 170, 170, 150, 170});
        
        assertFalse(first.contains(second));
        assertTrue(first.contains(third));
        assertTrue(first.contains(first));
    }
    
    @Test
    public void testIntersectsPolygon() {
        Polygon first = new Polygon(new int[] {100, 100, 200, 100, 200, 200, 100, 200});
        Polygon second = new Polygon(new int[] {300, 300, 400, 300, 400, 400, 300, 400});
        Polygon third = new Polygon(new int[] {180, 180, 220, 180, 220, 220, 180, 220});
        Polygon fourth = new Polygon(new int[] {150, 150, 170, 150, 170, 170, 150, 170});
        
        assertTrue(first.intersects(first));
        assertFalse(first.intersects(second));
        assertTrue(first.intersects(third));
        assertTrue(first.intersects(fourth));
    }
    
    @Test
    public void testRectContains() {
        Rect rect = new Rect(100, 200, 300, 400);
        assertFalse(rect.contains(99, 300));
        assertTrue(rect.contains(150, 250));
        assertTrue(rect.contains(101, 300));
        assertTrue(rect.contains(100, 200));
        assertTrue(rect.contains(400, 600));
        
        assertFalse(rect.contains(new Rect(0, 0, 50, 50)));
        assertFalse(rect.contains(new Rect(0, 200, 200, 100)));
        assertTrue(rect.contains(new Rect(200, 210, 50, 50)));
        assertTrue(rect.contains(rect));
    }
    
    @Test
    public void testRectIntersects() {
        Rect rect = new Rect(100, 200, 300, 400);
        assertTrue(rect.intersects(rect));
        assertFalse(rect.intersects(new Rect(0, 0, 50, 50)));
        assertTrue(rect.intersects(new Rect(0, 200, 200, 100)));
        assertTrue(rect.intersects(new Rect(200, 210, 50, 50)));
    }
    
    @Test
    public void testRectToPolygon() {
        int[] points = {100, 100, 200, 100, 200, 150, 100, 150};
        assertEquals(new Polygon(points), new Rect(100, 100, 100, 50).toPolygon());
    }
    
    @Test
    public void testRectVsPolygon() {
        int[] points = {0, 0, 800, 0, 800, 600, 0, 600};
        Polygon polygon = new Polygon(points);
        
        assertTrue(polygon.intersects(new Rect(-20, 200, 100, 100)));
        assertTrue(polygon.intersects(new Rect(0, 300, 100, 100)));
        assertTrue(polygon.intersects(new Rect(100, 100, 100, 100)));
        assertFalse(polygon.intersects(new Rect(-200, 0, 100, 100)));
        
        assertTrue(new Rect(-20, 200, 100, 100).intersects(polygon));
        assertTrue(new Rect(0, 300, 100, 100).intersects(polygon));
        assertTrue(new Rect(100, 100, 100, 100).intersects(polygon));
        assertFalse(new Rect(-200, 0, 100, 100).intersects(polygon));
        
        assertTrue(new Polygon(new int[] {11, 3, 59, 3, 59, 67, 11, 67})
                .intersects(new Rect(0, 0, 800, 600)));
    }
}
