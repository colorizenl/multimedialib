//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.scene.Updatable;

import java.util.Optional;

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
public interface InputDevice extends Updatable {

    /**
     * Returns the pointer device's current location relative to the canvas.
     * If the platform uses a mouse or trackpad, this will always return a
     * result based on the current location of the cursor. If the platform
     * uses touch controls, this will only return a result if touch events
     * are currently in progress.
     */
    public Optional<Point2D> getPointer();

    /**
     * Returns true if any of the currently active pointers have been pressed
     * and located within the specified area during the current frame.
     */
    public boolean isPointerPressed(Rect area);

    /**
     * Returns true if any of the currently active pointers were pressed,
     * regardless of their location.
     */
    public boolean isPointerPressed();

    /**
     * Returns true if any of the currently active pointers were released and
     * located within the specified area during the current frame.
     */
    public boolean isPointerReleased(Rect area);

    /**
     * Returns true if any of the currently active pointers were released,
     * regardless of their location.
     */
    public boolean isPointerReleased();

    /**
     * Clears the state of the pointer, so that the pointer released event
     * cannot propagate to other event handlers that might run during the
     * same frame.
     *
     * @deprecated Scenes should not have direct access to the underlying
     *             renderer state. The need for this method will be replaced
     *             by the ability to stop propagation for pointer events in
     *             a future version of MultimediaLib.
     */
    @Deprecated
    public void clearPointerReleased();

    /**
     * Returns if the current device supports touch input. If true, the values
     * returned by {@link #getPointer()}, {@link #isPointerPressed(Rect)}, and
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
