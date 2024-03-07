//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PolygonTest {

    private static final float EPSILON = 0.001f;

    @Test
    public void testPolygonPoints() {
        float[] points = {1, 2, 3, 4, 5, 6, 7, 8};
        Polygon polygon = new Polygon(points);

        assertEquals(4, polygon.getNumPoints());
        assertEquals(8, polygon.points().length);
        assertEquals(1, polygon.getPointX(0), EPSILON);
        assertEquals(2, polygon.getPointY(0), EPSILON);
        assertEquals(5, polygon.getPointX(2), EPSILON);
        assertEquals(6, polygon.getPointY(2), EPSILON);
    }
    
    @Test
    public void testMove() {
        float[] points = {100, 100, 200, 150, 100, 200};
        Polygon polygon = new Polygon(points);
        Polygon moved = polygon.reposition(new Point2D(100, 50));
        assertEquals("[200.0, 150.0, 300.0, 200.0, 200.0, 250.0]", Arrays.toString(moved.points()));
    }

    @Test
    public void testContainsPoint() {
        float[] points = {100, 100, 200, 100, 200, 200, 100, 200};
        Polygon polygon = new Polygon(points);
        
        assertTrue(polygon.contains(new Point2D(150, 150)));
        assertFalse(polygon.contains(new Point2D(250, 150)));
        assertFalse(polygon.contains(new Point2D(200, 300)));
        assertTrue(polygon.contains(new Point2D(100, 100)));
        assertTrue(polygon.contains(new Point2D(200, 200)));
        assertTrue(polygon.contains(new Point2D(100, 150)));
    }

    @Test
    public void testCreateCircle() {
        Polygon circle = Polygon.createCircle(new Point2D(0, 0), 1f, 4);

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

    @Test
    public void testCreateCone() {
        Polygon cone = Polygon.createCone(new Point2D(10, 10), 90, 10, 100);

        assertEquals(4, cone.getNumPoints());
        assertEquals(10f, cone.getPointX(0), Shape.EPSILON);
        assertEquals(10f, cone.getPointY(0), Shape.EPSILON);
        assertEquals(18.715, cone.getPointX(1), Shape.EPSILON);
        assertEquals(109.619, cone.getPointY(1), Shape.EPSILON);
        assertEquals(1.284, cone.getPointX(3), Shape.EPSILON);
        assertEquals(109.619, cone.getPointY(3), Shape.EPSILON);
    }

    @Test
    public void testIntersects() {
        Polygon circle = Polygon.createCircle(new Point2D(0f, 0f), 10f, 16);

        assertTrue(circle.intersects(circle));
        assertTrue(Polygon.createCircle(new Point2D(5f, 0f), 10f, 16).intersects(circle));
        assertTrue(Polygon.createCircle(new Point2D(10f, 0f), 10f, 16).intersects(circle));
        assertFalse(Polygon.createCircle(new Point2D(21f, 0f), 10f, 16).intersects(circle));
    }

    @Test
    void getBounds() {
        Polygon polygon = new Polygon(10, 10, 20, 10, 20, 20);

        assertEquals("(10, 10, 10, 10)", polygon.getBoundingBox().toString());
    }

    @Test
    void getCenter() {
        Polygon polygon = new Polygon(10, 10, 20, 10, 20, 20);

        assertEquals("(15, 15)", polygon.getCenter().toString());
    }

    @Test
    void subdivide() {
        Polygon polygon = new Polygon(160, 140, 240, 140, 270, 200, 240, 260, 160, 260, 130, 200);
        List<Polygon> result = polygon.subdivide();

        assertEquals(6, result.size());
        assertEquals("[160.0, 140.0, 240.0, 140.0, 200.0, 200.0]",
            Arrays.toString(result.get(0).points()));
        assertEquals("[240.0, 140.0, 270.0, 200.0, 200.0, 200.0]",
            Arrays.toString(result.get(1).points()));
        assertEquals("[270.0, 200.0, 240.0, 260.0, 200.0, 200.0]",
            Arrays.toString(result.get(2).points()));
        assertEquals("[240.0, 260.0, 160.0, 260.0, 200.0, 200.0]",
            Arrays.toString(result.get(3).points()));
        assertEquals("[160.0, 260.0, 130.0, 200.0, 200.0, 200.0]",
            Arrays.toString(result.get(4).points()));
        assertEquals("[130.0, 200.0, 160.0, 140.0, 200.0, 200.0]",
            Arrays.toString(result.get(5).points()));
    }

    @Test
    void subdivideRect() {
        Polygon polygon = new Rect(10, 20, 30, 40).toPolygon();
        List<Polygon> result = polygon.subdivide();

        assertEquals(4, result.size());
        assertEquals("[10.0, 20.0, 40.0, 20.0, 25.0, 40.0]", Arrays.toString(result.get(0).points()));
        assertEquals("[40.0, 20.0, 40.0, 60.0, 25.0, 40.0]", Arrays.toString(result.get(1).points()));
        assertEquals("[40.0, 60.0, 10.0, 60.0, 25.0, 40.0]", Arrays.toString(result.get(2).points()));
        assertEquals("[10.0, 60.0, 10.0, 20.0, 25.0, 40.0]", Arrays.toString(result.get(3).points()));
    }

    @Test
    void subdivideTriangle() {
        Polygon polygon = new Polygon(10, 10, 20, 10, 20, 20);
        List<Polygon> result = polygon.subdivide();

        assertEquals(1, result.size());
        assertEquals("[10.0, 10.0, 20.0, 10.0, 20.0, 20.0]", Arrays.toString(result.get(0).points()));
    }
}
