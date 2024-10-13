//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import lombok.Getter;
import nl.colorize.util.TextUtils;

/**
 * Utility class for time-based behavior. A timer consists of a position and
 * a duration. The position is moved during every frame update, until the
 * duration has been reached.
 * <p>
 * The timer is based on float precision, meaning the timer can run for
 * about 2.5-3 hours before float precision errors start to occur.
 */
@Getter
public class Timer implements Updatable {

    private float position;
    private float duration;

    /**
     * Creates a new timer with the specified duration in seconds. A duration
     * of zero is allowed, and indicates the timer will immmediately be
     * considered as completed upon creation.
     *
     * @throws IllegalArgumentException for a negative timer duration.
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

    public void setTime(float time) {
        this.position = Math.clamp(time, 0f, duration);
    }

    public float getTime() {
        return position;
    }

    public float getTimeRemaining() {
        return duration - position;
    }

    public boolean isCompleted() {
        return position >= duration;
    }

    public float getRatio() {
        if (duration == 0f) {
            return 0f;
        }
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
        String result = TextUtils.numberFormat(position, 1);
        if (duration < Float.MAX_VALUE) {
            result += " / " + TextUtils.numberFormat(duration, 1);
        }
        return result;
    }

    /**
     * Factory method that creates a timer with the specified duration, which
     * has its position set to the specified value.
     */
    public static Timer at(float time, float duration) {
        Timer timer = new Timer(duration);
        timer.setTime(time);
        return timer;
    }

    /**
     * Factory method that creates a timer which has its position set to the
     * specified value. The timer will never reach its duration, similar to
     * {@link #infinite()}.
     */
    public static Timer at(float time) {
        return at(time, Float.MAX_VALUE);
    }

    /**
     * Factory method that creates a timer with a zero duration. This will
     * immediately mark the timer as completed upon creation.
     */
    public static Timer none() {
        Timer timer = new Timer(0f);
        timer.end();
        return timer;
    }

    /**
     * Factory method that creates a timer which will never reach his duration,
     * meaning the timer will never be marked as completed.
     */
    public static Timer infinite() {
        return new Timer(Float.MAX_VALUE);
    }

    /**
     * Factory method that creates a timer which starts in the completed state,
     * with the playhead set to the timer's duration.
     */
    public static Timer ended(float duration) {
        Timer timer = new Timer(duration);
        timer.end();
        return timer;
    }
}
