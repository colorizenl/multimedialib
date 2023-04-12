//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.stage.StageVisitor;

/**
 * Central access point for the renderer's underlying capabilities.
 */
public record RenderCapabilities(
    GraphicsMode graphicsMode,
    DisplayMode displayMode,
    StageVisitor graphics,
    InputDevice inputDevice,
    MediaLoader mediaLoader,
    Network network) {

    public Canvas getCanvas() {
        return displayMode.canvas();
    }
}
