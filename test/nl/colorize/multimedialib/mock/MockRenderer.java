//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import nl.colorize.multimedialib.graphics.Align;
import nl.colorize.multimedialib.graphics.AlphaTransform;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.PolygonModel;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.GeometryBuilder;
import nl.colorize.multimedialib.renderer.GraphicsContext2D;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.NetworkAccess;
import nl.colorize.multimedialib.renderer.NetworkConnection;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.util.ApplicationData;
import nl.colorize.util.Task;
import nl.colorize.util.http.Headers;
import nl.colorize.util.http.PostData;

/**
 * Mock implementation of the {@code Renderer} interface plus all nested objects.
 */
public class MockRenderer implements Renderer, GraphicsContext2D, MediaLoader, NetworkAccess {

    private Canvas canvas;
    private SceneContext context;

    public MockRenderer() {
        this.canvas = Canvas.flexible(800, 600);
        this.context = new SceneContext(canvas, new MockInputDevice(), this, this);
    }

    @Override
    public void start(Scene initialScene) {
        context.changeScene(initialScene);
    }

    @Override
    public GraphicsMode getGraphicsMode() {
        return GraphicsMode.HEADLESS;
    }

    @Override
    public Canvas getCanvas() {
        return canvas;
    }

    @Override
    public void drawBackground(ColorRGB backgroundColor) {
    }

    @Override
    public void drawLine(Point2D from, Point2D to, ColorRGB color, float thickness) {
    }

    @Override
    public void drawRect(Rect rect, ColorRGB color, AlphaTransform alpha) {
    }

    @Override
    public void drawCircle(Circle circle, ColorRGB color, AlphaTransform alpha) {
    }

    @Override
    public void drawPolygon(Polygon polygon, ColorRGB color, AlphaTransform alpha) {
    }

    @Override
    public void drawImage(Image image, float x, float y, Transform transform) {
    }

    @Override
    public void drawText(String text, TTFont font, float x, float y, Align align, AlphaTransform alpha) {
    }

    @Override
    public Image loadImage(FilePointer source) {
        return new MockImage();
    }

    @Override
    public Audio loadAudio(FilePointer source) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TTFont loadFont(FilePointer file, String family, int size, ColorRGB color, boolean bold) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String loadText(FilePointer file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PolygonModel loadModel(FilePointer file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsResourceFile(FilePointer file) {
        return true;
    }

    @Override
    public ApplicationData loadApplicationData(String appName, String fileName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveApplicationData(ApplicationData data, String appName, String fileName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GeometryBuilder getGeometryBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Task<String> get(String url, Headers headers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Task<String> post(String url, Headers headers, PostData body) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWebSocketSupported() {
        return false;
    }

    @Override
    public NetworkConnection connectWebSocket(String uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWebRtcSupported() {
        return false;
    }

    @Override
    public NetworkConnection connectWebRTC(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String takeScreenshot() {
        throw new UnsupportedOperationException();
    }
}
