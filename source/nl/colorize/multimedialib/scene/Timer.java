//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import nl.colorize.util.TextUtils;

/**
 * Utility class for time-based behavior. A timer consists of a position and a
 * duration. The position is moved during every frame update, until the duration
 * has been reached.
 */
public class Timer implements Updatable {

    private float position;
    private float duration;

    /**
     * Creates a new timer with the specified duration in seconds.
     */
    public Timer(float duration) {
        Preconditions.checkArgument(duration >= 0f, "Invalid duration: " + duration);

        this.position = 0f;
        this.duration = duration;
    }

    @Override
    public void update(float deltaTime) {
        position = Math.min(position + deltaTime, duration);
    }

    public float getTime() {
        return position;
    }

    public float getTimeRemaining() {
        return duration - position;
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
        return TextUtils.numberFormat(position, 1) + " / " + TextUtils.numberFormat(duration, 1);
    }

    /**
     * Factory method that creates a no-op timer with a zero duration.
     */
    public static Timer none() {
        Timer timer = new Timer(0f);
        timer.end();
        return timer;
    }

    /**
     * Factory method that creates a timer which will never reach his duration.
     */
    public static Timer infinite() {
        return new Timer(Float.MAX_VALUE);
    }

    /**
     * Factory method that creates a timer which starts in the ended state,
     * and needs to be reset before it can be used.
     */
    public static Timer ended(float duration) {
        Timer timer = new Timer(duration);
        timer.end();
        return timer;
    }
}
