//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import org.junit.Test;

import static org.junit.Assert.*;

public class CanvasTest {

    @Test
    public void testDefaultZoomLevel() {
        Canvas canvas = new Canvas(800, 600, 1f);
        canvas.resize(1024, 768);

        assertEquals(0, canvas.toScreenX(0));
        assertEquals(512, canvas.toScreenX(512));
        assertEquals(1024, canvas.toScreenX(1024));

        assertEquals(0, canvas.toCanvasX(0));
        assertEquals(512, canvas.toCanvasX(512));
        assertEquals(1024, canvas.toCanvasX(1024));
    }

    @Test
    public void testOffset() {
        Canvas canvas = new Canvas(800, 600, 1f);
        canvas.resize(1024, 768);
        canvas.offset(0, 20);

        assertEquals(20, canvas.toScreenY(0));
        assertEquals(532, canvas.toScreenY(512));
        assertEquals(1044, canvas.toScreenY(1024));

        assertEquals(-20, canvas.toCanvasY(0));
        assertEquals(492, canvas.toCanvasY(512));
        assertEquals(1004, canvas.toCanvasY(1024));
    }

    @Test
    public void testCustomZoomLevel() {
        Canvas canvas = new Canvas(800, 600, 2f);
        canvas.resize(1024, 768);

        assertEquals(0, canvas.toScreenX(0));
        assertEquals(512, canvas.toScreenX(256));
        assertEquals(1024, canvas.toScreenX(512));

        assertEquals(0, canvas.toCanvasX(0));
        assertEquals(256, canvas.toCanvasX(512));
        assertEquals(512, canvas.toCanvasX(1024));
    }
}
