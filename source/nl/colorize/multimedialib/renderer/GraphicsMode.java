//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

/**
 * Indicates whether the renderer is capable of displaying 2D or 3D graphics.
 * Certain MultimediaLib features are only available when using a renderer
 * that is actually capable of displaying those features. This can be checked
 * at runtime using {@link Renderer#getGraphicsMode()}. Trying to use renderer
 * features on an unsupported platform will result in a
 * {@link UnsupportedGraphicsModeException}.
 */
public enum GraphicsMode {
    HEADLESS,
    MODE_2D,
    MODE_3D
}
