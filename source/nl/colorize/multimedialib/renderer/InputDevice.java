//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.math.Point;

/**
 * The platform's method for user input, such as a keyboard, mouse, or touch 
 * screen.
 * <p>
 * As different platforms will have different input devices, this interface only
 * guarantees the availability of a pointer device, which will be either a mouse
 * or a touch screen.
 * <p>
 * Non-pointer input devices can be accessed through implementing classes, but
 * are not guaranteed to be available on all platforms.
 */
public interface InputDevice {

	public Point getPointer();
	
	public boolean isPointerPressed();
	
	public boolean isPointerReleased();
}
