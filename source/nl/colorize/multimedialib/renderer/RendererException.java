//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

/**
 * Exception that is thrown by the renderer when it is unable to continue the
 * execution of the current frame in the animation loop.
 */
public class RendererException extends RuntimeException {

    public RendererException(String message) {
        super(message);
    }
    
    public RendererException(String message, Throwable cause) {
        super(message, cause);
    }
}
