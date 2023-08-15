//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;

/**
 * Current state of a pointer device for user input. Depending on the current
 * device, the pointer can represent a mouse, a trackpad, or touch input.
 * <p>
 * Devices that support multi-touch input will allow multiple pointers to be
 * active simultaneously. The pointer ID can be used to differentiate each
 * pointer in such cases.
 */
@Getter
@Setter
public class Pointer {

    private String id;
    private Point2D position;
    private boolean pressed;
    private boolean released;

    public Pointer(String id, Point2D position) {
        Preconditions.checkArgument(!id.isEmpty(), "Missing pointer ID");

        this.id = id;
        this.position = position;
        this.pressed = false;
        this.released = false;
    }

    public Pointer(String id) {
        this(id, new Point2D(0, 0));
    }

    public boolean isPressed(Rect bounds) {
        return pressed && bounds.contains(position);
    }

    public boolean isReleased(Rect bounds) {
        return released && bounds.contains(position);
    }
}
