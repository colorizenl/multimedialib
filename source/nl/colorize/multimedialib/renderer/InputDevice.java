//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;

import java.util.List;

/**
 * Used to poll the status of the platform's input devices. Depending on the
 * device, this may consist of a mouse, keyboard, or touch screen. Attempting
 * to access an input device that is not available will result in a
 * {@code UnsupportedOperationException}.
 * <p>
 * Mouse, touch, and keyboard events all use the concepts of "pressed" and
 * "released". They are considered "pressed" during the entire lifespan of the
 * event. In contrast, the "released" state is only active for a single frame
 * after the event has ended.
 */
public interface InputDevice {

    /**
     * Returns the current locations of all currently active pointers. When using
     * a mouse, this will always return a list with a single element. For touch
     * controls, the list could also have multiple elements (during multi-touch
     * gestures) or the list could be empty (unlike mouse events, touch coordinates
     * are only available during the event itself).
     */
    public List<Point2D> getPointers();

    /**
     * Returns true if any of the currently active pointers have been pressed and
     * located within the specified area during the current frame.
     */
    public boolean isPointerPressed(Rect area);

    /**
     * Returns true if any of the currently active pointers were released and
     * located within the specified area during the current frame.
     */
    public boolean isPointerReleased(Rect area);

    /**
     * Returns if the current device supports touch input. If true, the values
     * returned by {@link #getPointers()}, {@link #isPointerPressed(Rect)}, and
     * {@link #isPointerReleased(Rect)} will be based on touch input. If false,
     * they will be based on mouse or trackpad input.
     */
    public boolean isTouchAvailable();

    /**
     * Returns true if the current device has a hardware keyboard.
     */
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
     * Shows a dialog window requesting the user to enter text. This method
     * exists only because text fields, unlike other input elements such as
     * buttons, cannot be emulated by the renderer without losing common
     * functionality such as copy/paste. Text input must therefore be delegated
     * to the platform so that a native text field can be used.
     *
     * @deprecated Although this method is necessary for the reasons outlined
     *             above, it does lead to user experience issues due to the mix
     *             of user interface elements provided by the renderer and those
     *             from the native platform.
     */
    @Deprecated
    public String requestTextInput(String label, String initialValue);
}
