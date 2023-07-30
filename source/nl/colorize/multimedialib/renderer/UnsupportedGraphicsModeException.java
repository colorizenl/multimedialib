//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

/**
 * Thrown when attempting to perform an operation which is not supported by
 * the renderer's graphics mode.
 */
public class UnsupportedGraphicsModeException extends RuntimeException {

    public UnsupportedGraphicsModeException(String message) {
        super(message);
    }

    public UnsupportedGraphicsModeException() {
        this("Graphics mode not supported");
    }
}
