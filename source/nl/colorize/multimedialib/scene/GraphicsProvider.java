//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.stage.Spatial2D;

/**
 * Provides graphics for the currently active scene. This allows the same
 * object to contain scene logic and graphics by implementing the
 * {@link Actor} interface and this interface, respectively.
 */
@FunctionalInterface
public interface GraphicsProvider {

    /**
     * Initializes the graphics provided by this object. This method is
     * idempotent, calling it multiple times will keep returning the same
     * graphics.
     */
    public Spatial2D getGraphics();
}
