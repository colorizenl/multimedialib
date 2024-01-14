//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;

/**
 * Represents a pointer device, which can be a mouse, a trackpad, or touch
 * controls, depending on the current platform and device.
 * <p>
 * Devices that support multi-touch will allow multiple pointers to be active
 * simultaneously. In such situations the render will provide access to
 * multiple {@code Pointer} instances that can be tracked individually, using
 * {@link #getId()} to identify each pointer.
 */
@Getter
@Setter
public class Pointer {

    private String id;
    private Point2D position;
    private int state;

    public static final int STATE_IDLE = 0;
    public static final int STATE_PRESSED = 1;
    public static final int STATE_RELEASED = 2;

    public Pointer(String id) {
        Preconditions.checkArgument(!id.isEmpty(), "Empty pointer ID");

        this.id = id;
        this.position = Point2D.ORIGIN;
        this.state = STATE_IDLE;
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
}
