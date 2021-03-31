//-----------------------------------------------------------------------------
// Ape Attack
// Copyright 2005-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import nl.colorize.multimedialib.renderer.GraphicsContext2D;

/**
 * Callback interface for drawing 2D graphics to the stage. The layer's graphics
 * will be drawn on top of the 3D graphics, if any, as well as all underlying
 * 2D graphics layers.
 */
public interface GraphicsLayer2D {

    /**
     * Callback method that exposes the render's graphics context, which can be
     * used to draw all graphics for this layer. This method is called during
     * every frame update as long as the layer's parent scene is active.
     */
    public void render(GraphicsContext2D graphics);
}
