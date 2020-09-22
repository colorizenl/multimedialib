//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CanvasTest {

    private static final float EPSILON = 0.001f;

    @Test
    public void testFixedCanvas() {
        Canvas canvas = Canvas.zoomOut(800, 600);
        canvas.resizeScreen(1024, 768);

        assertEquals(0, canvas.toScreenX(0), EPSILON);
        assertEquals(512, canvas.toScreenX(400), EPSILON);
        assertEquals(1024, canvas.toScreenX(800), EPSILON);

        assertEquals(0, canvas.toCanvasX(0), EPSILON);
        assertEquals(400, canvas.toCanvasX(512), EPSILON);
        assertEquals(800, canvas.toCanvasX(1024), EPSILON);
    }

    @Test
    public void testDifferentAspectRatioHorizontal() {
        Canvas canvas = Canvas.zoomOut(800, 600);
        canvas.resizeScreen(1000, 600);

        assertEquals(0, canvas.toScreenX(0), EPSILON);
        assertEquals(400, canvas.toScreenX(400), EPSILON);
        assertEquals(800, canvas.toScreenX(800), EPSILON);
        assertEquals(0, canvas.toScreenY(0), EPSILON);
        assertEquals(300, canvas.toScreenY(300), EPSILON);
        assertEquals(600, canvas.toScreenY(600), EPSILON);

        assertEquals(0, canvas.toCanvasX(0), EPSILON);
        assertEquals(500, canvas.toCanvasX(500), EPSILON);
        assertEquals(1000, canvas.toCanvasX(1000), EPSILON);
        assertEquals(0, canvas.toCanvasY(0), EPSILON);
        assertEquals(300, canvas.toCanvasY(300), EPSILON);
        assertEquals(600, canvas.toCanvasY(600), EPSILON);
    }

    @Test
    public void testDifferentAspectRatioVertical() {
        Canvas canvas = Canvas.zoomOut(800, 600);
        canvas.resizeScreen(800, 1000);

        assertEquals(0, canvas.toScreenY(0), EPSILON);
        assertEquals(400, canvas.toScreenY(400), EPSILON);
        assertEquals(800, canvas.toScreenY(800), EPSILON);
        assertEquals(0, canvas.toScreenY(0), EPSILON);
        assertEquals(300, canvas.toScreenY(300), EPSILON);
        assertEquals(600, canvas.toScreenY(600), EPSILON);

        assertEquals(0, canvas.toCanvasX(0), EPSILON);
        assertEquals(400, canvas.toCanvasX(400), EPSILON);
        assertEquals(800, canvas.toCanvasX(800), EPSILON);
        assertEquals(0, canvas.toCanvasY(0), EPSILON);
        assertEquals(500, canvas.toCanvasY(500), EPSILON);
        assertEquals(1000, canvas.toCanvasY(1000), EPSILON);
    }

    @Test
    public void testOffset() {
        Canvas canvas = Canvas.zoomOut(800, 600);
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
    public void testFlexibleCanvas() {
        Canvas canvas = Canvas.flexible(800, 600);
        canvas.resizeScreen(1280, 800);

        assertEquals(0, canvas.toScreenY(0), EPSILON);
        assertEquals(512, canvas.toScreenY(512), EPSILON);
        assertEquals(1024, canvas.toScreenY(1024), EPSILON);

        assertEquals(0, canvas.toCanvasY(0), EPSILON);
        assertEquals(512, canvas.toCanvasY(512), EPSILON);
        assertEquals(1024, canvas.toCanvasY(1024), EPSILON);
    }

    @Test
    public void testFlexibleCanvasWithOffset() {
        Canvas canvas = Canvas.zoomOut(800, 600);
        canvas.resizeScreen(1280, 800);
        canvas.offsetScreen(0, 20);

        assertEquals(20, canvas.toScreenY(0), EPSILON);
        assertEquals(702.666f, canvas.toScreenY(512), EPSILON);
        assertEquals(1385.333f, canvas.toScreenY(1024), EPSILON);

        assertEquals(-15, canvas.toCanvasY(0), EPSILON);
        assertEquals(369, canvas.toCanvasY(512), EPSILON);
        assertEquals(753, canvas.toCanvasY(1024), EPSILON);
    }

    @Test
    void zoomIn() {
        Canvas canvas = Canvas.zoomIn(800, 600);
        canvas.resizeScreen(500, 1000);

        assertEquals(0f, canvas.toCanvasX(0), EPSILON);
        assertEquals(300f, canvas.toCanvasX(500), EPSILON);
        assertEquals(0f, canvas.toCanvasY(0), EPSILON);
        assertEquals(600f, canvas.toCanvasY(1000), EPSILON);
    }

    @Test
    void zoomOut() {
        Canvas canvas = Canvas.zoomOut(800, 600);
        canvas.resizeScreen(500, 1000);

        assertEquals(0f, canvas.toCanvasX(0), EPSILON);
        assertEquals(800f, canvas.toCanvasX(500), EPSILON);
        assertEquals(0f, canvas.toCanvasY(0), EPSILON);
        assertEquals(1600f, canvas.toCanvasY(1000), EPSILON);
    }

    @Test
    void zoomBalanced() {
        Canvas canvas = Canvas.zoomBalanced(800, 600);
        canvas.resizeScreen(500, 1000);

        assertEquals(0f, canvas.toCanvasX(0), EPSILON);
        assertEquals(436.363f, canvas.toCanvasX(500), EPSILON);
        assertEquals(0f, canvas.toCanvasY(0), EPSILON);
        assertEquals(872.727f, canvas.toCanvasY(1000), EPSILON);
    }
}
