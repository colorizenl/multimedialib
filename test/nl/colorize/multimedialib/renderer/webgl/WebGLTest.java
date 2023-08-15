//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.webgl;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebGLTest {

    private static final Canvas CANVAS = new Canvas(800, 600, ScaleStrategy.flexible());
    private static final float EPSILON = 0.1f;

    @Test
    void convertCoordinates() {
        WebGL gl = new WebGL(CANVAS);

        assertEquals(-1f, gl.toGLX(0f), EPSILON);
        assertEquals(-0.5f, gl.toGLX(200f), EPSILON);
        assertEquals(0f, gl.toGLX(400f), EPSILON);
        assertEquals(0.5f, gl.toGLX(600f), EPSILON);
        assertEquals(1f, gl.toGLX(800f), EPSILON);

        assertEquals(1f, gl.toGLY(0f), EPSILON);
        assertEquals(0f, gl.toGLY(300f), EPSILON);
        assertEquals(-1f, gl.toGLY(600f), EPSILON);
    }
}
