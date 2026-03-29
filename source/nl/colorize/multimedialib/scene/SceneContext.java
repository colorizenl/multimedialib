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
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.StageNode2D;
import nl.colorize.util.EventQueue;
import nl.colorize.util.Platform;
import nl.colorize.util.Subject;
import nl.colorize.util.animation.Timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * The currently active scene (and its sub-scenes) receive access to the
 * <em>scene context</em>, which is provided by the renderer via callback
 * methods. This allows the scene to access the underlying renderer and
 * the stage.
 * <p>
 * The <em>stage</em> contains the graphics and audio for the currently
 * active scene. The stage can contain both 2D and 3D graphics, depending
 * on what is supported by the renderer. At the end of a scene, the stage
 * is cleared so the next scene can take over.
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
     * Requests to change the active scene after the current frame update has
     * been completed. If another scene had already been requested, calling
     * this method again will overrule that request.
     */
    default void changeScene(Scene requestedScene) {
        getSceneManager().changeScene(requestedScene);
    }

    /**
     * Attaches a sub-scene to the currently active scene. The sub-scene will
     * remain active until {@link Scene#isCompleted()} returns true or the
     * parent scene ends.
     * 
     * @see SceneManager#attach(Scene)
     */
    default void attach(Scene subScene) {
        getSceneManager().attach(subScene);
    }

    /**
     * Creates a sub-scene using the specified callback methods, then attaches
     * that sub-scene to the currently active scene. The sub-scene will remain
     * active until {@code completed} returns true or the parent scene ends.
     *
     * @see SceneManager#attach(Scene)
     * @see FluentScene
     */
    default void attach(Updatable onFrame, BooleanSupplier completed, Runnable onComplete) {
        FluentScene subScene = FluentScene.create()
            .withFrameHandler(onFrame)
            .withCompletionCheck(completed)
            .withCompletionHandler(onComplete);
        getSceneManager().attach(subScene);
    }

    /**
     * Attaches a sub-scene, based on the specified callback, to the currently
     * active scene. The sub-scene will remain active until the parent scene
     * ends.
     */
    default void attach(Updatable callback) {
        Scene subScene = (context, deltaTime) -> callback.update(deltaTime);
        attach(subScene);
    }

    /**
     * Attaches a sub-scene, based on the specified callback, to the currently
     * active scene. The sub-scene will remain active until the parent scene
     * ends.
     */
    default void attach(Runnable callback) {
        Scene subScene = (context, deltaTime) -> callback.run();
        attach(subScene);
    }

    /**
     * Attaches a sub-scene that processes an {@link EventQueue} using the
     * provided callback functions. This ensures asynchronous events are
     * processed during frame updates, instead of when they are received.
     */
    default <T> void attach(EventQueue<T> events, Consumer<T> onEvent, Consumer<Exception> onError) {
        attach(() -> events.flush(onEvent, onError));
    }

    /**
     * Attaches a sub-scene that processes an {@link EventQueue} during frame
     * updates. This returns a {@link Subject} that only publishes events
     * during frame updates, instead of publishing events whenever they arrive.
     */
    default <T> Subject<T> attach(EventQueue<T> eventQueue) {
        Subject<T> frameUpdateSubject = new Subject<>();
        attach(() -> eventQueue.flush(frameUpdateSubject::next, frameUpdateSubject::nextError));
        return frameUpdateSubject;
    }

    /**
     * Attaches a sub-scene that will update the timer during every frame
     * update, and then call the {@code onFrame} callback based on the
     * timer's value. Invokes the {@code onComplete} callback exactly once
     * when the timer has completed.
     */
    default void attachTimer(Timer timer, Consumer<Float> onFrame, Runnable onComplete) {
        FluentScene subScene = FluentScene.create()
            .withTimerHandler(timer, onFrame)
            .withCompletionHandler(onComplete);
        getSceneManager().attach(subScene);
    }

    /**
     * Attaches a sub-scene that will update the timer during every frame
     * update and will then invoke the specified callback function exactly
     * once when the timer has completed.
     */
    default void attachTimer(Timer timer, Runnable callback) {
        attach(timer, timer::isCompleted, callback);
    }

    /**
     * Attaches a sub-scene that will invoke the specified callback function
     * exactly once, after the specified delay.
     */
    default void attachTimer(float delay, Runnable callback) {
        attachTimer(new Timer(delay), callback);
    }

    /**
     * Attaches a sub-scene that will (A) update the timeline during every
     * frame update, (B) invoke the callback function with the timeline's
     * value. The sub-scene will run until the timeline is completed.
     */
    default void attachTimeline(Timeline timeline, Consumer<Float> callback, Runnable onComplete) {
        FluentScene subScene = FluentScene.create()
            .withTimelineHandler(timeline, callback)
            .withCompletionHandler(onComplete);
        getSceneManager().attach(subScene);
    }

    /**
     * Attaches a sub-scene that will (A) update the timeline during every
     * frame update, (B) invoke the callback function with the timeline's
     * value. The sub-scene will run until the timeline is completed.
     */
    default void attachTimeline(Timeline timeline, Consumer<Float> callback) {
        attachTimeline(timeline, callback, () -> {});
    }

    /**
     * Attaches a sub-scene that will invoke the specified callback function
     * when the specified graphics are clicked.
     * <p>
     * Note: This should be called "pointer released handler", since it also
     * supports touch events. The term "click handler" exists for historic
     * reasons.
     */
    default void attachClickHandler(StageNode2D node, Runnable callback) {
        attach(() -> {
            if (getInput().isPointerReleased(node)) {
                callback.run();
            }
        });
    }

    /**
     * Attaches a sub-scene that is <em>not</em> tied to the currently active
     * scene, and will remain active for as long as the application is active.
     * 
     * @see SceneManager#attachGlobalSubScene(Scene)
     */
    default void attachGlobalSubScene(Scene globalSubScene) {
        getSceneManager().attachGlobalSubScene(globalSubScene);
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
