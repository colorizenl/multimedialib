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
 * Implementation of an {@link Actor} that is created out of callback
 * functions.
 */
public class FluentActor implements Actor {

    private Actor onFrame;
    private BooleanSupplier completed;
    private Runnable onComplete;
    private boolean terminated;

    private FluentActor() {
        this.onFrame = _ -> {};
        this.onComplete = () -> {};
        this.terminated = false;
    }

    public FluentActor withFrameHandler(Actor callback) {
        Actor existingCallback = onFrame;
        onFrame = deltaTime -> {
            existingCallback.update(deltaTime);
            callback.update(deltaTime);
        };
        return this;
    }

    public FluentActor withTimerHandler(Timer timer, Consumer<Double> callback) {
        Actor frameHandler = deltaTime -> {
            timer.update(deltaTime);
            callback.accept(timer.getPosition());
        };

        withFrameHandler(frameHandler);
        withCompletionCheck(timer::isCompleted);
        return this;
    }

    public FluentActor withTimelineHandler(Timeline timeline, Consumer<Double> callback) {
        Actor frameHandler = deltaTime -> {
            timeline.movePlayhead(deltaTime);
            callback.accept(timeline.getValue());
        };

        withFrameHandler(frameHandler);
        withCompletionCheck(() -> timeline.isCompleted() && !timeline.isLoop());
        return this;
    }

    public FluentActor withCompletionCheck(BooleanSupplier callback) {
        if (completed == null) {
            completed = callback;
        } else {
            BooleanSupplier existingCallback = completed;
            completed = () -> existingCallback.getAsBoolean() && callback.getAsBoolean();
        }
        return this;
    }

    public FluentActor withCompletionHandler(Runnable callback) {
        Runnable existingCallback = onComplete;
        onComplete = () -> {
            existingCallback.run();
            callback.run();
        };
        return this;
    }

    @Override
    public void update(double deltaTime) {
        onFrame.update(deltaTime);

        // Make sure we have a chance to run the completion
        // handler if this turns out to be the last frame
        // this actor is going to be around.
        isCompleted();
    }

    @Override
    public boolean isCompleted() {
        if (completed == null || !completed.getAsBoolean()) {
            return false;
        }

        if (!terminated) {
            onComplete.run();
            terminated = true;
        }

        return true;
    }

    /**
     * Static factory method that creates a new, empty {@link FluentActor}
     * instance. No callbacks will be registered initially, they need to be
     * added using the various {@code withX} methods.
     */
    public static FluentActor create() {
        return new FluentActor();
    }
}
