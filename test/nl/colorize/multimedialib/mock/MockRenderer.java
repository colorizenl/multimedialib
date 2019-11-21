//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import nl.colorize.multimedialib.graphics.Alignment;
import nl.colorize.multimedialib.graphics.Audio;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.graphics.TrueTypeFont;
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
        this.canvas = new Canvas(800, 600, 1f);
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
    public void drawRect(Rect rect, ColorRGB color, Transform transform) {
    }

    @Override
    public void drawPolygon(Polygon polygon, ColorRGB color, Transform transform) {
    }

    @Override
    public void drawImage(Image image, float x, float y, Transform transform) {
    }

    @Override
    public void drawText(String text, TrueTypeFont font, float x, float y, Alignment align) {
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
    public TrueTypeFont loadFont(String fontFamily, FilePointer file) {
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
