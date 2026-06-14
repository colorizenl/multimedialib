//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import lombok.Getter;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.Pointer;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.util.Stopwatch;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Responsible for the scene life cycle, including the life cycle of its
 * attached actors and the stage.
 * <p>
 * Applications will typically interact with the {@link SceneContext},
 * instead of directly with this class. The purpos of this class can be
 * seen as a partial standard implementation of {@link SceneContext},
 * allowing the scene management logic to be shared across renderers.
 */
public class SceneManager {

    private SceneContext context;
    private Stopwatch animationTimer;
    private long elapsedTime;
    @Getter private FrameStats frameStats;

    private SceneLogic activeScene;
    private List<Actor> attachedActors;
    @Getter private Stage stage;
    private Queue<SceneLogic> requestedSceneQueue;
    private List<Actor> globalActors;

    private static final long FRAME_LEEWAY_MS = 5;
    private static final double MIN_FRAME_TIME = 0.01f;
    private static final double MAX_FRAME_TIME = 0.2f;

    protected SceneManager(SceneContext context, Stopwatch timer) {
        this.context = context;
        this.animationTimer = timer;
        this.elapsedTime = 0L;
        this.frameStats = new FrameStats();

        activeScene = null;
        attachedActors = new CopyOnWriteArrayList<>();
        stage = new Stage(context.getConfig().getCanvas());
        requestedSceneQueue = new ArrayDeque<>();
        globalActors = new CopyOnWriteArrayList<>();
    }

    public SceneManager(SceneContext context, Scene initialScene) {
        this(context, new Stopwatch());
        changeScene(initialScene);
    }

    @Deprecated
    public SceneManager(SceneContext context) {
        this(context, new Stopwatch());
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
    public int requestFrameUpdate() {
        long frameTime = animationTimer.tick();
        elapsedTime += frameTime;

        long targetFrameTime = Math.round(1000f / context.getConfig().getFramerate());
        if (elapsedTime < targetFrameTime - FRAME_LEEWAY_MS) {
            return 0;
        }

        // Only count the frame when calling this method actually leads
        // to a frame update. Otherwise, this would just count the
        // precision of the underlying animation loop.
        frameStats.markEnd(FrameStats.PHASE_FRAME_TIME);

        double deltaTime = Math.clamp(elapsedTime / 1000f, MIN_FRAME_TIME, MAX_FRAME_TIME);
        frameStats.markStart(FrameStats.PHASE_FRAME_UPDATE);
        performFrameUpdate(deltaTime);
        frameStats.markEnd(FrameStats.PHASE_FRAME_UPDATE);
        elapsedTime = 0L;

        return 1;
    }

    /**
     * Performs an application frame update. The renderer will first call
     * {@link #requestFrameUpdate()}, which then calls this method depending
     * on how much time has elapsed since the last frame.
     */
    protected void performFrameUpdate(double deltaTime) {
        updateInput(context.getInput(), deltaTime);

        if (!requestedSceneQueue.isEmpty()) {
            activateRequestedScene();
        }

        updateActiveScene(deltaTime);
        updateGlobalActors(deltaTime);
    }

    private void updateInput(InputDevice input, double deltaTime) {
        if (!(input instanceof Renderer)) {
            input.update(deltaTime);
        }

        for (Pointer pointer : input.getPointers()) {
            pointer.update(deltaTime);
        }
    }

    private void updateActiveScene(double deltaTime) {
        activeScene.scene.update(context, deltaTime);

        for (Actor actor : activeScene.attachedActors) {
            updateActor(actor, deltaTime);

            // We need to check an actor's status again,
            // in case it has been marked as completed
            // during the frame update that just happened.
            if (actor.isCompleted()) {
                activeScene.attachedActors.remove(actor);
            }
        }

        stage.getAnimationTimer().update(deltaTime);
    }

    private void updateActor(Actor actor, double deltaTime) {
        if (actor.isCompleted()) {
            return;
        }

        actor.update(deltaTime);

        for (Actor subActor : actor.getSubActors()) {
            updateActor(subActor, deltaTime);
        }

        // Top-level actors are removed after they have
        // been marked as completed. But this class does
        // not "own" an actor's list of sub-actors, so
        // for sub-actors we merely skip the completed
        // ones.
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
            activeScene.scene.end(context);
            stage.clear();
            stage.getAnimationTimer().reset();
        }

        SceneLogic requestedScene = requestedSceneQueue.poll();

        if (requestedScene != null) {
            activeScene = requestedScene;
            activeScene.scene.start(context);

            if (!requestedSceneQueue.isEmpty()) {
                activateRequestedScene();
            }
        }
    }

    private void updateGlobalActors(double deltaTime) {
        for (Actor actor : globalActors) {
            if (!actor.isCompleted()) {
                actor.update(deltaTime);
                // Same as with "normal" scene actors,
                // we need to check again if it was
                // marked as completed during the
                // frame update we just did.
                if (actor.isCompleted()) {
                    globalActors.remove(actor);
                }
            }
        }
    }

    /**
     * Requests to change the active scene after the current frame update has
     * been completed. If another scene had already been requested, calling
     * this method again will overrule that request.
     *
     * @see SceneContext#changeScene(Scene)
     */
    public void changeScene(Scene requestedScene) {
        List<Actor> attachedActors = new CopyOnWriteArrayList<>();
        SceneLogic sceneConfig = new SceneLogic(requestedScene, attachedActors);
        requestedSceneQueue.offer(sceneConfig);
    }

    /**
     * Attaches an actor to the currently active scene. The actor will remain
     * active until either its parent scene ends or it is marked as completed,
     * whichever comes first.
     *
     * @see SceneContext#attach(Actor)
     */
    public void attach(Actor actor) {
        if (requestedSceneQueue.isEmpty()) {
            activeScene.attachedActors.add(actor);
        } else {
            SceneLogic nextScene = requestedSceneQueue.peek();
            nextScene.attachedActors.add(actor);
        }
    }

    /**
     * Attaches an actor that is <em>not</em> tied to the currently active
     * scene. Instead, it will remain active for the remainder of the
     * application's life cycle.
     *
     * @see SceneContext#attachGlobalActor(Actor)
     */
    public void attachGlobalActor(Actor actor) {
        globalActors.add(actor);
    }

    /**
     * Combines a scene with its attached actors. Keeping them together allows
     * actors to be attached before the scene has received its first frame
     * update.
     */
    private record SceneLogic(Scene scene, List<Actor> attachedActors) {
    }
}
