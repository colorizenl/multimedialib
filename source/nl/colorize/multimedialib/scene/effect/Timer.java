//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import nl.colorize.multimedialib.scene.Agent;
import nl.colorize.multimedialib.scene.Updatable;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for time-based behavior. Callback actions can be attached to
 * the timer, and will then be automatically called once the timer has been
 * completed.
 */
public class Timer implements Agent {

    private float position;
    private float duration;
    private List<Updatable> frameUpdateActions;
    private List<Runnable> completionActions;

    public Timer(float duration) {
        this.position = 0f;
        this.duration = duration;
        this.frameUpdateActions = new ArrayList<>();
        this.completionActions = new ArrayList<>();

        reset();
    }

    @Override
    public void update(float deltaTime) {
        if (isCompleted()) {
            return;
        }

        position = Math.min(position + deltaTime, duration);

        for (Updatable action : frameUpdateActions) {
            action.update(deltaTime);
        }

        if (isCompleted()) {
            for (Runnable action : completionActions) {
                action.run();
            }
        }
    }

    public float getTime() {
        return position;
    }

    public float getDuration() {
        return duration;
    }

    @Override
    public boolean isCompleted() {
        return position >= duration;
    }

    public float getRatio() {
        return position / duration;
    }

    public void reset() {
        position = 0f;
    }

    public void end() {
        position = duration;
    }

    /**
     * Attaches a callback function that will be invoked during every frame
     * update until this timer has completed.
     */
    public void attachFrameUpdate(Updatable action) {
        frameUpdateActions.add(action);
    }

    /**
     * Attaches a callback function that will be invoked when this timer has
     * completed.
     */
    public void attachCompletion(Runnable action) {
        completionActions.add(action);
    }

    /**
     * Attaches a callback function that will be invoked when this timer has
     * completed.
     *
     * @deprecated Use {@link #attachCompletion(Runnable)} instead.
     */
    @Deprecated
    public void attach(Runnable action) {
        attachCompletion(action);
    }

    /**
     * Factory method that creates a timer and immediately attaches the specified
     * action that will be performed once the time has elapsed.
     */
    public static Timer create(float duration, Runnable completion) {
        Timer timer = new Timer(duration);
        timer.attachCompletion(completion);
        return timer;
    }

    /**
     * Factory method that creates a timer and attached the specified actions
     * to be performed during frame updates and upon completion.
     */
    public static Timer create(float duration, Updatable frameUpdate, Runnable completion) {
        Timer timer = new Timer(duration);
        timer.attachFrameUpdate(frameUpdate);
        timer.attachCompletion(completion);
        return timer;
    }

    /**
     * Returns a timer that will run indefinitely and will never complete.
     */
    public static Timer indefinite() {
        return new Timer(Float.MAX_VALUE);
    }

    /**
     * Returns a timer that has a duration of zero and is therefore always and
     * permanently considered completed.
     */
    public static Timer completed() {
        return new Timer(0f);
    }
}
