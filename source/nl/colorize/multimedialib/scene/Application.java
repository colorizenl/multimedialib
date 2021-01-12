//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.math.RotatingBuffer;
import nl.colorize.multimedialib.renderer.ApplicationData;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.GraphicsContext2D;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.NetworkAccess;
import nl.colorize.multimedialib.renderer.RenderCallback;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.Stage;
import nl.colorize.multimedialib.scene.effect.Effect;
import nl.colorize.util.PlatformFamily;
import nl.colorize.util.Stopwatch;
import nl.colorize.util.animation.Interpolation;
import nl.colorize.util.animation.Timeline;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a standard application structure that divides the application into
 * a number of scenes. One scene is marked as currently active, and will receive
 * frame updates and render graphics for as a long as it is active. Any number
 * of sub-scenes can be attached to the active scene, which then receive frame
 * updates from the application. Once the active "parent" scene ends, all
 * attached sub-scenes are automatically stopped.
 */
public final class Application implements RenderCallback {

    private Renderer renderer;
    private MediaManager media;
    private Scene activeScene;
    private List<SubScene> activeSubScenes;
    private Scene requestedScene;
    private List<SubScene> requestedSubScenes;
    private List<SubScene> completedSubScenes;

    private Stopwatch fpsTimer;
    private RotatingBuffer fpsBuffer;
    private RotatingBuffer frameTimeBuffer;
    private RotatingBuffer upsBuffer;

    private boolean orientationLock;
    private Effect orientationLockAnim;

    private static final int FPS_MEASUREMENT_BUFFER_SIZE = 100;
    private static final FilePointer ORIENTATION_LOCK_IMAGE = new FilePointer("orientation-lock.png");

    private Application() {
        this.activeScene = null;
        this.activeSubScenes = new ArrayList<>();
        this.requestedScene = null;
        this.requestedSubScenes = new ArrayList<>();
        this.completedSubScenes = new ArrayList<>();

        this.fpsTimer = new Stopwatch();
        this.fpsBuffer = new RotatingBuffer(FPS_MEASUREMENT_BUFFER_SIZE, 1000f);
        this.frameTimeBuffer = new RotatingBuffer(FPS_MEASUREMENT_BUFFER_SIZE);
        this.upsBuffer = new RotatingBuffer(FPS_MEASUREMENT_BUFFER_SIZE, 1000f);
    }

    private void init() {
        media = new MediaManager(renderer.getMediaLoader());

        orientationLock = false;
        orientationLockAnim = initOrientationLockAnim();
    }

    private Effect initOrientationLockAnim() {
        Image orientationLockImage = getMediaLoader().loadImage(ORIENTATION_LOCK_IMAGE);

        Timeline timeline = new Timeline(Interpolation.EASE, true);
        timeline.addKeyFrame(0f, 100f);
        timeline.addKeyFrame(1f, 110f);
        timeline.addKeyFrame(2f, 100f);

        Effect effect = Effect.forImage(orientationLockImage, timeline);
        effect.modify(value -> effect.getTransform().setScale(Math.round(value)));
        return effect;
    }

    @Override
    public void update(Renderer renderer, float deltaTime) {
        if (this.renderer == null) {
            this.renderer = renderer;
            init();
        }

        Stopwatch frameTimer = new Stopwatch();
        frameTimer.tick();

        updateFrame(deltaTime);

        long actualFrameTime = frameTimer.tick();
        frameTimeBuffer.add(actualFrameTime);
    }

    private void updateFrame(float deltaTime) {
        upsBuffer.add(deltaTime);
        
        if (requestedScene != null) {
            activateRequestedScene();
        }

        activeSubScenes.addAll(requestedSubScenes);
        requestedSubScenes.clear();

        activeSubScenes.removeAll(completedSubScenes);
        completedSubScenes.clear();

        if (activeScene != null && isScreenOrientationSupported()) {
            updateActiveScene(deltaTime);
        } else {
            orientationLockAnim.update(deltaTime);
        }
    }

    private void updateActiveScene(float deltaTime) {
        activeScene.update(this, deltaTime);

        for (SubScene subScene : activeSubScenes) {
            subScene.update(deltaTime);

            if (subScene.isCompleted()) {
                completedSubScenes.add(subScene);
            }
        }
    }

    private void activateRequestedScene() {
        if (activeScene != null) {
            activeScene.end(this);
            activeSubScenes.clear();
            completedSubScenes.clear();
        }

        if (renderer.getSupportedGraphicsMode() == GraphicsMode.ALL) {
            Stage stage = renderer.getStage();
            stage.clear();
        }

        activeScene = requestedScene;
        requestedScene = null;
        activeScene.start(this);
    }

    @Override
    public void render(Renderer renderer, GraphicsContext2D graphics) {
        long fpsValue = fpsTimer.tick();
        fpsBuffer.add(fpsValue);

        if (!isScreenOrientationSupported()) {
            drawOrientationLock(graphics);
            return;
        }

        if (activeScene != null) {
            renderActiveScene(graphics);
        }
    }

    private void renderActiveScene(GraphicsContext2D graphics) {
        for (SubScene subScene : activeSubScenes) {
            if (subScene.hasBackgroundGraphics()) {
                subScene.render(graphics);
            }
        }

        activeScene.render(this, graphics);

        for (SubScene subScene : activeSubScenes) {
            if (!subScene.hasBackgroundGraphics()) {
                subScene.render(graphics);
            }
        }
    }

    /**
     * Requests to change the active scene after the current frame has been
     * completed.
     *
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

    /**
     * Attaches a sub-scene to the currently active scene. The sub-scene will
     * play for as long as its parent scene. If the scene is changed, all
     * attached sub-scenes will be stopped.
     */
    public void attach(SubScene subScene) {
        requestedSubScenes.add(subScene);
    }

    public void detach(SubScene subScene) {
        requestedSubScenes.remove(subScene);
        activeSubScenes.remove(subScene);
    }

    public List<SubScene> getActiveSubScenes() {
        return ImmutableList.copyOf(activeSubScenes);
    }

    /**
     * Restricts the application to landscape orientation. When attempting to
     * use the application in portrait application, the current scene will be
     * suspended and an image will be shown prompting to switch back to
     * landscape orientation.
     */
    public void lockScreenOrientation() {
        orientationLock = true;
    }

    private boolean isScreenOrientationSupported() {
        if (!orientationLock) {
            return true;
        }

        Canvas canvas = renderer.getCanvas();
        return canvas.getWidth() > 0 && canvas.getWidth() > canvas.getHeight();
    }

    private void drawOrientationLock(GraphicsContext2D graphics) {
        Canvas canvas = getCanvas();
        orientationLockAnim.setPosition(canvas.getWidth() / 2f, canvas.getHeight() / 2f);
        orientationLockAnim.render(graphics);
    }

    /**
     * Returns the renderer that is used by this application.
     * @deprecated There should be no need for scenes to interact with the
     *             renderer directly.
     */
    @Deprecated
    public Renderer getRenderer() {
        return renderer;
    }

    public Canvas getCanvas() {
        return renderer.getCanvas();
    }

    public Stage getStage() {
        return renderer.getStage();
    }

    public InputDevice getInputDevice() {
        return renderer.getInputDevice();
    }

    public MediaLoader getMediaLoader() {
        return renderer.getMediaLoader();
    }

    public MediaManager getMedia() {
        return media;
    }

    public ApplicationData getApplicationData(String appName) {
        return renderer.getApplicationData(appName);
    }

    public NetworkAccess getNetwork() {
        return renderer.getNetwork();
    }

    public PlatformFamily getPlatform() {
        return renderer.getPlatform();
    }

    /**
     * Returns the distribution channel that was used to obtain the application.
     * Examples of return values are "App Store", "Download", and "Web".
     */
    public String getDistributionChannel() {
        PlatformFamily platform = getPlatform();

        switch (platform) {
            case IOS : return "App Store";
            case ANDROID : return "Google Play";
            case MAC : return "Mac App Store";
            case TEAVM : return "Web";
            default : return "Download";
        }
    }

    /**
     * Returns the dimensions of the screen that contain the application window.
     * The return value is in the format {@code width}x{@code height}. If the
     * application is not dislayed fullscreen but in the window, the returned
     * dimensions will be for that window rather than for the entire screen.
     */
    public String getScreenSize() {
        Canvas canvas = renderer.getCanvas();
        int width = Math.round(canvas.getWidth() * canvas.getZoomLevel());
        int height = Math.round(canvas.getHeight() * canvas.getZoomLevel());

        return width + "x" + height;
    }

    public float getAverageFramerate() {
        return 1000f / fpsBuffer.getAverageValue();
    }

    public float getAverageFrameTime() {
        return frameTimeBuffer.getAverageValue();
    }
    
    public float getAverageUpdateRate() {
        return 1f / upsBuffer.getAverageValue();
    }

    /**
     * Creates an application and starts it by attaching it to the specified renderer.
     * Note that the application initially starts without an active scene, the initial
     * scene can be played by calling {@link #changeScene(Scene)}.
     * @deprecated Use {@link #start(Renderer, Scene)} instead.
     */
    @Deprecated
    public static Application start(Renderer renderer) {
        Application app = new Application();
        renderer.attach(app);
        renderer.start();
        return app;
    }

    /**
     * Creates an application, starts it by attaching it to the specified renderer,
     * and immediately changes the initial scene so that the application starts by
     * playing that scene.
     */
    public static void start(Renderer renderer, Scene initialScene) {
        Application app = new Application();
        app.changeScene(initialScene);
        renderer.attach(app);
        renderer.start();
    }
}
