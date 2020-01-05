//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.RotatingBuffer;
import nl.colorize.multimedialib.renderer.ApplicationData;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsContext;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.util.Stopwatch;

/**
 * Implements a mechanism on top of the renderer that divides the application
 * life cycle into a number of separate scenes. One scene is marked as currently
 * active, and will receive frame updates and render graphics for as a long as
 * it is active. Scenes will also receive notifications whenever the active scene
 * changes.
 */
public class Application implements Updatable, Renderable {

    private Renderer renderer;
    private MediaCache mediaCache;

    private Scene activeScene;
    private Scene requestedScene;

    private Stopwatch fpsTimer;
    private RotatingBuffer fpsBuffer;
    private RotatingBuffer frameTimeBuffer;

    private static final int FPS_MEASUREMENT_BUFFER_SIZE = 100;

    public Application(Renderer renderer) {
        this.renderer = renderer;
        this.mediaCache = new MediaCache(renderer.getMediaLoader());

        this.fpsTimer = new Stopwatch();
        this.fpsBuffer = new RotatingBuffer(FPS_MEASUREMENT_BUFFER_SIZE);
        this.frameTimeBuffer = new RotatingBuffer(FPS_MEASUREMENT_BUFFER_SIZE);

        renderer.addUpdateCallback(this);
        renderer.addRenderCallback(this);
    }

    /**
     * Requests to change the active scene after the current frame has been
     * completed.
     * @throws IllegalStateException if a different scene has already been
     *         requested, but that scene has not yet started.
     */
    public void changeScene(Scene requestedScene) {
        Preconditions.checkState(this.requestedScene == null,
            "Another scene has already been requested: " + requestedScene);

        this.requestedScene = requestedScene;
    }

    public Scene getActiveScene() {
        return activeScene;
    }

    @Override
    public void update(float deltaTime) {
        if (requestedScene != null) {
            activeScene = requestedScene;
            requestedScene = null;
            activeScene.start();
        }

        Stopwatch frameTimer = new Stopwatch();
        frameTimer.tick();

        if (activeScene != null) {
            activeScene.update(deltaTime);
        }

        long actualFrameTime = frameTimer.tick();
        frameTimeBuffer.add(actualFrameTime);
    }

    @Override
    public void render(GraphicsContext graphics) {
        if (activeScene != null) {
            long fpsValue = fpsTimer.tick();
            fpsBuffer.add(fpsValue);

            activeScene.render(graphics);
        }
    }

    /**
     * Returns the renderer that is used by this application.
     * @deprecated The purpose of this class is to wrap the renderer, so scenes
     *             should not access it directly. When needing to access one of
     *             the renderer's fields, use one of the more specific methods
     *             provided by this class instead.
     */
    @Deprecated
    public Renderer getRenderer() {
        return renderer;
    }

    public Canvas getCanvas() {
        return renderer.getCanvas();
    }

    public InputDevice getInputDevice() {
        return renderer.getInputDevice();
    }

    public MediaLoader getMediaLoader() {
        return mediaCache;
    }

    public ApplicationData getApplicationData(String appName) {
        return renderer.getApplicationData(appName);
    }

    public float getAverageFPS() {
        return 1000f / fpsBuffer.getAverageValue();
    }

    public float getAverageFrameTime() {
        return frameTimeBuffer.getAverageValue();
    }
}
