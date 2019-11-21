//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for time-based behavior. Callback actions can be attached to
 * the timer, and will then be automatically called once the timer has been
 * completed.
 */
public class Timer implements Updatable {

    private float position;
    private float duration;
    private List<Runnable> actions;

    public Timer(float duration) {
        this.actions = new ArrayList<>();

        reset(duration);
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

    public boolean isCompleted() {
        return position >= duration;
    }

    public float getRatio() {
        return position / duration;
    }

    public void reset() {
        position = 0f;
    }

    public void reset(float duration) {
        Preconditions.checkArgument(duration >= 0f, "Invalid duration: " + duration);

        this.position = 0f;
        this.duration = duration;
    }

    public void attach(Runnable action) {
        actions.add(action);
    }

    /**
     * Returns a timer that will run indefinitely and will never complete.
     */
    public static Timer indefinite() {
        return new Timer(Float.MAX_VALUE);
    }
}
