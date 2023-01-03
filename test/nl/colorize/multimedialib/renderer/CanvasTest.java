//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CanvasTest {

    private static final float EPSILON = 0.1f;

    @Test
    public void testFixedCanvas() {
        Canvas canvas = Canvas.forNative(800, 600);
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
        Canvas canvas = Canvas.forSize(800, 600);
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
        Canvas canvas = Canvas.forSize(800, 600);
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
        Canvas canvas = Canvas.forSize(800, 600);
        canvas.resizeScreen(400, 300);

        assertEquals(0, canvas.toCanvasX(0), EPSILON);
        assertEquals(400, canvas.toCanvasX(200), EPSILON);
        assertEquals(800, canvas.toCanvasX(400), EPSILON);

        assertEquals(0, canvas.toScreenX(0), EPSILON);
        assertEquals(200, canvas.toScreenX(400), EPSILON);
        assertEquals(400, canvas.toScreenX(800), EPSILON);
    }
}
