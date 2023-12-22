//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

/**
 * Indicates an error that has caused the renderer to terminate the animation
 * loop. {@link RendererException} should only be thrown by the renderer
 * itself, it is not intended to be used during application logic.
 */
public class RendererException extends RuntimeException {

    public RendererException(String message) {
        super(message);
    }
}
