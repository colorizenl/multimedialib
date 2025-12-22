//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.util.animation.Timeline;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Implementation of the {@link Scene} interface that delegates to callback
 * functions. This class is intended for dynamically creating sub-scenes.
 * It is possible to add <em>multiple</em> callbacks, which are then invoked
 * in sequence during frame updates.
 */
public class FluentScene implements Scene {

    private Updatable onFrame;
    private BooleanSupplier completed;
    private Runnable onComplete;

    private FluentScene() {
        this.onFrame = _ -> {};
        this.onComplete = () -> {};
    }

    public FluentScene withFrameHandler(Updatable callback) {
        Updatable existingCallback = onFrame;
        onFrame = deltaTime -> {
            existingCallback.update(deltaTime);
            callback.update(deltaTime);
        };
        return this;
    }

    public FluentScene withTimerHandler(Timer timer, Consumer<Float> callback) {
        Updatable frameHandler = deltaTime -> {
            timer.update(deltaTime);
            callback.accept(timer.getPosition());
        };

        withFrameHandler(frameHandler);
        withCompletionCheck(timer::isCompleted);
        return this;
    }

    public FluentScene withTimelineHandler(Timeline timeline, Consumer<Float> callback) {
        Updatable frameHandler = deltaTime -> {
            timeline.movePlayhead(deltaTime);
            callback.accept(timeline.getValue());
        };

        withFrameHandler(frameHandler);
        withCompletionCheck(() -> timeline.isCompleted() && !timeline.isLoop());
        return this;
    }

    public FluentScene withCompletionCheck(BooleanSupplier callback) {
        if (completed == null) {
            completed = callback;
        } else {
            BooleanSupplier existingCallback = completed;
            completed = () -> existingCallback.getAsBoolean() && callback.getAsBoolean();
        }
        return this;
    }

    public FluentScene withCompletionHandler(Runnable callback) {
        Runnable existingCallback = onComplete;
        onComplete = () -> {
            existingCallback.run();
            callback.run();
        };
        return this;
    }

    @Override
    public void update(SceneContext context, float deltaTime) {
        onFrame.update(deltaTime);
    }

    @Override
    public void end(SceneContext context) {
        onComplete.run();
    }

    @Override
    public boolean isCompleted() {
        return completed != null && completed.getAsBoolean();
    }

    /**
     * Static factory method that creates a new, empty {@link FluentScene}
     * instance. No callbacks will be registered initially, they need to be
     * added using the various {@code withX} methods.
     */
    public static FluentScene create() {
        return new FluentScene();
    }
}
