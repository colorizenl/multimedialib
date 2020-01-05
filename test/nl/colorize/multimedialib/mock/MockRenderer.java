//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import nl.colorize.multimedialib.graphics.Align;
import nl.colorize.multimedialib.graphics.AlphaTransform;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.ApplicationData;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.GraphicsContext;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.scene.Renderable;
import nl.colorize.multimedialib.scene.Updatable;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock implementation of the {@code Renderer} interface plus all nested objects.
 */
public class MockRenderer implements Renderer, GraphicsContext, MediaLoader {

    private Canvas canvas;
    private InputDevice inputDevice;
    private List<Updatable> updateCallbacks;
    private List<Renderable> renderCallbacks;

    public MockRenderer() {
        this.canvas = Canvas.flexible(800, 600);
        this.inputDevice = new MockInputDevice();
        this.updateCallbacks = new ArrayList<>();
        this.renderCallbacks = new ArrayList<>();
    }

    @Override
    public Canvas getCanvas() {
        return canvas;
    }

    @Override
    public void drawBackground(ColorRGB backgroundColor) {
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
    public void addUpdateCallback(Updatable callback) {
        updateCallbacks.add(callback);
    }

    @Override
    public void addRenderCallback(Renderable callback) {
        renderCallbacks.add(callback);
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
    public TTFont loadFont(String fontFamily, FilePointer file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String loadText(FilePointer file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsResourceFile(FilePointer file) {
        return true;
    }
}
