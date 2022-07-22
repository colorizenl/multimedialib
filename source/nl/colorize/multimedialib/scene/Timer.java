//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for time-based behavior. At first glance there is some overlap
 * between timers and {@code Timeline}s, but they target different scenarios:
 * timelines are based around keyframe animation and interpolation. Timelines
 * also allow the playhead to be moved and/or loop. Timers are more simple, the
 * playhead will simply move forward every frame until the timer's duration has
 * been reached.
 */
public class Timer implements Updatable {

    private float position;
    private float duration;
    private List<Updatable> frameUpdateActions;
    private List<Runnable> completionActions;

    public Timer(float duration) {
        Preconditions.checkArgument(duration >= 0f, "Invalid duration: " + duration);

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

    public boolean isCompleted() {
        return position >= duration;
    }

    public float getRatio() {
        return position / duration;
    }

    public void reset() {
        position = 0f;
    }

    /**
     * Ends this timer by moving the playhead to the end of the timeline.
     *
     * @deprecated Use {@link #complete(boolean)} instead.
     */
    @Deprecated
    public void end() {
        complete(false);
    }

    /**
     * Ends this timer by moving the playhead to the end of the timeline. Using
     * this method will *not* fire the normal completion actions unless
     * {@code fireActions} is set to true.
     */
    public void complete(boolean fireActions) {
        position = duration;

        if (fireActions) {
            for (Runnable action : completionActions) {
                action.run();
            }
        }
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
