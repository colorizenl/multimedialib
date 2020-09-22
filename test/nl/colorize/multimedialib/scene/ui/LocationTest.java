//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.ui;

import nl.colorize.multimedialib.renderer.Canvas;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocationTest {

    private static final float EPSILON = 0.001f;

    @Test
    public void testRight() {
        Canvas canvas = Canvas.zoomOut(800, 600);
        Location location = Location.right(canvas, 300, 100);

        assertEquals(500f, location.getX(), EPSILON);
        assertEquals(100f, location.getY(), EPSILON);
    }

    @Test
    public void testCenter() {
        Canvas canvas = Canvas.zoomOut(800, 600);
        Location location = Location.center(canvas, 100);

        assertEquals(400f, location.getX(), EPSILON);
        assertEquals(100f, location.getY(), EPSILON);
    }

    @Test
    public void testBottom() {
        Canvas canvas = Canvas.zoomOut(800, 600);
        Location location = Location.bottom(canvas, 300, 100);

        assertEquals(300f, location.getX(), EPSILON);
        assertEquals(500f, location.getY(), EPSILON);
    }

    @Test
    public void testRelative() {
        Canvas canvas = Canvas.zoomOut(800, 600);
        Location location = Location.center(canvas, 100);
        Location relativeLocation = location.relativeTo(0f, 200f);

        assertEquals(400f, relativeLocation.getX(), EPSILON);
        assertEquals(300f, relativeLocation.getY(), EPSILON);
    }

    @Test
    public void testRelativeToRight() {
        Canvas canvas = Canvas.zoomOut(800, 600);
        Location location = Location.right(canvas, 100, 100);
        Location relativeLocation = location.relativeTo(50f, 200f);

        assertEquals(750f, relativeLocation.getX(), EPSILON);
        assertEquals(300f, relativeLocation.getY(), EPSILON);
    }
}
