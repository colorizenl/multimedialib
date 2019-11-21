//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.graphics.Alignment;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.graphics.TrueTypeFont;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.ApplicationData;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsContext;
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
public class TeaRenderer implements Renderer, GraphicsContext, AnimationFrameCallback {

    private Canvas canvas;
    private TeaInputDevice inputDevice;
    private TeaMediaLoader mediaLoader;
    private TeaLocalStorage localStorage;
    private List<Updatable> updateCallbacks;
    private List<Renderable> renderCallbacks;

    private static final int HTML_CANVAS_ANIMATION_FRAMERATE = 60;
    private static final Transform NULL_TRANSFORM = new Transform();

    public TeaRenderer(Canvas canvas) {
        this.canvas = canvas;
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
    public void drawBackground(ColorRGB backgroundColor) {
        drawRect(new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), backgroundColor, null);
    }

    @Override
    public void drawRect(Rect rect, ColorRGB color, Transform transform) {
        if (transform == null) {
            transform = NULL_TRANSFORM;
        }

        float x = canvas.toScreenX(rect.getX());
        float y = canvas.toScreenX(rect.getY());
        float width = canvas.toScreenX(rect.getWidth());
        float height = canvas.toScreenX(rect.getHeight());

        Browser.drawRect(x, y, width, height, color.toHex(), transform.getAlpha() / 100f);
    }

    @Override
    public void drawPolygon(Polygon polygon, ColorRGB color, Transform transform) {
        if (transform == null) {
            transform = NULL_TRANSFORM;
        }

        float[] points = new float[polygon.getPoints().length];

        for (int i = 0; i < polygon.getPoints().length; i += 2) {
            points[i] = canvas.toScreenX(polygon.getPoints()[i]);
            points[i + 1] = canvas.toScreenY(polygon.getPoints()[i + 1]);
        }

        Browser.drawPolygon(points, color.toHex(), transform.getAlpha() / 100f);
    }

    @Override
    public void drawImage(Image image, float x, float y, Transform transform) {
        if (transform == null) {
            transform = NULL_TRANSFORM;
        }

        TeaImage pointer = (TeaImage) image;
        float canvasX = canvas.toScreenX(x);
        float canvasY = canvas.toScreenY(y);

        if (pointer.getRegion() == null) {
            float width = image.getWidth() * canvas.getZoomLevel();
            float height = image.getHeight() * canvas.getZoomLevel();

            Browser.drawImage(pointer.getId(), canvasX - width / 2f, canvasY - height / 2f,
                width, height, transform.getAlpha() / 100f, getMask(transform));
        } else {
            Rect region = pointer.getRegion();
            float width = region.getWidth() * canvas.getZoomLevel();
            float height = region.getHeight() * canvas.getZoomLevel();

            Browser.drawImageRegion(pointer.getId(), region.getX(), region.getY(),
                region.getWidth(), region.getHeight(),
                canvasX, canvasY, width, height, transform.getAlpha() / 100f, getMask(transform));
        }
    }

    @Override
    public void drawText(String text, TrueTypeFont font, float x, float y, Alignment align) {
        float canvasX = canvas.toScreenX(x);
        float canvasY = canvas.toScreenY(y);

        Browser.drawText(text, font.getFamily(), font.getSize(), font.getColor().toHex(),
            canvasX, canvasY, align.toString().toLowerCase());
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
            renderCallback.render(this);
        }
    }

    private void updateCanvas() {
        int canvasWidth = Math.round(Browser.getCanvasWidth());
        int canvasHeight = Math.round(Browser.getCanvasHeight());

        if (canvasWidth > 0 && canvasHeight > 0) {
            canvas.resize(canvasWidth, canvasHeight);
        }
    }

    private String getMask(Transform transform) {
        if (transform == null || transform.getMask() == null) {
            return null;
        }
        return transform.getMask().toHex();
    }
}
