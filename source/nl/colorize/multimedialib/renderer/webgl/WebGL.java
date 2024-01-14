//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.webgl;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.math.SegmentedLine;
import nl.colorize.multimedialib.math.Shape;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.RendererException;
import nl.colorize.multimedialib.renderer.teavm.BrowserDOM;
import nl.colorize.multimedialib.renderer.teavm.HtmlCanvasGraphics;
import nl.colorize.multimedialib.renderer.teavm.TeaGraphics;
import nl.colorize.multimedialib.renderer.teavm.TeaImage;
import nl.colorize.multimedialib.renderer.teavm.TeaMediaLoader;
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
 * browsers. For operations that are not supported natively by WebGL, most
 * importantly text rendering, a {@code <canvas>} overlay is used. This does
 * mean that the overlay's contents are always drawn "on top" of the WebGL
 * graphics, but specifically for text rendering this is usually not an issue.
 */
public class WebGL implements TeaGraphics {

    private GraphicsMode graphicsMode;
    private Canvas canvas;
    private BrowserDOM dom;
    private HTMLCanvasElement glCanvas;
    private WebGLRenderingContext gl;
    private HtmlCanvasGraphics overlay;

    private WebGLProgram shaderProgram;
    private int aVertexPosition;
    private int aTextureCoordinates;
    private WebGLUniformLocation uColor;
    private WebGLUniformLocation uPositionVector;
    private WebGLUniformLocation uRotationVector;
    private WebGLUniformLocation uScaleVector;

    private Cache<TeaImage, VertexData> spriteVertexCache;
    private Cache<Shape, VertexData> shapeVertexCache;
    private Cache<TeaImage, WebGLTexture> textureCache;
    private Cache<Mask, WebGLTexture> maskTextureCache;
    private WebGLTexture nullTexture;

    private static final FilePointer VERTEX_SHADER_FILE = new FilePointer("vertex-shader.glsl");
    private static final FilePointer FRAGMENT_SHADER_FILE = new FilePointer("fragment-shader.glsl");

    public WebGL(GraphicsMode graphicsMode, Canvas canvas) {
        this.graphicsMode = graphicsMode;
        this.canvas = canvas;

        if (graphicsMode != GraphicsMode.HEADLESS) {
            dom = new BrowserDOM();
        }
    }

    @Override
    public GraphicsMode getGraphicsMode() {
        return graphicsMode;
    }

    @Override
    public int getDisplayWidth() {
        return glCanvas.getWidth();
    }

    @Override
    public int getDisplayHeight() {
        return glCanvas.getHeight();
    }

    @Override
    public void init(TeaMediaLoader mediaLoader) {
        HTMLDocument document = Window.current().getDocument();
        HTMLElement container = document.getElementById("multimediaLibContainer");
        glCanvas = dom.createFullScreenCanvas(container);
        gl = (WebGLRenderingContext) glCanvas.getContext("webgl");

        String vertexShader = mediaLoader.loadText(VERTEX_SHADER_FILE);
        String fragmentShader = mediaLoader.loadText(FRAGMENT_SHADER_FILE);
        initShaderProgram(vertexShader, fragmentShader);

        spriteVertexCache = Cache.from(this::createSpriteVertexData);
        shapeVertexCache = Cache.from(this::createShapeVertexData);
        textureCache = Cache.from(this::loadTexture);
        maskTextureCache = Cache.from(this::loadMaskTexture);
        nullTexture = loadTexture(dom.createColorCanvas(8, 8, ColorRGB.WHITE));

        initOverlay(mediaLoader);
    }

    private void initShaderProgram(String vertexShader, String fragmentShader) {
        shaderProgram = gl.createProgram();
        compileShader(gl.VERTEX_SHADER, vertexShader);
        compileShader(gl.FRAGMENT_SHADER, fragmentShader);
        gl.linkProgram(shaderProgram);

        if (gl.getProgramParameter(shaderProgram, gl.LINK_STATUS) == null) {
            throw new RendererException("Shader link error: " + gl.getProgramInfoLog(shaderProgram));
        }

        aVertexPosition = gl.getAttribLocation(shaderProgram, "aVertexPosition");
        aTextureCoordinates = gl.getAttribLocation(shaderProgram, "aTextureCoordinates");
        uColor = gl.getUniformLocation(shaderProgram, "uColor");
        uPositionVector = gl.getUniformLocation(shaderProgram, "uPositionVector");
        uRotationVector = gl.getUniformLocation(shaderProgram, "uRotationVector");
        uScaleVector = gl.getUniformLocation(shaderProgram, "uScaleVector");
    }

    private void compileShader(int shaderType, String glsl) {
        WebGLShader shader = gl.createShader(shaderType);
        gl.shaderSource(shader, glsl);
        gl.compileShader(shader);

        if (gl.getShaderParameter(shader, gl.COMPILE_STATUS) == null) {
            gl.deleteShader(shader);
            throw new RendererException("Shader compile error: " + gl.getShaderInfoLog(shader));
        }

        gl.attachShader(shaderProgram, shader);
    }

    private void initOverlay(TeaMediaLoader mediaLoader) {
        overlay = new HtmlCanvasGraphics(canvas);
        overlay.init(mediaLoader);
        overlay.getHtmlCanvas().getStyle().setProperty("position", "absolute");
        overlay.getHtmlCanvas().getStyle().setProperty("left", "0px");
        overlay.getHtmlCanvas().getStyle().setProperty("top", "0px");
        overlay.getHtmlCanvas().getStyle().setProperty("z-index", "2");
    }

    private VertexData createSpriteVertexData(TeaImage image) {
        WebGLBuffer vertexBuffer = gl.createBuffer();
        fillBuffer(vertexBuffer, -0.5f, -0.5f, 0.5f, 0.5f);

        WebGLBuffer textureBuffer = gl.createBuffer();
        fillBuffer(textureBuffer, 0f, 0f, 1f, 1f);

        WebGLTexture texture = textureCache.get(image.forParentImage());

        VertexData vertexData = new VertexData();
        vertexData.setVertices(6);
        vertexData.setVertexBuffer(vertexBuffer);
        vertexData.setTexture(texture);
        vertexData.setTextureCoordinateBuffer(textureBuffer);
        return vertexData;
    }

    private VertexData createShapeVertexData(Shape shape) {
        float[] vertices = toVertices(shape);
        WebGLBuffer vertexBuffer = gl.createBuffer();
        fillBuffer(vertexBuffer, vertices);

        WebGLBuffer textureBuffer = gl.createBuffer();
        fillBuffer(textureBuffer, -1f, -1f, -1f, -1f);

        VertexData vertexData = new VertexData();
        vertexData.setVertices(vertices.length / 2);
        vertexData.setVertexBuffer(vertexBuffer);
        vertexData.setTexture(nullTexture);
        vertexData.setTextureCoordinateBuffer(textureBuffer);
        return vertexData;
    }

    private WebGLTexture loadTexture(TeaImage image) {
        Preconditions.checkState(image.isLoaded(), "Image is still loading");
        Preconditions.checkState(image.isFullImage(), "Cannot create texture from image region");

        HTMLImageElement imageElement = image.getImageElement().get();

        WebGLTexture texture = gl.createTexture();
        gl.bindTexture(gl.TEXTURE_2D, texture);
        gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, imageElement);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.REPEAT);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.REPEAT);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
        gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, 1);
        return texture;
    }

    private WebGLTexture loadTexture(HTMLCanvasElement canvasElement) {
        WebGLTexture texture = gl.createTexture();
        gl.bindTexture(gl.TEXTURE_2D, texture);
        gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, canvasElement);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.REPEAT);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.REPEAT);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
        gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, 1);
        return texture;
    }

    private WebGLTexture loadMaskTexture(Mask mask) {
        HTMLCanvasElement maskCanvas = dom.applyMask(mask.image, mask.maskColor);
        return loadTexture(maskCanvas);
    }

    /**
     * Fills the specified buffer with vertex data for creating one or more
     * 2D polygons. The data in the array is assumed to specify 2D points,
     * in the format {@code [x0, y0, x1, y1, ...]}.
     */
    private void fillBuffer(WebGLBuffer buffer, float[] data) {
        Preconditions.checkArgument(data.length > 0, "Missing 2D vertices");
        Preconditions.checkArgument(data.length % 2 == 0, "Invalid 2D vertices: " + data.length);

        Float32Array wrapper = Float32Array.create(data.length);
        wrapper.set(data);

        gl.bindBuffer(gl.ARRAY_BUFFER, buffer);
        gl.bufferData(gl.ARRAY_BUFFER, wrapper, gl.STATIC_DRAW);
    }

    /**
     * Fills the specified buffer with vertex data for creating a quad that
     * consists of 2 triangles, which in turn consist of 6 vertices. This
     * is a convenience version of{@link #fillBuffer(WebGLBuffer, float[])}.
     */
    private void fillBuffer(WebGLBuffer buffer, float x0, float y0, float x1, float y1) {
        float[] data = {
            x0, y0,
            x1, y0,
            x0, y1,
            x0, y1,
            x1, y0,
            x1, y1
        };

        fillBuffer(buffer, data);
    }

    /**
     * Renders 2D polygon graphics based on the display information specified
     * in the {@link VertexData} instance.
     */
    private void render(VertexData vertexData) {
        gl.bindTexture(gl.TEXTURE_2D, vertexData.getTexture());
        gl.uniform4fv(uColor, vertexData.getColor());
        gl.uniform2fv(uPositionVector, vertexData.getPosition());
        gl.uniform2fv(uRotationVector, vertexData.getRotation());
        gl.uniform2fv(uScaleVector, vertexData.getScale());

        gl.bindBuffer(gl.ARRAY_BUFFER, vertexData.getVertexBuffer());
        gl.enableVertexAttribArray(aVertexPosition);
        gl.vertexAttribPointer(aVertexPosition, 2, gl.FLOAT, false, 0, 0);

        gl.bindBuffer(gl.ARRAY_BUFFER, vertexData.getTextureCoordinateBuffer());
        gl.enableVertexAttribArray(aTextureCoordinates);
        gl.vertexAttribPointer(aTextureCoordinates, 2, gl.FLOAT, false, 0, 0);

        gl.drawArrays(gl.TRIANGLES, 0, vertexData.getVertices());
    }

    @Override
    public void prepareStage(Stage stage) {
        gl.viewport(0, 0, gl.getCanvas().getWidth(), gl.getCanvas().getHeight());
        gl.enable(gl.BLEND);
        gl.blendFunc(gl.ONE, gl.ONE_MINUS_SRC_ALPHA);
        gl.enable(gl.DEPTH_TEST);
        gl.depthFunc(gl.LEQUAL);
        gl.useProgram(shaderProgram);

        overlay.prepareStage(stage);
    }

    @Override
    public void onGraphicAdded(Container parent, Graphic2D graphic) {
    }

    @Override
    public void onGraphicRemoved(Container parent, Graphic2D graphic) {
    }

    @Override
    public boolean visitGraphic(Stage stage, Graphic2D graphic) {
        return stage.isVisible(graphic);
    }

    @Override
    public void drawBackground(ColorRGB color) {
        gl.clearColor(color.r() / 255f, color.g() / 255f, color.b() / 255f, 1f);
        gl.clear(gl.COLOR_BUFFER_BIT);
    }

    @Override
    public void drawSprite(Sprite sprite) {
        TeaImage image = (TeaImage) sprite.getCurrentGraphics();
        HTMLImageElement imageElement = image.getImageElement().orElse(null);

        // Skip drawing this sprite if the underlying image is still loading.
        if (imageElement == null) {
            return;
        }

        VertexData vertexData = spriteVertexCache.get(image);
        Region region = image.getRegion();
        Transform transform = sprite.getGlobalTransform();
        Point2D position = transform.getPosition();
        float zoomLevel = canvas.getZoomLevel();

        WebGLTexture originalTexture = vertexData.getTexture();
        if (transform.getMaskColor() != null) {
            WebGLTexture maskTexture = maskTextureCache.get(new Mask(image, transform.getMaskColor()));
            vertexData.setTexture(maskTexture);
        }

        fillBuffer(vertexData.getVertexBuffer(),
            -image.getWidth() / (float) canvas.getScreenWidth(),
            image.getHeight() / (float) canvas.getScreenHeight(),
            image.getWidth() / (float) canvas.getScreenWidth(),
            -image.getHeight() / (float) canvas.getScreenHeight()
        );

        fillBuffer(vertexData.getTextureCoordinateBuffer(),
            region.x() / (float) imageElement.getWidth(),
            1f - (region.y() / (float) imageElement.getHeight()),
            region.x1() / (float) imageElement.getWidth(),
            1f - (region.y1() / (float) imageElement.getHeight())
        );

        vertexData.setPosition(toGLX(position.getX()), toGLY(position.getY()));
        vertexData.setRotationInRadians(transform.getRotationInRadians());
        vertexData.setScale(transform.getScaleX() * zoomLevel, transform.getScaleY() * zoomLevel);

        render(vertexData);

        vertexData.setTexture(originalTexture);
    }

    @Override
    public void drawLine(Primitive graphic, Line line) {
        //TODO
        overlay.drawLine(graphic, line);
    }

    @Override
    public void drawSegmentedLine(Primitive graphic, SegmentedLine line) {
        //TODO
        overlay.drawSegmentedLine(graphic, line);
    }

    @Override
    public void drawRect(Primitive graphic, Rect rect) {
        VertexData vertexData = shapeVertexCache.get(rect);
        Transform transform = graphic.getGlobalTransform();

        fillBuffer(vertexData.getVertexBuffer(), toVertices(rect));
        vertexData.setColor(graphic.getColor(), transform.getAlpha());
        render(vertexData);
    }

    @Override
    public void drawCircle(Primitive graphic, Circle circle) {
        //TODO
        overlay.drawCircle(graphic, circle);
    }

    @Override
    public void drawPolygon(Primitive graphic, Polygon polygon) {
        //TODO
        overlay.drawPolygon(graphic, polygon);
    }

    @Override
    public void drawText(Text text) {
        overlay.drawText(text);
    }

    private float[] toVertices(Shape shape) {
        if (shape instanceof Rect rect) {
            return toVertices(rect);
        } else {
            throw new RendererException("Cannot extract vertices: " + shape.getClass());
        }
    }

    private float[] toVertices(Rect rect) {
        float x0 = toGLX(rect.getX());
        float y0 = toGLY(rect.getY());
        float x1 = toGLX(rect.getEndX());
        float y1 = toGLY(rect.getEndY());

        return new float[] {
            x0, y0,
            x1, y0,
            x0, y1,
            x0, y1,
            x1, y0,
            x1, y1
        };
    }

    protected float toGLX(float canvasX) {
        return (canvasX / canvas.getWidth()) * 2f - 1f;
    }

    protected float toGLY(float canvasY) {
        return -1f * ((canvasY / canvas.getHeight()) * 2f - 1f);
    }

    /**
     * Combines the mask color with the image it is supposed to mask.
     */
    private record Mask(TeaImage image, ColorRGB maskColor) {
    }
}
