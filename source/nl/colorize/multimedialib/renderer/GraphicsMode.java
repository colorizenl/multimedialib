//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

/**
 * Describes the type of graphics that can be displayed by the renderer. Some
 * renderers can operate in both 2D and 3D mode, and can be configured to
 * indicate which should be active.
 */
public enum GraphicsMode {
    MODE_2D,
    MODE_3D,
    HEADLESS;
}
