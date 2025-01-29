//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CanvasTest {

    private static final float EPSILON = 0.1f;

    @Test
    public void testFixedCanvas() {
        Canvas canvas = new Canvas(800, 600, ScaleStrategy.flexible());
        canvas.resizeScreen(1024, 768);

        assertEquals(0, canvas.toScreenX(0), EPSILON);
        assertEquals(400, canvas.toScreenX(400), EPSILON);
        assertEquals(800, canvas.toScreenX(800), EPSILON);

        assertEquals(0, canvas.toCanvasX(0), EPSILON);
        assertEquals(512, canvas.toCanvasX(512), EPSILON);
        assertEquals(1024, canvas.toCanvasX(1024), EPSILON);
    }

    @Test
    public void testOffset() {
        Canvas canvas = new Canvas(800, 600, ScaleStrategy.scale());
        canvas.resizeScreen(1024, 768);
        canvas.offsetScreen(0, 20);

        assertEquals(20, canvas.toScreenY(0), EPSILON);
        assertEquals(404, canvas.toScreenY(300), EPSILON);
        assertEquals(788, canvas.toScreenY(600), EPSILON);

        assertEquals(-15.625, canvas.toCanvasY(0), EPSILON);
        assertEquals(284.375, canvas.toCanvasY(384), EPSILON);
        assertEquals(584.375, canvas.toCanvasY(768), EPSILON);
    }

    @Test
    void canvasNeedsToStretch() {
        Canvas canvas = new Canvas(800, 600, ScaleStrategy.scale());
        canvas.resizeScreen(1200, 900);

        assertEquals(0, canvas.toCanvasX(0), EPSILON);
        assertEquals(400, canvas.toCanvasX(600), EPSILON);
        assertEquals(800, canvas.toCanvasX(1200), EPSILON);

        assertEquals(0, canvas.toScreenX(0), EPSILON);
        assertEquals(600, canvas.toScreenX(400), EPSILON);
        assertEquals(1200, canvas.toScreenX(800), EPSILON);
    }

    @Test
    void canvasNeedsToSquash() {
        Canvas canvas = new Canvas(800, 600, ScaleStrategy.scale());
        canvas.resizeScreen(400, 300);

        assertEquals(0, canvas.toCanvasX(0), EPSILON);
        assertEquals(400, canvas.toCanvasX(200), EPSILON);
        assertEquals(800, canvas.toCanvasX(400), EPSILON);

        assertEquals(0, canvas.toScreenX(0), EPSILON);
        assertEquals(200, canvas.toScreenX(400), EPSILON);
        assertEquals(400, canvas.toScreenX(800), EPSILON);
    }

    @Test
    void scaleToDifferentAspectRatio() {
        Canvas canvas = new Canvas(800, 600, ScaleStrategy.scale());
        canvas.resizeScreen(600, 800);

        assertEquals(0f, canvas.toScreenX(0), EPSILON);
        assertEquals(533.3f, canvas.toScreenX(400), EPSILON);
        assertEquals(1066.6, canvas.toScreenX(800), EPSILON);
    }

    @Test
    void fitToDifferentAspectRatio() {
        Canvas canvas = new Canvas(800, 600, ScaleStrategy.fit());
        canvas.resizeScreen(600, 800);

        assertEquals(0f, canvas.toScreenX(0), EPSILON);
        assertEquals(300f, canvas.toScreenX(400), EPSILON);
        assertEquals(600f, canvas.toScreenX(800), EPSILON);
    }

    @Test
    void screenOrientation() {
        Canvas horizontal = new Canvas(800, 600, ScaleStrategy.flexible());
        Canvas vertical = new Canvas(300, 400, ScaleStrategy.flexible());

        assertTrue(horizontal.isLandscape());
        assertFalse(horizontal.isPortait());

        assertFalse(vertical.isLandscape());
        assertTrue(vertical.isPortait());
    }
}
