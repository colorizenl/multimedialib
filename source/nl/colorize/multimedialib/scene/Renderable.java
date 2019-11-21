//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
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
