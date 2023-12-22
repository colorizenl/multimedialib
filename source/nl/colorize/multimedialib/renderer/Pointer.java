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
 * controls depending on the current platform and device.
 * <p>
 * Devices that support multi-touch will allow multiple pointers to be active
 * simultaneously. In such situations, the pointer {@code id} can be used to
 * differentiate between different pointers.
 */
@Getter
@Setter
public class Pointer {

    private String id;
    private Point2D position;
    private boolean pressed;
    private boolean released;

    public Pointer(String id) {
        Preconditions.checkArgument(!id.isEmpty(), "Empty pointer ID");

        this.id = id;
        this.position = Point2D.ORIGIN;
        this.pressed = false;
        this.released = false;
    }

    /**
     * Convenience method that returns true if this pointer is currently in
     * the pressed state <em>and</em> currently located within the specified
     * bounds.
     */
    public boolean isPressed(Rect bounds) {
        return pressed && bounds.contains(position);
    }

    /**
     * Convenience method that returns true if this pointer is currently in
     * the released state <em>and</em> currently located within the specified
     * bounds.
     */
    public boolean isReleased(Rect bounds) {
        return released && bounds.contains(position);
    }
}
