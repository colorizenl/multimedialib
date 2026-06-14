//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
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
 */
@Getter
public class Timer implements Actor {

    private double position;
    private double duration;

    /**
     * Creates a new timer with the specified duration in seconds. A duration
     * of zero is allowed, and indicates the timer will immmediately be
     * considered as completed upon creation.
     *
     * @throws IllegalArgumentException for a negative timer duration.
     */
    public Timer(double duration) {
        Preconditions.checkArgument(duration >= 0f, "Invalid duration: " + duration);

        this.position = 0f;
        this.duration = duration;
    }

    @Override
    public void update(double deltaTime) {
        position = Math.min(position + deltaTime, duration);
    }

    public void setTime(double time) {
        this.position = Math.clamp(time, 0f, duration);
    }

    public double getTime() {
        return position;
    }

    public double getTimeRemaining() {
        return duration - position;
    }

    public boolean isCompleted() {
        return position >= duration;
    }

    public double getRatio() {
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

    public boolean isInfinite() {
        return duration == Double.MAX_VALUE;
    }

    @Override
    public String toString() {
        String result = TextUtils.numberFormat(position, 1);
        if (duration < Double.MAX_VALUE) {
            result += " / " + TextUtils.numberFormat(duration, 1);
        }
        return result;
    }

    /**
     * Factory method that creates a timer with the specified duration, which
     * has its position set to the specified value.
     */
    public static Timer at(double time, double duration) {
        Timer timer = new Timer(duration);
        timer.setTime(time);
        return timer;
    }

    /**
     * Factory method that creates a timer which has its position set to the
     * specified value. The timer will never reach its duration, similar to
     * {@link #infinite()}.
     */
    public static Timer at(double time) {
        return at(time, Double.MAX_VALUE);
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
        return new Timer(Double.MAX_VALUE);
    }

    /**
     * Factory method that creates a timer which starts in the completed state,
     * with the playhead set to the timer's duration.
     */
    public static Timer ended(double duration) {
        Timer timer = new Timer(duration);
        timer.end();
        return timer;
    }
}
