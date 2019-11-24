//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
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
 * Implements a mechanism that divides the application life cycle into a number
 * of separate scenes. One scene is marked as currently active, and will receive
 * frame updates and render graphics for as a long as it is active. Scenes will
 * also receive notifications whenever the active scene changes.
 */
public class SceneManager implements Updatable, Renderable, SceneContext {

    private Renderer renderer;
    private Scene activeScene;
    private Scene requestedScene;

    private Stopwatch fpsTimer;
    private RotatingBuffer fpsBuffer;
    private RotatingBuffer frameTimeBuffer;

    private static final int FPS_MEASUREMENT_BUFFER_SIZE = 100;

    private SceneManager(Renderer renderer) {
        this.renderer = renderer;

        this.fpsTimer = new Stopwatch();
        this.fpsBuffer = new RotatingBuffer(FPS_MEASUREMENT_BUFFER_SIZE);
        this.frameTimeBuffer = new RotatingBuffer(FPS_MEASUREMENT_BUFFER_SIZE);
    }

    /**
     * Requests to change the active scene after the current frame has been
     * completed.
     */
    @Override
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
            activeScene.start(this);
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

    @Override
    public Canvas getCanvas() {
        return renderer.getCanvas();
    }

    @Override
    public InputDevice getInputDevice() {
        return renderer.getInputDevice();
    }

    @Override
    public MediaLoader getMediaLoader() {
        return renderer.getMediaLoader();
    }

    @Override
    public ApplicationData getApplicationData(String appName) {
        return renderer.getApplicationData(appName);
    }

    @Override
    public float getAverageFPS() {
        return 1000f / fpsBuffer.getAverageValue();
    }

    @Override
    public float getAverageFrameTime() {
        return frameTimeBuffer.getAverageValue();
    }

    /**
     * Creates a new {@code SceneManager} and immediately attaches it as a
     * callback to the specified renderer.
     */
    public static SceneManager attach(Renderer renderer) {
        SceneManager sceneManager = new SceneManager(renderer);
        renderer.addUpdateCallback(sceneManager);
        renderer.addRenderCallback(sceneManager);
        return sceneManager;
    }
}
