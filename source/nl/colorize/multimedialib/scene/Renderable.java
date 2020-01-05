//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.renderer.GraphicsContext;

/**
 * Interface for all objects that render graphics during the animation loop.
 */
@FunctionalInterface
public interface Renderable {

    public void render(GraphicsContext graphics);
}
