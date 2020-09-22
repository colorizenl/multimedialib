//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

/**
 * Callback interface for objects that draw 2D graphics. The callback is invoked
 * by the renderer during every frame update.
 */
@FunctionalInterface
public interface Drawable {

    public void render(GraphicsContext2D graphics);
}
