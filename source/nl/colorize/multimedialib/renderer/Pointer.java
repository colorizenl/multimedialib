//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.scene.Timer;
import nl.colorize.multimedialib.scene.Updatable;

/**
 * Represents a pointer device, which can be a mouse, a trackpad, or touch
 * controls, depending on the current platform and device.
 * <p>
 * Devices that support multi-touch will allow multiple pointers to be active
 * simultaneously. In such situations the render will provide access to
 * multiple {@code Pointer} instances that can be tracked individually, using
 * {@link #getId()} to identify each pointer.
 * <p>
 * {@link #getTimePressed()}
 */
@Getter
@Setter
public class Pointer implements Updatable {

    private final String id;
    private Point2D position;
    private int state;
    private final Timer pressedTimer;

    public static final int STATE_IDLE = 0;
    public static final int STATE_PRESSED = 1;
    public static final int STATE_RELEASED = 2;

    public Pointer(String id) {
        Preconditions.checkArgument(!id.isEmpty(), "Empty pointer ID");

        this.id = id;
        this.position = Point2D.ORIGIN;
        this.state = STATE_IDLE;
        this.pressedTimer = Timer.infinite();
    }

    /**
     * Returns true if this pointer is currently in the pressed state,
     * regardless of the pointer's current position.
     */
    public boolean isPressed() {
        return state == STATE_PRESSED;
    }

    /**
     * Returns true if this pointer is currently in the pressed state
     * <em>and</em> the pointer's position is currently located within the
     * specified area.
     */
    public boolean isPressed(Rect bounds) {
        return state == STATE_PRESSED && bounds.contains(position);
    }

    /**
     * Returns true if this pointer is currently in the released state,
     * regardless of the pointer's current position.
     */
    public boolean isReleased() {
        return state == STATE_RELEASED;
    }

    /**
     * Returns true if this pointer is currently in the released state
     * <em>and</em> the pointer's position is currently located within the
     * specified area.
     */
    public boolean isReleased(Rect bounds) {
        return state == STATE_RELEASED && bounds.contains(position);
    }

    /**
     * Returns the time this pointer has been pressed, in seconds. If this
     * pointer is in the pressed state, this will return the time since the
     * pressed state started. If this pointer is in the released state, this
     * returns the time between the pointer originally being pressed and it
     * being released. Returns zero if this pointer is in the idle state.
     */
    public float getTimePressed() {
        return pressedTimer.getTime();
    }

    /**
     * Resets the state of this pointer, so that it is no longer marked as
     * being pressed or released. This can be used to make certain logic
     * "consume" the pointer, without subsequent logic during the same frame
     * update also trying to consume the same pointer.
     */
    public void clearState() {
        state = STATE_IDLE;
        pressedTimer.reset();
    }

    @Override
    public void update(float deltaTime) {
        if (state == STATE_PRESSED || state == STATE_RELEASED) {
            pressedTimer.update(deltaTime);
        } else {
            pressedTimer.reset();
        }
    }
}
