//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

/**
 * Callback interface for the renderer, that will receive events at various
 * points in the renderer's life cycle. Interaction with the renderer should
 * only take place from within these callback methods.
 * <p>
 * This callback interface is intended for low-level interaction with the
 * renderer. For most applications it will be sufficient to use
 * {@code Application}, which provides a more high-level application
 * structure on top of the callbacks.
 */
public interface RenderCallback {

    public void update(Renderer renderer, float deltaTime);

    public void render(Renderer renderer, GraphicsContext2D graphics);
}
