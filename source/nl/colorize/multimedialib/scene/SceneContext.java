//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.RenderConfig;
import nl.colorize.multimedialib.stage.Spatial2D;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.util.EventQueue;
import nl.colorize.util.Platform;
import nl.colorize.util.Subject;
import nl.colorize.util.animation.Timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * The scene context is provided to the currently active scene by
 * the renderer, allowing the scene to access the {@link Stage}, the
 * scene's {@link Actor}s, and the underlying renderer.
 * <p>
 * Both the scene's actors and the contents of the stage cannot outlive
 * their parent scene. When the scene ends, the actors are removed and
 * the stage is cleared.
 */
public interface SceneContext {

    public RenderConfig getConfig();

    default Canvas getCanvas() {
        return getConfig().getCanvas();
    }

    public MediaLoader getMediaLoader();

    public InputDevice getInput();

    public Network getNetwork();

    public SceneManager getSceneManager();

    default Stage getStage() {
        return getSceneManager().getStage();
    }

    default FrameStats getFrameStats() {
        return getSceneManager().getFrameStats();
    }

    /**
     * Requests the renderer to change the active scene after the current
     * frame update has been completed. If another scene has already been
     * requested, calling this method again will overrule that request.
     */
    default void changeScene(Scene requestedScene) {
        getSceneManager().changeScene(requestedScene);
    }

    /**
     * Attaches an actor to the currently active scene. The actor will remain
     * active until either its parent scene ends or it is marked as completed,
     * whichever comes first.
     */
    default void attach(Actor actor) {
        getSceneManager().attach(actor);
    }

    /**
     * Attaches an actor consisting of the specified callback functions to the
     * currently active scene. The actor will remain active until either its
     * parent scene ends or it is marked as completed, whichever comes first.
     */
    default void attach(Actor onFrame, BooleanSupplier completed, Runnable onComplete) {
        FluentActor actor = FluentActor.create()
            .withFrameHandler(onFrame)
            .withCompletionCheck(completed)
            .withCompletionHandler(onComplete);

        getSceneManager().attach(actor);
    }

    /**
     * Attaches an actor to the currently active scene. The actor will remain
     * active until either its parent scene ends or it is marked as completed,
     * whichever comes first.
     *
     * @deprecated Prefer using {@link #attach(Actor)}.
     */
    @Deprecated
    default void attach(Runnable callback) {
        Actor actor = _ -> callback.run();
        attach(actor);
    }

    /**
     * Attaches an actor that processes an {@link EventQueue} using the
     * specified callback functions. This ensures asynchronous events are
     * processed during frame updates, instead of immediately when they
     * are received.
     */
    default <T> void attach(EventQueue<T> events, Consumer<T> onEvent, Consumer<Exception> onError) {
        Actor actor = _ -> events.flush(onEvent, onError);
        attach(actor);
    }

    /**
     * Attaches an actor that processes an {@link EventQueue} during frame
     * updates. This returns a {@link Subject} that only publishes events
     * during frame updates, instead of immediately when they are received.
     */
    default <T> Subject<T> attach(EventQueue<T> eventQueue) {
        Subject<T> frameUpdateSubject = new Subject<>();
        attach(() -> eventQueue.flush(frameUpdateSubject::next, frameUpdateSubject::nextError));
        return frameUpdateSubject;
    }

    /**
     * Attaches an actor that will update the timer during every frame
     * update, and then call the {@code onFrame} callback based on the
     * timer's value. Invokes the {@code onComplete} callback exactly once
     * when the timer has completed.
     */
    default void attachTimer(Timer timer, Consumer<Double> onFrame, Runnable onComplete) {
        FluentActor actor = FluentActor.create()
            .withTimerHandler(timer, onFrame)
            .withCompletionHandler(onComplete);

        getSceneManager().attach(actor);
    }

    /**
     * Attaches an actor that will update the timer during every frame
     * update and will then invoke the specified callback function exactly
     * once when the timer has completed.
     */
    default void attachTimer(Timer timer, Runnable callback) {
        attach(timer, timer::isCompleted, callback);
    }

    /**
     * Attaches an actor that will invoke the specified callback function
     * exactly once, after the specified delay.
     */
    default void attachTimer(double delay, Runnable callback) {
        attachTimer(new Timer(delay), callback);
    }

    /**
     * Attaches an actor that will first update the timeline during every
     * frame update and will then invoke the callback function using the
     * timeline's value. The actor will run for the duration of the timeline.
     */
    default void attachTimeline(Timeline timeline, Consumer<Double> callback, Runnable onComplete) {
        FluentActor actor = FluentActor.create()
            .withTimelineHandler(timeline, callback)
            .withCompletionHandler(onComplete);

        getSceneManager().attach(actor);
    }

    /**
     * Attaches an actor that will first update the timeline during every
     * frame update and will then invoke the callback function using the
     * timeline's value. The actor will run for the duration of the timeline.
     */
    default void attachTimeline(Timeline timeline, Consumer<Double> callback) {
        attachTimeline(timeline, callback, () -> {});
    }

    /**
     * Attaches an actor that will invoke a callback function whenever the
     * specified graphics are clicked.
     * <p>
     * Note: This method's title is slightly misleading since it supports
     * pointer events from both mouse and touch devices. The terminology
     * "click handler" remains for historic reasons.
     */
    default void attachClickHandler(Spatial2D node, Runnable callback) {
        attach(() -> {
            if (getInput().isPointerReleased(node)) {
                callback.run();
            }
        });
    }

    /**
     * Attaches an actor that is <em>not</em> tied to the currently active
     * scene. Instead, it will remain active for the remainder of the
     * application's life cycle.
     */
    default void attachGlobalActor(Actor actor) {
        getSceneManager().attachGlobalActor(actor);
    }

    /**
     * Terminates/exits/quits the application.
     * <p>
     * This method is only supported on desktop platforms. Mobile and
     * browser-based platforms do not allow applications to self-terminate.
     * Calling this method on such a platform will therefore have no effect.
     */
    default void terminate() {
    }

    /**
     * Returns debug and support information that can be displayed when running
     * a MultimediaLib application in debug mode. The returned list is intended
     * to be displayed in a {@code Text}, which can be styled to match the
     * application appearance.
     */
    default List<String> getDebugInformation() {
        FrameStats frameStats = getSceneManager().getFrameStats();
        int targetFPS = getConfig().getFramerate();

        List<String> info = new ArrayList<>();
        info.add("Renderer:  " + getConfig().getRendererName());
        info.add("Canvas:  " + getCanvas());
        info.add("Framerate:  " + Math.round(frameStats.getAverageFramerate()) + " / " + targetFPS);
        if (Platform.isDesktopPlatform()) {
            info.add("Memory:  " + (Runtime.getRuntime().totalMemory() / 1_000_000L) + " MB");
        }
        info.add("Update time:  " + frameStats.getFrameUpdateTime() + "ms");
        info.add("Render time:  " + frameStats.getFrameRenderTime() + "ms");

        if (!frameStats.getCustomStats().isEmpty()) {
            info.add("");
        }

        for (String customStat : frameStats.getCustomStats()) {
            info.add(customStat + ":  " + frameStats.getAverageTimeMS(customStat) + "ms");
        }

        return info;
    }
}
