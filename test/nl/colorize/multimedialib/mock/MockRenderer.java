//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.PolygonModel;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.NetworkAccess;
import nl.colorize.multimedialib.renderer.NetworkConnection;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.util.Configuration;
import nl.colorize.util.Callback;
import nl.colorize.util.http.Headers;
import nl.colorize.util.http.PostData;

/**
 * Mock implementation of the {@code Renderer} interface plus all nested objects.
 */
public class MockRenderer implements Renderer, MediaLoader, NetworkAccess {

    private DisplayMode displayMode;
    private SceneContext context;

    public MockRenderer() {
        this.displayMode = new DisplayMode(Canvas.flexible(800, 600), 60);
        this.context = new SceneContext(displayMode, new MockInputDevice(), this, this);
    }

    @Override
    public void start(Scene initialScene) {
        context.changeScene(initialScene);
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
    public Configuration loadApplicationData(String appName, String fileName) {
        return Configuration.fromProperties();
    }

    @Override
    public void saveApplicationData(Configuration data, String appName, String fileName) {
        // Do nothing
    }

    @Override
    public void get(String url, Headers headers, Callback<String> callback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void post(String url, Headers headers, PostData body, Callback<String> callback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NetworkConnection connectWebSocket(String uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NetworkConnection connectWebRTC(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    @Override
    public String takeScreenshot() {
        throw new UnsupportedOperationException();
    }
}
