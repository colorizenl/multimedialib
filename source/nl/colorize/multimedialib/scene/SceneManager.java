//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import lombok.Getter;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.Pointer;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.util.Stopwatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * Controls the scene life cycle, including the life cycle of its attached
 * sub-scenes. The scene manager is accessible to the currently active scene
 * via the {@link SceneContext}.
 */
public class SceneManager {

    private Stopwatch timer;
    private long elapsedTime;
    @Getter private FrameStats frameStats;

    private SceneState activeScene;
    private Queue<SceneState> requestedSceneQueue;
    private List<Scene> globalSubScenes;

    private static final long FRAME_LEEWAY_MS = 5;
    private static final float MIN_FRAME_TIME = 0.01f;
    private static final float MAX_FRAME_TIME = 0.2f;

    public SceneManager(Stopwatch timer) {
        this.timer = timer;
        this.elapsedTime = 0L;
        this.frameStats = new FrameStats();

        this.requestedSceneQueue = new LinkedList<>();
        this.globalSubScenes = new ArrayList<>();
    }

    public SceneManager() {
        this(new Stopwatch());
    }

    /**
     * Synchronizes between "native" frames and application frame updates.
     * Should be called by the renderer during every "native" frame. Depending
     * on the elapsed time and target framerate, this method will then perform
     * application frame updates.
     * <p>
     * Although this class provides the renderer with the delta time since the
     * last frame update, this may not always reflect the <em>actual</em>
     * elapsed time. it is not realistic to expect applications to be able to
     * function correctly for every possible {@code deltaTime} value, so this
     * method will attempt to produce frame updates that try to find a balance
     * between the targeted framerate and the actual elapsed time.
     * <p>
     * Note that rendering the frame is <em>not</em> managed by this method.
     * The renderer should make sure that every "native" frame is rendered,
     * even if that frame did not lead to an application frame update.
     * <p>
     * Calling this method will also register the corresponding performance
     * statistics with the {@code FrameStats} instance provided in the
     * constructor.
     *
     * @return The number of application frame updates that were performed
     *         during the frame synchronization process. A value of zero
     *         indicates no frame updates were performed, meaning that it is
     *         not necessary for the renderer to render the frame.
     */
    public int requestFrameUpdate(SceneContext context) {
        long frameTime = timer.tick();
        elapsedTime += frameTime;

        long targetFrameTime = Math.round(1000f / context.getConfig().getFramerate());
        if (elapsedTime < targetFrameTime - FRAME_LEEWAY_MS) {
            return 0;
        }

        // Only count the frame when calling this method actually leads
        // to a frame update. Otherwise, this would just count the
        // precision of the underlying animation loop.
        frameStats.markEnd(FrameStats.PHASE_FRAME_TIME);

        float deltaTime = Math.clamp(elapsedTime / 1000f, MIN_FRAME_TIME, MAX_FRAME_TIME);
        frameStats.markStart(FrameStats.PHASE_FRAME_UPDATE);
        performFrameUpdate(context, deltaTime);
        frameStats.markEnd(FrameStats.PHASE_FRAME_UPDATE);
        elapsedTime = 0L;

        return 1;
    }

    /**
     * Performs an application frame update. The renderer will first call
     * {@link #requestFrameUpdate(SceneContext)}, which then calls this method
     * depending on how much time has elapsed since the last frame.
     */
    protected void performFrameUpdate(SceneContext context, float deltaTime) {
        updateInput(context.getInput(), deltaTime);

        if (!requestedSceneQueue.isEmpty()) {
            activateRequestedScene(context);
        }

        updateCurrentScene(context, activeScene, deltaTime);
        updateGlobalSubScenes(context, deltaTime);
    }

    private void updateInput(InputDevice input, float deltaTime) {
        if (!(input instanceof Renderer)) {
            input.update(deltaTime);
        }

        for (Pointer pointer : input.getPointers()) {
            pointer.update(deltaTime);
        }
    }

    private void updateCurrentScene(SceneContext context, SceneState current, float deltaTime) {
        current.scene.update(context, deltaTime);

        // Iterate the list of systems backwards to handle
        // concurrent modification while the list is being
        // iterated, without having to create a copy of the
        // list every frame.
        for (int i = current.subScenes.size() - 1; i >= 0; i--) {
            Scene subScene = current.subScenes.get(i);

            // We need to check twice if the sub-scene has
            // been completed, both before and after its
            // own update.
            if (!checkCompleted(context, current, subScene)) {
                subScene.update(context, deltaTime);
                checkCompleted(context, current, subScene);
            }
        }

        context.getStage().getAnimationTimer().update(deltaTime);
    }

    private boolean checkCompleted(SceneContext context, SceneState parent, Scene subScene) {
        if (subScene.isCompleted()) {
            subScene.end(context);
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
    private void activateRequestedScene(SceneContext context) {
        if (activeScene != null) {
            activeScene.walk(scene -> scene.end(context));
            context.getStage().clear();
            context.getStage().getAnimationTimer().reset();
        }

        SceneState requestedScene = requestedSceneQueue.peek();

        if (requestedScene != null) {
            activeScene = requestedScene;
            activeScene.walk(scene -> scene.start(context));
            requestedSceneQueue.poll();

            if (!requestedSceneQueue.isEmpty()) {
                activateRequestedScene(context);
            }
        }
    }

    private void updateGlobalSubScenes(SceneContext context, float deltaTime) {
        Iterator<Scene> iterator = globalSubScenes.iterator();

        while (iterator.hasNext()) {
            Scene globalScene = iterator.next();
            globalScene.update(context, deltaTime);

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
        requestedSceneQueue.offer(new SceneState(requestedScene));
    }

    /**
     * Attaches a sub-scene to the currently active scene. The sub-scene will
     * remain active until it is detached or the parent scene ends.
     */
    public void attach(SceneContext context, Scene subScene) {
        if (requestedSceneQueue.isEmpty()) {
            activeScene.attachSubScene(subScene);
            subScene.start(context);
        } else {
            SceneState requestedScene = requestedSceneQueue.peek();
            requestedScene.attachSubScene(subScene);
        }
    }

    /**
     * Attaches a scene that is *not* tied to the currently active scene, and
     * will remain active for the rest of the application. Multiple global
     * scenes can be attached.
     */
    public void attachGlobal(SceneContext context, Scene globalSubScene) {
        globalSubScenes.add(globalSubScene);
        globalSubScene.start(context);
    }

    /**
     * Returns true if the specified scene is currently active. Note this
     * will also return true if the specified scene has been attached as
     * a sub-scene.
     */
    public boolean isActiveScene(Scene scene) {
        if (activeScene == null) {
            return false;
        }

        if (activeScene.scene.equals(scene)) {
            return true;
        }

        return activeScene.subScenes.stream()
            .anyMatch(subScene -> subScene.equals(scene));
    }

    /**
     * One of the scenes that is managed by this {@link SceneContext},
     * consisting of both the scene itself plus all of its attached
     * sub-scenes.
     */
    private static class SceneState {

        private Scene scene;
        private List<Scene> subScenes;

        public SceneState(Scene scene) {
            this.scene = scene;
            this.subScenes = new ArrayList<>();
        }

        public void attachSubScene(Scene subScene) {
            // We iterate the sub-scenes backwards, but still want
            // to preserve the expected order.
            subScenes.addFirst(subScene);
        }

        public void walk(Consumer<Scene> callback) {
            callback.accept(scene);
            // We iterate the list backwards to avoid issues with
            // concurrent modification.
            for (int i = subScenes.size() - 1; i >= 0; i--) {
                callback.accept(subScenes.get(i));
            }
        }
    }
}
