//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

/**
 * Configures the renderer's display mode, which consists of both the canvas
 * resolution and the targeted framerate.
 */
public record DisplayMode(Canvas canvas, int framerate) {
}
