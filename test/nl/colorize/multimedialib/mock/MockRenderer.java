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
import nl.colorize.multimedialib.graphics.PolygonMesh;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.ApplicationData;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.GraphicsContext2D;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.NetworkAccess;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.NetworkConnection;
import nl.colorize.multimedialib.renderer.RenderCallback;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.Stage;
import nl.colorize.util.PlatformFamily;
import nl.colorize.util.Task;
import nl.colorize.util.http.Headers;
import nl.colorize.util.http.PostData;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock implementation of the {@code Renderer} interface plus all nested objects.
 */
public class MockRenderer implements Renderer, GraphicsContext2D, MediaLoader, NetworkAccess {

    private List<RenderCallback> callbacks;
    private Canvas canvas;
    private InputDevice inputDevice;

    public MockRenderer() {
        this.callbacks = new ArrayList<>();
        this.canvas = Canvas.flexible(800, 600);
        this.inputDevice = new MockInputDevice();
    }

    @Override
    public void attach(RenderCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void start() {
    }

    @Override
    public GraphicsMode getSupportedGraphicsMode() {
        return GraphicsMode.HEADLESS;
    }

    @Override
    public Stage getStage() {
        throw new UnsupportedOperationException();
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
    public InputDevice getInputDevice() {
        return inputDevice;
    }

    @Override
    public MediaLoader getMediaLoader() {
        return this;
    }

    @Override
    public ApplicationData getApplicationData(String appName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NetworkAccess getNetwork() {
        return this;
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
    public PolygonMesh loadMesh(FilePointer file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsResourceFile(FilePointer file) {
        return true;
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

    @Override
    public PlatformFamily getPlatform() {
        throw new UnsupportedOperationException();
    }
}
