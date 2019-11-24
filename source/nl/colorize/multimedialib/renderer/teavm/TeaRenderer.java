//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.renderer.ApplicationData;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.scene.Renderable;
import nl.colorize.multimedialib.scene.Updatable;

import java.util.ArrayList;
import java.util.List;

/**
 * Renderer based on TeaVM (http://teavm.org) that is transpiled to JavaScript
 * and runs in the browser.
 */
public class TeaRenderer implements Renderer, AnimationFrameCallback {

    private Canvas canvas;
    private TeaGraphicsContext graphics;
    private TeaInputDevice inputDevice;
    private TeaMediaLoader mediaLoader;
    private TeaLocalStorage localStorage;
    private List<Updatable> updateCallbacks;
    private List<Renderable> renderCallbacks;

    private static final int HTML_CANVAS_ANIMATION_FRAMERATE = 60;

    public TeaRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.graphics = new TeaGraphicsContext(canvas);
        this.inputDevice = new TeaInputDevice();
        this.mediaLoader = new TeaMediaLoader();
        this.localStorage = new TeaLocalStorage();
        this.updateCallbacks = new ArrayList<>();
        this.renderCallbacks = new ArrayList<>();

        Browser.renderFrame(this);
    }

    @Override
    public Canvas getCanvas() {
        return canvas;
    }

    @Override
    public InputDevice getInputDevice() {
        return inputDevice;
    }

    @Override
    public MediaLoader getMediaLoader() {
        return mediaLoader;
    }

    @Override
    public ApplicationData getApplicationData(String appName) {
        return localStorage;
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
    public void onRenderFrame() {
        float deltaTime = 1f / HTML_CANVAS_ANIMATION_FRAMERATE;

        updateCanvas();
        inputDevice.update(deltaTime);

        for (Updatable updateCallback : updateCallbacks) {
            updateCallback.update(deltaTime);
        }

        for (Renderable renderCallback : renderCallbacks) {
            renderCallback.render(graphics);
        }
    }

    private void updateCanvas() {
        int canvasWidth = Math.round(Browser.getCanvasWidth());
        int canvasHeight = Math.round(Browser.getCanvasHeight());

        if (canvasWidth > 0 && canvasHeight > 0) {
            canvas.resize(canvasWidth, canvasHeight);
        }
    }
}
