//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.action;

import nl.colorize.multimedialib.renderer.Updatable;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for time-based behavior. Callback actions can be attached to
 * the timer, and will then be automatically called once the timer has been
 * completed.
 */
public class Timer implements Action, Updatable {

    private float position;
    private float duration;
    private List<Runnable> actions;

    public Timer(float duration) {
        this.position = 0f;
        this.duration = duration;
        this.actions = new ArrayList<>();

        reset();
    }

    @Override
    public void update(float deltaTime) {
        if (isCompleted()) {
            return;
        }

        position = Math.min(position + deltaTime, duration);

        if (isCompleted()) {
            for (Runnable action : actions) {
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
     * Attaches a callback function that will be invoked when this timer has
     * completed.
     */
    public void attach(Runnable action) {
        actions.add(action);
    }

    public void clearActions() {
        actions.clear();
    }

    /**
     * Factory method that creates a timer and immediately attaches the specified
     * action that will be performed once the time has elapsed.
     */
    public static Timer create(float duration, Runnable action) {
        Timer timer = new Timer(duration);
        timer.attach(action);
        return timer;
    }

    /**
     * Returns a timer that will run indefinitely and will never complete.
     */
    public static Timer indefinite() {
        return new Timer(Float.MAX_VALUE);
    }
}
