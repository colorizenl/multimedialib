//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.MathUtils;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;
import nl.colorize.util.Stopwatch;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides access to the contents of the currently active scene, including
 * the stage and the scene's graphics, and to the underlying renderer. The
 * {@link SceneContext} is passed to the scene during each frame update, and
 * enables the scene to perform the application's frame update logic.
 * <p>
 * The {@link SceneContext} also allows <em>sub-scenes</em> to be attached to
 * the current scene. These sub-scenes can contain their own logic, but cannot
 * outlive their parent scene. When the active scene is changed, both the scene
 * itself and its sub-scenes will be terminated and the stage will be cleared
 * in preparation for the next scene.
 */
public final class SceneContext implements Updatable {

    private Renderer renderer;
    private Stopwatch timer;
    private long elapsedTime;
    private FrameStats stats;

    private Stage stage;
    private SceneLogic activeScene;
    private Queue<SceneLogic> requestedSceneQueue;
    private List<Scene> globalScenes;

    private static final long FRAME_LEEWAY_MS = 5;
    private static final float MIN_FRAME_TIME = 0.01f;
    private static final float MAX_FRAME_TIME = 0.2f;
    private static final Logger LOGGER = LogHelper.getLogger(SceneContext.class);

    /**
     * Creates a new {@code SceneContext} that is based on the provided
     * underlying renderer.
     * <p>
     * Applications should not call this constructor directly. This
     * constructor is public so different renderer implementations can
     * create {@code SceneContext} instances, but this is normally done
     * by the renderer itself during initialization.
     */
    public SceneContext(Renderer renderer, Stopwatch timer) {
        this.renderer = renderer;
        this.timer = timer;
        this.elapsedTime = 0L;
        this.stats = new FrameStats(renderer.getDisplayMode());

        this.stage = new Stage(renderer.getGraphicsMode(), renderer.getCanvas());
        this.requestedSceneQueue = new LinkedList<>();
        this.globalScenes = new ArrayList<>();
    }

    /**
     * Synchronizes between "native" frames and application frame updates.
     * Should be called by the renderer during every "native" frame. Depending
     * on the elapsed time and target framerate, this method will then manage
     * application frame updates by calling {@link #update(float)} accordingly.
     * <p>
     * Although this class provides the renderer with the delta time since the last
     * frame update, this may not always reflect the <em>actual</em> elapsed time.
     * it is not realistic to expect applications to be able to function correctly
     * for every possible {@code deltaTime} value, so this method will attempt to
     * produce frame updates that try to find a balance between the targeted
     * framerate and the actual elapsed time.
     * <p>
     * Note that rendering the frame is <em>not</em> managed by this method. The
     * renderer should make sure that every "native" frame is rendered, even if
     * that frame did not lead to an application frame update.
     * <p>
     * Calling this method will also register the corresponding performance
     * statistics with the {@code FrameStats} instance provided in the
     * constructor.
     *
     * @return True if calling this method resulted in at least one application
     *         frame update.
     */
    public boolean syncFrame() {
        long frameTime = timer.tick();
        elapsedTime += frameTime;

        if (elapsedTime < getDisplayMode().getFrameTimeMS() - FRAME_LEEWAY_MS) {
            return false;
        }

        // Only count the frame when calling this method actually leads
        // to a frame update. Otherwise, this would just count the
        // precision of the underlying animation loop.
        stats.markEnd(FrameStats.PHASE_FRAME_TIME);

        float deltaTime = MathUtils.clamp(elapsedTime / 1000f, MIN_FRAME_TIME, MAX_FRAME_TIME);
        stats.markStart(FrameStats.PHASE_FRAME_UPDATE);
        update(deltaTime);
        stats.markEnd(FrameStats.PHASE_FRAME_UPDATE);

        elapsedTime = 0L;
        return true;
    }

    /**
     * Performs an application frame update. The renderer should <em>not</em>
     * call this method. Instead, it should call {@link #syncFrame()}, which
     * will decouple "native" frame updates in the renderer from application
     * frame updates. Attempting to render the application at the renderer's
     * native refresh rate can introduce slowdown if application and/or
     * graphics complexity makes it impossible to actually achieve the native
     * framerate.
     */
    @Override
    public void update(float deltaTime) {
        getInput().update(deltaTime);

        stage.update(deltaTime);

        if (!requestedSceneQueue.isEmpty()) {
            activateRequestedScene();
        }

        updateSceneGraph(activeScene, deltaTime);
        updateGlobalScenes(deltaTime);
    }

    private void updateSceneGraph(SceneLogic current, float deltaTime) {
        current.scene.update(this, deltaTime);

        // Iterate the list of systems backwards to handle
        // concurrent modification while the list is being
        // iterated, without having to create a copy of the
        // list every frame.
        for (int i = current.subScenes.size() - 1; i >= 0; i--) {
            Scene subScene = current.subScenes.get(i);

            // We need to check twice if the sub-scene has
            // been completed, both before and after its
            // own update.
            if (!checkCompleted(current, subScene)) {
                subScene.update(this, deltaTime);
                checkCompleted(current, subScene);
            }
        }
    }

    private boolean checkCompleted(SceneLogic parent, Scene subScene) {
        if (subScene.isCompleted()) {
            subScene.end(this);
            parent.subScenes.remove(subScene);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Activated the next requested scene. If multiple scenes were requested
     * during the active scene's most recent frame, these scenes are activated
     * in the order they were added to the queue. In practical terms this means
     * earlier scenes will receive start events, but are then immediately
     * replaced by the next requested scene, meaning they will never actually
     * receive frame updates.
     */
    private void activateRequestedScene() {
        if (activeScene != null) {
            activeScene.walk(scene -> scene.end(this));
            stage.clear();
        }

        SceneLogic requestedScene = requestedSceneQueue.poll();

        if (requestedScene != null) {
            activeScene = requestedScene;
            activeScene.walk(scene -> scene.start(this));

            if (!requestedSceneQueue.isEmpty()) {
                activateRequestedScene();
            }
        }
    }

    private void updateGlobalScenes(float deltaTime) {
        Iterator<Scene> iterator = globalScenes.iterator();

        while (iterator.hasNext()) {
            Scene globalScene = iterator.next();
            globalScene.update(this, deltaTime);

            if (globalScene.isCompleted()) {
                iterator.remove();
            }
        }
    }

    /**
     * Requests to change the active scene after the current frame update has
     * been completed. If another scene had already been requested, calling
     * this method again will overrule that request.
     */
    public void changeScene(Scene requestedScene) {
        requestedSceneQueue.offer(new SceneLogic(requestedScene));
    }

    /**
     * Attaches a sub-scene to the currently active scene. The sub-scene will
     * remain active until it is detached or the parent scene ends.
     */
    public void attach(Scene subScene) {
        // We iterate the list of sub-scenes backwards to allow for
        // concurrent modification, but that means we need to store
        // the list in reverse order to keep the expected behavior.
        // This is a relatively expensive operation, but we expect
        // iterating sub-scenes is done *much* more often than adding
        // sub-scenes.
        if (requestedSceneQueue.isEmpty()) {
            activeScene.subScenes.add(0, subScene);
            subScene.start(this);
        } else {
            SceneLogic requestedScene = requestedSceneQueue.peek();
            requestedScene.subScenes.add(0, subScene);
        }
    }

    /**
     * Attaches an {@link Updatable} to act as a sub-scene for the currently
     * active scene. The sub-scene will remain active until it is detached or
     * the parent scene ends.
     */
    public void attach(Updatable subScene) {
        Scene wrappedSubScene = (context, deltaTime) -> subScene.update(deltaTime);
        attach(wrappedSubScene);
    }

    /**
     * Convenience method that attaches a {@link InteractiveObject} to the
     * currently active scene, as well as adding its graphics to the specified
     * parent. This avoids having to register logic and graphics separately.
     */
    public void attach(InteractiveObject subScene, Container parent) {
        attach((Scene) subScene);
        parent.addChild(subScene);
    }

    /**
     * Convenience method that attaches a {@link InteractiveObject} to the
     * currently active scene ands adds its graphics to the stage root. Using
     * this method is equivalent to {@code attach(subScene, stage.getRoot())}.
     *
     * @deprecated Prefer {@link #attach(InteractiveObject, Container)} to
     *             have more explicit behavior on where the object's graphics
     *             should appear in relation to other graphics.
     */
    @Deprecated
    public void attach(InteractiveObject subScene) {
        attach(subScene, stage.getRoot());
    }

    /**
     * Attaches a scene that is *not* tied to the currently active scene, and
     * will remain active for the rest of the application. Multiple global
     * scenes can be attached.
     */
    public void attachGlobal(Scene globalScene) {
        globalScenes.add(globalScene);
        globalScene.start(this);
    }

    public Stage getStage() {
        return stage;
    }

    public FrameStats getFrameStats() {
        return stats;
    }

    /**
     * Returns the display name for the renderer that is powering this context.
     * The display name does not include the word "renderer" itself.
     */
    public String getRendererName() {
        if (renderer.getGraphics() == null) {
            return "<headless>";
        }

        return renderer.getGraphics().getClass().getSimpleName()
            .replace("GraphicsContext", "")
            .replace("Graphics", "");
    }

    /**
     * Returns debug and support information that can be displayed when running
     * a MultimediaLib application in debug mode. The returned list is intended
     * to be displayed in a {@code Text}, which can be styled to match the
     * application appearance.
     */
    public List<String> getDebugInformation() {
        List<String> info = new ArrayList<>();
        info.add("Renderer:  " + getRendererName());
        info.add("Canvas:  " + getCanvas());
        info.add("Framerate:  " + stats.getActualFramerate() + " / " + stats.getTargetFramerate());
        info.add("Update time:  " + stats.getFrameUpdateTime() + "ms");
        info.add("Render time:  " + stats.getFrameRenderTime() + "ms");

        if (!stats.getCustomStats().isEmpty()) {
            info.add("--------");
        }

        for (String customStat : stats.getCustomStats()) {
            info.add(customStat + ":  " + stats.getAverageTimeMS(customStat) + "ms");
        }

        return info;
    }

    //-------------------------------------------------------------------------
    // Renderer access
    //-------------------------------------------------------------------------

    public void terminate() {
        renderer.terminate();
    }

    public GraphicsMode getGraphicsMode() {
        return renderer.getGraphicsMode();
    }

    public DisplayMode getDisplayMode() {
        return renderer.getDisplayMode();
    }

    public Canvas getCanvas() {
        return renderer.getCanvas();
    }

    public InputDevice getInput() {
        return renderer.getInput();
    }

    public MediaLoader getMediaLoader() {
        return renderer.getMediaLoader();
    }

    public Network getNetwork() {
        return renderer.getNetwork();
    }

    /**
     * Convenience method that captures a screenshot and saves it to a PNG
     * file on the user's desktop. Only supported on desktop platforms, does
     * nothing on mobile platforms or in the browser.
     */
    public void takeScreenshot() {
        Preconditions.checkState(Platform.isWindows() || Platform.isMac(),
            "Taking screenshots is not supported on platform " + Platform.getPlatform());

        try {
            File outputFile = new File(Platform.getUserDesktopDir(),
                "screenshot-" + System.currentTimeMillis() + ".png");
            renderer.takeScreenshot(outputFile);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to save screenshot", e);
        }
    }

    /**
     * One of the scenes that is managed by this {@link SceneContext},
     * consisting of both the scene itself plus all of its attached
     * sub-scenes.
     */
    private record SceneLogic(Scene scene, List<Scene> subScenes) {

        public SceneLogic(Scene scene) {
            this(scene, new ArrayList<>());
        }

        public void walk(Consumer<Scene> callback) {
            callback.accept(scene);
            subScenes.forEach(callback);
        }
    }
}
