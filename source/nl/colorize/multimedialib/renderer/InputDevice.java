//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.math.Point;

/**
 * Used to poll the status of the platform's input devices. Depending on the
 * device, this may consist of a mouse, keyboard, or touch screen. Attempting
 * to access an input device that is not available will result in a
 * {@code UnsupportedOperationException}.
 */
public interface InputDevice {

    public Point getPointer();
    
    public boolean isPointerPressed();
    
    public boolean isPointerReleased();

    public boolean isKeyboardAvailable();

    public boolean isKeyPressed(KeyCode keyCode);

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
