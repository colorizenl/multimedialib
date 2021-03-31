//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.GraphicsLayer2D;
import nl.colorize.multimedialib.math.RotatingBuffer;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.NetworkAccess;
import nl.colorize.multimedialib.renderer.teavm.TeaRenderer;
import nl.colorize.util.Platform;
import nl.colorize.util.PlatformFamily;
import nl.colorize.util.Stopwatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides access to all information that is available in the context of the
 * currently active scene. This includes the stage which contains all graphics
 * and audio, but also the renderer and the underlying platform.
 */
public final class SceneContext implements Updatable {

    private Canvas canvas;
    private Stage stage;
    private InputDevice inputDevice;
    private MediaLoader mediaLoader;
    private MediaManager media;
    private NetworkAccess network;

    private Scene activeScene;
    private Scene requestedScene;
    private List<Agent> activeAgents;
    private List<Agent> requestedAgents;
    private List<Agent> completedAgents;

    private Stopwatch fpsTimer;
    private RotatingBuffer fpsBuffer;
    private RotatingBuffer frameTimeBuffer;

    private static final int FPS_MEASUREMENT_BUFFER_SIZE = 100;

    /**
     * Initializes the scene context. This constructor is used by the renderer,
     * which will then provide the scene context to scenes. There should be no
     * reason to call this constructor from application code.
     */
    public SceneContext(Canvas canvas, InputDevice input, MediaLoader mediaLoader, NetworkAccess network) {
        this.canvas = canvas;
        this.stage = new Stage(canvas);
        this.inputDevice = input;
        this.mediaLoader = mediaLoader;
        this.media = new MediaManager(mediaLoader);
        this.network = network;

        this.activeScene = null;
        this.requestedScene = null;
        this.activeAgents = new ArrayList<>();
        this.requestedAgents = new ArrayList<>();
        this.completedAgents = new ArrayList<>();

        this.fpsTimer = new Stopwatch();
        this.fpsBuffer = new RotatingBuffer(FPS_MEASUREMENT_BUFFER_SIZE, 1000f);
        this.frameTimeBuffer = new RotatingBuffer(FPS_MEASUREMENT_BUFFER_SIZE);
    }

    //---------------------------------
    // Animation loop
    //---------------------------------

    @Override
    public void update(float deltaTime) {
        Preconditions.checkState(activeScene != null || requestedScene != null,
            "No initial scene requested");

        long fpsValue = fpsTimer.tick();
        fpsBuffer.add(fpsValue);

        Stopwatch frameTimer = new Stopwatch();
        frameTimer.tick();

        updateFrame(deltaTime);

        long actualFrameTime = frameTimer.tick();
        frameTimeBuffer.add(actualFrameTime);
    }

    private void updateFrame(float deltaTime) {
        if (requestedScene != null) {
            activateRequestedScene();
        }

        activeAgents.addAll(requestedAgents);
        requestedAgents.clear();
        activeAgents.removeAll(completedAgents);
        completedAgents.clear();

        updateActiveScene(deltaTime);
    }

    private void updateActiveScene(float deltaTime) {
        activeScene.update(this, deltaTime);

        for (Agent agent : activeAgents) {
            agent.update(deltaTime);

            if (agent.isCompleted()) {
                completedAgents.add(agent);
            }
        }
    }

    private void activateRequestedScene() {
        if (activeScene != null) {
            activeScene.end(this);
            activeAgents.clear();
            completedAgents.clear();
            stage.clear();
        }

        activeScene = requestedScene;
        requestedScene = null;
        activeScene.start(this);

        if (activeScene instanceof GraphicsLayer2D) {
            stage.addLayer((GraphicsLayer2D) activeScene);
        }
    }

    //---------------------------------
    // Scene management
    //---------------------------------

    /**
     * Requests to change the active scene after the current frame has been
     * completed.
     * <p>
     * If the scene also implements the {@link GraphicsLayer2D} interface, it
     * is immediately registered as a graphics layer. This is done for backward
     * compatibility with old versions of MultimediaLib, where scenes also
     * provided 2D graphics.
     *
     * @throws IllegalStateException if a different scene has already been
     *         requested, but that scene has not yet started.
     */
    public void changeScene(Scene requestedScene) {
        Preconditions.checkState(this.requestedScene == null,
            "Another scene has already been requested: " + requestedScene);

        this.requestedScene = requestedScene;
    }

    /**
     * Attaches an agent to the currently active scene. The agent will remain
     * active until it either completes or the current scene has ended.
     * <p>
     * If the agent *also* implements the {@link GraphicsLayer2D} interface,
     * calling this method will also attach the agent's graphics to the current
     * scene.
     */
    public void attachAgent(Agent agent) {
        requestedAgents.add(agent);

        if (agent instanceof GraphicsLayer2D) {
            stage.addLayer((GraphicsLayer2D) agent);
        }
    }

    public void cancelAgent(Agent agent) {
        activeAgents.remove(agent);
        requestedAgents.remove(agent);
    }

    //---------------------------------
    // Renderer access
    //---------------------------------

    public Canvas getCanvas() {
        return canvas;
    }

    public Stage getStage() {
        return stage;
    }

    public InputDevice getInputDevice() {
        return inputDevice;
    }

    public MediaLoader getMediaLoader() {
        return mediaLoader;
    }

    public MediaManager getMedia() {
        return media;
    }

    public NetworkAccess getNetworkAccess() {
        return network;
    }

    //---------------------------------
    // Application utilities
    //---------------------------------

    /**
     * Returns the platform running the application. When running in a browser,
     * this will return the platform that is running the browser based on the
     * browser's {@code User-Agent} header.
     */
    public PlatformFamily getPlatform() {
        if (Platform.isTeaVM()) {
            return TeaRenderer.getPlatform();
        } else {
            return Platform.getPlatformFamily();
        }
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

    /**
     * Returns true if the current platform allows the application to quit. If true,
     * actually quitting the application can be done using {@link #quit()}. This will
     * return false on platforms like browsers that do not allow applications to
     * control the platform itself.
     */
    public boolean canQuit() {
        return Platform.isWindows() || Platform.isMac();
    }

    /**
     * Quits the application. This will only work if the current platform allows the
     * application to quit, i.e. {@link #canQuit()} returns true. If not, this method
     * does nothing.
     */
    public void quit() {
        if (canQuit()) {
            System.exit(0);
        }
    }
}
