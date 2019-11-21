//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
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
}
