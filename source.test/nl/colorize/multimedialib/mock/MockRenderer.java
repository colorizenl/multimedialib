//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import nl.colorize.multimedialib.graphics.Audio;
import nl.colorize.multimedialib.graphics.BitmapFont;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.RenderCallback;
import nl.colorize.multimedialib.renderer.RenderContext;
import nl.colorize.multimedialib.renderer.RenderStats;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.util.ResourceFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock implementation of the {@code Renderer} interface.
 */
public class MockRenderer implements Renderer, RenderContext, MediaLoader {

    private ScaleStrategy scaleStrategy;
    private int framerate;
    private Rect screen;
    private List<RenderCallback> callbacks;

    public MockRenderer() {
        this.scaleStrategy = ScaleStrategy.flexible(800, 600);
        this.framerate = 25;
        screen = new Rect(0, 0, 1280, 800);
        callbacks = new ArrayList<>();
    }

    @Override
    public void initialize() {
    }

    @Override
    public void terminate() {
    }

    @Override
    public int getCanvasWidth() {
        return scaleStrategy.getCanvasWidth(screen);
    }

    @Override
    public int getCanvasHeight() {
        return scaleStrategy.getCanvasHeight(screen);
    }

    @Override
    public ScaleStrategy getScaleStrategy() {
        return scaleStrategy;
    }

    @Override
    public int getTargetFramerate() {
        return framerate;
    }

    @Override
    public void drawBackground(ColorRGB backgroundColor) {
    }

    @Override
    public void drawRect(Rect rect, ColorRGB color, Transform transform) {
    }

    @Override
    public void drawImage(Image image, int x, int y, Transform transform) {
    }

    @Override
    public void drawText(String text, BitmapFont font, int x, int y) {
    }

    @Override
    public MediaLoader getMediaLoader() {
        return this;
    }

    @Override
    public Image loadImage(ResourceFile source) {
        return new MockImage();
    }

    @Override
    public Audio loadAudio(ResourceFile source) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RenderStats getStats() {
        return null;
    }

    @Override
    public void registerCallback(RenderCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void unregisterCallback(RenderCallback callback) {
        callbacks.remove(callback);
    }

    public List<RenderCallback> getCallbacks() {
        return callbacks;
    }
}
