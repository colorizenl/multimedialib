//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.webgl;

import lombok.Data;
import nl.colorize.multimedialib.stage.ColorRGB;
import org.teavm.jso.webgl.WebGLBuffer;
import org.teavm.jso.webgl.WebGLTexture;

/**
 * Data structure that collects all properties necessary for rendering
 * 2D polygon graphics using WebGL. Internally, this class stores all
 * properties in the "native" format expected by WebGL. For example,
 * colors are represented using an {@code [r, g, b, a]} array rather
 * than a {@link ColorRGB} instance. However, convenience methods are
 * provided to convert other property representations into this format.
 * <p>
 * Note this class is purely a data structure and does not perform any
 * WebGL operations itself (since the WebGL context is effectively global
 * and therefore benefits from central control by the renderer).
 */
@Data
public class VertexData {

    private int vertices;
    private WebGLBuffer vertexBuffer;
    private WebGLTexture texture;
    private WebGLBuffer textureCoordinateBuffer;
    private float[] color;
    private float[] position;
    private float[] rotation;
    private float[] scale;

    public VertexData() {
        this.color = new float[] {1f, 1f, 1f, 1f};
        this.position = new float[] {0f, 0f};
        this.rotation = new float[] {(float) Math.sin(0f), (float) Math.cos(0f)};
        this.scale = new float[] {1f, 1f};
    }

    public void setPosition(float x, float y) {
        position[0] = x;
        position[1] = y;
    }

    public void setColor(ColorRGB rgb, float alpha) {
        color[0] = rgb.r() / 255f;
        color[1] = rgb.g() / 255f;
        color[2] = rgb.b() / 255f;
        color[3] = alpha / 100f;
    }

    public void setRotationInRadians(float radians) {
        rotation[0] = (float) Math.sin(radians);
        rotation[1] = (float) Math.cos(radians);
    }

    public void setScale(float scaleX, float scaleY) {
        scale[0] = scaleX / 100f;
        scale[1] = scaleY / 100f;
    }
}
