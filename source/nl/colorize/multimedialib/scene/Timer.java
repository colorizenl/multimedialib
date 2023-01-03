//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.MathUtils;

/**
 * Utility class for time-based behavior. A timer consists of a position and a
 * duration. The position is moved during every frame update, until the duration
 * has been reached.
 */
public class Timer implements Updatable {

    private float position;
    private float duration;
    private Runnable action;

    /**
     * Creates a new timer with the specified duration in seconds, that will
     * perform the requested action when completed.
     */
    public Timer(float duration, Runnable action) {
        Preconditions.checkArgument(duration >= 0f, "Invalid duration: " + duration);

        this.position = 0f;
        this.duration = duration;
        this.action = action;
    }

    /**
     * Creates a new timer with the specified duration in seconds, with the
     * timer performing no action when completed.
     */
    public Timer(float duration) {
        this(duration, null);
    }

    @Override
    public void update(float deltaTime) {
        position = Math.min(position + deltaTime, duration);
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

    public void end() {
        position = duration;
    }

    @Override
    public String toString() {
        return MathUtils.format(position, 1) + " / " + MathUtils.format(duration, 1);
    }

    /**
     * Returns a no-op timer that has a zero duration.
     */
    public static Timer none() {
        Timer timer = new Timer(0f);
        timer.end();
        return timer;
    }
}
