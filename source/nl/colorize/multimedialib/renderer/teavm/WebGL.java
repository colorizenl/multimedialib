//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.math.SegmentedLine;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.Graphic2D;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.stage.Transform;
import nl.colorize.util.stats.Cache;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLImageElement;
import org.teavm.jso.typedarrays.Float32Array;
import org.teavm.jso.webgl.WebGLBuffer;
import org.teavm.jso.webgl.WebGLProgram;
import org.teavm.jso.webgl.WebGLRenderingContext;
import org.teavm.jso.webgl.WebGLShader;
import org.teavm.jso.webgl.WebGLTexture;
import org.teavm.jso.webgl.WebGLUniformLocation;

/**
 * Graphics using <a href="https://en.wikipedia.org/wiki/WebGL">WebGL</a>.
 * This class is limited to WebGL 1, since that is supported by all modern
 * browsers.
 */
public class WebGL implements TeaGraphics {

    private Canvas canvas;
    private BrowserDOM dom;
    private HTMLCanvasElement htmlCanvas;
    private WebGLRenderingContext gl;

    private WebGLProgram shaderProgram;
    private int aVertexPosition;
    private int aTextureCoordinates;
    private WebGLUniformLocation uRotationVector;
    private WebGLUniformLocation uScaleVector;
    private WebGLUniformLocation uTexture;

    private Cache<TeaImage, MeshData> meshes;
    private Cache<TeaImage, WebGLTexture> textures;

    private static final String VERTEX_SHADER = """
        attribute vec2 aVertexPosition;
        attribute vec2 aTextureCoordinates;
        uniform vec2 uRotationVector;
        uniform vec2 uScaleVector;
        varying vec2 vTextureCoordinates;

        void main() {
            vec2 rotatedPosition = vec2(
                aVertexPosition.x * uRotationVector.y + aVertexPosition.y * uRotationVector.x,
                aVertexPosition.y * uRotationVector.y - aVertexPosition.x * uRotationVector.x
            );

            gl_Position = vec4(rotatedPosition * uScaleVector, 0.0, 1.0);
            vTextureCoordinates = aTextureCoordinates;
        }
        """;

    private static final String FRAGMENT_SHADER = """
        precision mediump float;
        varying vec2 vTextureCoordinates;
        uniform sampler2D uTexture;

        void main() {
            gl_FragColor = texture2D(uTexture, vTextureCoordinates);
            gl_FragColor.rgb *= gl_FragColor.a;
            //gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
        }
        """;

    public WebGL(Canvas canvas) {
        this.canvas = canvas;
        this.dom = new BrowserDOM();
    }

    @Override
    public GraphicsMode getGraphicsMode() {
        return GraphicsMode.MODE_2D;
    }

    @Override
    public int getDisplayWidth() {
        return htmlCanvas.getWidth();
    }

    @Override
    public int getDisplayHeight() {
        return htmlCanvas.getHeight();
    }

    @Override
    public void init() {
        HTMLDocument document = Window.current().getDocument();
        HTMLElement container = document.getElementById("multimediaLibContainer");
        htmlCanvas = dom.createCanvas(container);
        gl = (WebGLRenderingContext) htmlCanvas.getContext("webgl");

        initShaderProgram();

        meshes = Cache.from(this::createImageMeshData);
        textures = Cache.from(this::loadTexture);
    }

    private void initShaderProgram() {
        shaderProgram = gl.createProgram();
        compileShader(gl.VERTEX_SHADER, VERTEX_SHADER);
        compileShader(gl.FRAGMENT_SHADER, FRAGMENT_SHADER);
        gl.linkProgram(shaderProgram);

        if (gl.getProgramParameter(shaderProgram, gl.LINK_STATUS) == null) {
            throw new RuntimeException("Shader link error: " +gl.getProgramInfoLog(shaderProgram));
        }

        aVertexPosition = gl.getAttribLocation(shaderProgram, "aVertexPosition");
        aTextureCoordinates = gl.getAttribLocation(shaderProgram, "aTextureCoordinates");
        uRotationVector = gl.getUniformLocation(shaderProgram, "uRotationVector");
        uScaleVector = gl.getUniformLocation(shaderProgram, "uScaleVector");
        uTexture = gl.getUniformLocation(shaderProgram, "uTexture");
    }

    private void compileShader(int shaderType, String glsl) {
        WebGLShader shader = gl.createShader(shaderType);
        gl.shaderSource(shader, glsl);
        gl.compileShader(shader);

        if (gl.getShaderParameter(shader, gl.COMPILE_STATUS) == null) {
            gl.deleteShader(shader);
            throw new RuntimeException("Shader compile error: " + gl.getShaderInfoLog(shader));
        }

        gl.attachShader(shaderProgram, shader);
    }

    private MeshData createImageMeshData(TeaImage image) {
        WebGLBuffer vertexBuffer = gl.createBuffer();
        bufferData(vertexBuffer, -0.5f, -0.5f, 0.5f, 0.5f);

        WebGLBuffer textureBuffer = gl.createBuffer();
        bufferData(textureBuffer, 0f, 0f, 1f, 1f);

        float[] rotation = {(float) Math.sin(0f), (float) Math.cos(0f)};
        float[] scale = {1f, 1f};

        WebGLTexture texture = textures.get(image.forParentImage());

        return new MeshData(vertexBuffer, textureBuffer, rotation, scale, texture);
    }

    private WebGLTexture loadTexture(TeaImage image) {
        Preconditions.checkState(image.isLoaded(), "Image is still loading");
        Preconditions.checkState(image.isFullImage(), "Cannot create texture from image region");

        HTMLImageElement imageElement = image.getImageElement().get();

        WebGLTexture texture = gl.createTexture();
        gl.bindTexture(gl.TEXTURE_2D, texture);
        gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, imageElement);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
        gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, 1);
        return texture;
    }

    private void bufferData(WebGLBuffer buffer, float x0, float y0, float x1, float y1) {
        float[] data = {
            x0, y0,
            x1, y0,
            x0, y1,
            x0, y1,
            x1, y0,
            x1, y1
        };

        Float32Array wrapper = Float32Array.create(12);
        wrapper.set(data);

        gl.bindBuffer(gl.ARRAY_BUFFER, buffer);
        gl.bufferData(gl.ARRAY_BUFFER, wrapper, gl.STATIC_DRAW);
    }

    private void drawMesh(MeshData meshData) {
        gl.bindTexture(gl.TEXTURE_2D, meshData.texture);
        gl.uniform2fv(uRotationVector, meshData.rotation);
        gl.uniform2fv(uScaleVector, meshData.scale);

        gl.bindBuffer(gl.ARRAY_BUFFER, meshData.vertexBuffer);
        gl.enableVertexAttribArray(aVertexPosition);
        gl.vertexAttribPointer(aVertexPosition, 2, gl.FLOAT, false, 0, 0);

        gl.bindBuffer(gl.ARRAY_BUFFER, meshData.textureBuffer);
        gl.enableVertexAttribArray(aTextureCoordinates);
        gl.vertexAttribPointer(aTextureCoordinates, 2, gl.FLOAT, false, 0, 0);

        gl.drawArrays(gl.TRIANGLES, 0, 6);
    }

    @Override
    public void prepareStage(Stage stage) {
        gl.viewport(0, 0, gl.getCanvas().getWidth(), gl.getCanvas().getHeight());
        gl.enable(gl.BLEND);
        gl.blendFunc(gl.ONE, gl.ONE_MINUS_SRC_ALPHA);
        gl.useProgram(shaderProgram);
    }

    @Override
    public void onGraphicAdded(Container parent, Graphic2D graphic) {
        //TODO
    }

    @Override
    public void onGraphicRemoved(Container parent, Graphic2D graphic) {
        //TODO
    }

    @Override
    public boolean visitGraphic(Graphic2D graphic) {
        //TODO
        return true;
    }

    @Override
    public void drawBackground(ColorRGB color) {
        gl.clearColor(color.r() / 255f, color.g() / 255f, color.b() / 255f, 1f);
        gl.clear(gl.COLOR_BUFFER_BIT);
    }

    @Override
    public void drawSprite(Sprite sprite) {
        TeaImage image = (TeaImage) sprite.getCurrentGraphics();
        HTMLImageElement imageElement = image.getImagePromise().getValue().orElse(null);

        // Skip drawing this sprite if the underlying image is still loading.
        if (imageElement == null) {
            return;
        }

        MeshData meshData = meshes.get(image);
        Region region = image.getRegion();
        Transform transform = sprite.getGlobalTransform();

        bufferData(meshData.vertexBuffer,
            toGLX(transform.getPosition().getX() - image.getWidth() / 2f),
            toGLY(transform.getPosition().getY() - image.getHeight() / 2f),
            toGLX(transform.getPosition().getX() + image.getWidth() / 2f),
            toGLY(transform.getPosition().getY() + image.getHeight() / 2f)
        );

        bufferData(meshData.textureBuffer,
            region.x() / (float) imageElement.getWidth(),
            1f - (region.y1() / (float) imageElement.getHeight()),
            region.x1() / (float) imageElement.getWidth(),
            1f - (region.y() / (float) imageElement.getHeight())
        );

        drawMesh(meshData);
    }

    @Override
    public void drawLine(Primitive graphic, Line line) {
        //TODO
    }

    @Override
    public void drawSegmentedLine(Primitive graphic, SegmentedLine line) {
        //TODO
    }

    @Override
    public void drawRect(Primitive graphic, Rect rect) {
        //TODO
    }

    @Override
    public void drawCircle(Primitive graphic, Circle circle) {
        //TODO
    }

    @Override
    public void drawPolygon(Primitive graphic, Polygon polygon) {
        //TODO
    }

    @Override
    public void drawText(Text text) {
        //TODO
    }

    protected float toGLX(float canvasX) {
        return (canvasX / canvas.getWidth()) * 2f - 1f;
    }

    protected float toGLY(float canvasY) {
        return (canvasY / canvas.getHeight()) * 2f - 1f;
    }

    /**
     * WebGL buffer data for a mesh. For 2D graphics, all meshes are basically
     * quads that consist of 6 vertices each.
     */
    private record MeshData(
        WebGLBuffer vertexBuffer,
        WebGLBuffer textureBuffer,
        float[] rotation,
        float[] scale,
        WebGLTexture texture
    ) {

        public MeshData {
            Preconditions.checkArgument(rotation.length == 2, "Invalid rotation buffer");
            Preconditions.checkArgument(scale.length == 2, "Invalid scale buffer");
        }
    }
}
