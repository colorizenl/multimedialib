//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.collect.Streams;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.scene.Updatable;
import nl.colorize.util.Subject;

/**
 * Used to poll the status of the platform's input devices. This includes
 * keyboard, mouse, and touch controls, though the available input devices
 * depend on the platform and device. MultimediaLib applications are
 * frame-based, so input devices can be checked by polling.
 */
public interface InputDevice extends Updatable {

    /**
     * Returns all currently active pointers. Depending on the current platform
     * and device, pointers could be based on mouse input, a trackpad, or touch
     * controls.
     * <p>
     * Note that the type of pointer will also influence its behavior. The
     * mouse pointer is always included in this list, since the mouse cursor is
     * always visible. Touch pointer are only available during the touch, as
     * the pointer disappears once the touch event has ended.
     */
    public Iterable<Pointer> getPointers();

    /**
     * Convenience method that returns true if <em>any</em> of the pointer
     * devices is currently pressed and located within the specified bounds.
     */
    default boolean isPointerPressed(Rect bounds) {
        return Streams.stream(getPointers())
            .anyMatch(pointer -> pointer.isPressed(bounds));
    }

    /**
     * Convenience method that returns true if <em>any</em> of the pointer
     * devices has been released within the specified bounds.
     */
    default boolean isPointerReleased(Rect bounds) {
        return Streams.stream(getPointers())
            .anyMatch(pointer -> pointer.isReleased(bounds));
    }

    /**
     * Clears all pointer state for all currently active pointers. Using this
     * method is the equivalent of using {@link Pointer#clearState()} on all
     * pointers.
     *
     * @deprecated Prefer using {@link Pointer#clearState()} on individual
     *             pointers over trying to globally manage <em>all</em>
     *             pointers using this method.
     */
    @Deprecated
    public void clearPointerState();

    public boolean isTouchAvailable();

    public boolean isKeyboardAvailable();

    /**
     * Returns true if the key with the specified key code was pressed during
     * the current frame.
     */
    public boolean isKeyPressed(KeyCode keyCode);

    /**
     * Returns true if the key with the specified key code was released during
     * the current frame.
     */
    public boolean isKeyReleased(KeyCode keyCode);

    /**
     * Shows a dialog window with a text input field. The dialog window is not
     * part of the scene, it uses the platform's native user interface.
     */
    public Subject<String> requestTextInput(String label, String initialValue);

    /**
     * Copies the specified text to the system clipboard.
     */
    public void fillClipboard(String text);
}
