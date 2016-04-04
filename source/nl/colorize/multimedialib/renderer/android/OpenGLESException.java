//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.android;

import nl.colorize.multimedialib.renderer.RendererException;

/**
 * Indicates that a call to an OpenGL ES function has resulted in an error.
 */
public class OpenGLESException extends RendererException {

	public OpenGLESException(String message) {
		super(message);
	}
	
	public OpenGLESException(String message, Throwable cause) {
		super(message, cause);
	}
}
