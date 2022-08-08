//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

/**
 * Lists the available TeaVM renderer implementations. Although TeaVM provides
 * the general framework for browser-based renderers, it requires JavaScript
 * libraries in order to actually render graphics.
 */
public enum WebGraphics {
    CANVAS,
    PIXI,
    THREE
}
