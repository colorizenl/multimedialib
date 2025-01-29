//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;

/**
 * The renderer acts as the entry point from the application to the underlying
 * platform, managing the animation loop, graphics, audio, and input.
 * <p>
 * When started, the renderer will create the display system and then start the
 * animation loop. Frame updates will be scheduled to match the targeted
 * framerate. Each frame update consists of executing application logic for the
 * current <em>scene</em>, after which the contents of the <em>stage</em> are
 * rendered.
 * <p>
 * Application code does not interact with the {@link Renderer} directly.
 * Instead, the currently active scene is given access to the renderer in
 * callback methods via the {@link SceneContext}.
 */
public interface Renderer {

    /**
     * Starts this renderer based on the specified configuration. The renderer
     * will initially display the specified scene.
     * <p>
     * This method can be called from any thread. The renderer will run in
     * its own thread, which is started by calling this method. All callback
     * methods will be called from the renderer thread.
     *
     * @throws UnsupportedOperationException if this renderer is unable to
     *         support the requested configuration.
     */
    public void start(RenderConfig config, Scene initialScene);

    public boolean isSupported(GraphicsMode graphicsMode);
}
