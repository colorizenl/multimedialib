//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.NetworkAccess;
import nl.colorize.util.Platform;
import nl.colorize.util.PlatformFamily;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides access to all information that is available in the context of the
 * currently active scene. This includes the stage which contains all graphics
 * and audio, but also the renderer and the underlying platform.
 */
public class SceneContext implements Updatable {

    private Stage stage;
    private InputDevice inputDevice;
    private MediaLoader mediaLoader;
    private NetworkAccess network;

    private Scene activeScene;
    private List<ActorSystem> activeSceneSystems;
    private Scene requestedScene;
    private List<ActorSystem> requestedSceneSystems;

    private FrameStats frameStats;

    /**
     * Initializes the scene context. This constructor is used by the renderer,
     * which will then provide the scene context to scenes. There should be no
     * reason to call this constructor from application code.
     */
    public SceneContext(DisplayMode displayMode, InputDevice input, MediaLoader mediaLoader,
                        NetworkAccess network) {
        this.stage = new Stage(displayMode.canvas());
        this.inputDevice = input;
        this.mediaLoader = mediaLoader;
        this.network = network;

        this.activeScene = null;
        this.activeSceneSystems = new ArrayList<>();
        this.requestedScene = null;
        this.requestedSceneSystems = new ArrayList<>();

        this.frameStats = new FrameStats(displayMode.framerate());
    }

    @Override
    public void update(float deltaTime) {
        Preconditions.checkState(activeScene != null || requestedScene != null,
            "No initial scene requested");

        updateFrame(deltaTime);
    }

    private void updateFrame(float deltaTime) {
        stage.update(deltaTime);

        if (requestedScene != null) {
            activateRequestedScene();
        }

        updateActiveScene(deltaTime);
    }

    private void updateActiveScene(float deltaTime) {
        activeScene.update(this, deltaTime);

        // Iterate the list of systems backwards to handle
        // concurrent modification while the list is being
        // iterated, without having to create a copy of the
        // list every frame.
        for (int i = activeSceneSystems.size() - 1; i >= 0; i--) {
            ActorSystem system = activeSceneSystems.get(i);
            system.update(this, deltaTime);

            if (system.isCompleted()) {
                activeSceneSystems.remove(system);
            }
        }
    }

    private void activateRequestedScene() {
        if (activeScene != null) {
            activeScene.end(this);
            activeSceneSystems.clear();
            stage.clear();
        }

        activeScene = requestedScene;
        activeSceneSystems.addAll(requestedSceneSystems);

        requestedScene = null;
        List<ActorSystem> uninitializedSystems = ImmutableList.copyOf(requestedSceneSystems);
        requestedSceneSystems.clear();

        activeScene.start(this);
        uninitializedSystems.forEach(system -> system.init(this));
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
            "Requested " + requestedScene + ", but already requested " + this.requestedScene);

        this.requestedScene = requestedScene;
    }

    /**
     * Attaches a system to the currently active scene. The system will remain
     * active until either completed or the current scene ends.
     */
    public void attach(ActorSystem system) {
        // We iterate the list of systems backwards to allow for concurrent
        // modification, but that means we need to store the list in reverse
        // order to keep the expected behavior. This is a relatively expensive
        // operation, but we expect iterating systems is done *much* more often
        // than adding systems.
        if (requestedScene == null) {
            activeSceneSystems.add(0, system);
            system.init(this);
        } else {
            requestedSceneSystems.add(0, system);
        }
    }

    /**
     * Attaches a system to the currently active scene. The system will remain
     * active until either completed or the current scene ends. This is a
     * shorthand for {@link ActorSystem#wrap(Updatable)}.
     */
    public void attach(Updatable system) {
        attach(ActorSystem.wrap(system));
    }

    /**
     * Attaches a system that will wait until the specified duration in seconds
     * has been reached, after which it will perform the specified action and
     * immediately end.
     *
     * @deprecated Use {@link ActorSystem#delay(float, Runnable)} instead.
     */
    @Deprecated
    public void delay(float duration, Runnable action) {
        attach(ActorSystem.delay(duration, action));
    }

    public Stage getStage() {
        return stage;
    }

    public Stage3D getStage3D() {
        return (Stage3D) stage;
    }

    public Canvas getCanvas() {
        return stage.getCanvas();
    }

    public InputDevice getInputDevice() {
        return inputDevice;
    }

    public MediaLoader getMediaLoader() {
        return mediaLoader;
    }

    public NetworkAccess getNetwork() {
        return network;
    }

    /**
     * Returns the platform running the application. When running in a browser,
     * this will return the platform that is running the browser based on the
     * browser's {@code User-Agent} header.
     */
    public PlatformFamily getPlatform() {
        return mediaLoader.getPlatformFamily();
    }

    /**
     * Returns the distribution channel that was used to obtain the application.
     * Examples of return values are "App Store", "Download", and "Web".
     */
    public String getDistributionChannel() {
        return switch (getPlatform()) {
            case IOS -> "App Store";
            case ANDROID -> "Google Play";
            case MAC -> "Mac App Store";
            case TEAVM -> "Web";
            default -> "Download";
        };
    }

    /**
     * Returns the dimensions of the screen that contain the application window.
     * The return value is in the format {@code width}x{@code height}. If the
     * application is not dislayed fullscreen but in the window, the returned
     * dimensions will be for that window rather than for the entire screen.
     */
    public String getScreenSize() {
        Canvas canvas = stage.getCanvas();
        int width = Math.round(canvas.getWidth() * canvas.getZoomLevel());
        int height = Math.round(canvas.getHeight() * canvas.getZoomLevel());
        return width + "x" + height;
    }

    public FrameStats getFrameStats() {
        return frameStats;
    }

    /**
     * Returns true if the current platform allows the application to quit. If
     * true, actually quitting the application can be done using {@link #quit()}.
     * This will return false on platforms like browsers that do not allow
     * applications to control the platform itself.
     */
    public boolean canQuit() {
        return Platform.isWindows() || Platform.isMac();
    }

    /**
     * Quits the application. This will only work if the current platform allows
     * the application to quit, i.e. {@link #canQuit()} returns true. If not,
     * this method does nothing.
     */
    public void quit() {
        if (canQuit()) {
            System.exit(0);
        }
    }
}
