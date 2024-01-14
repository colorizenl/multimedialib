//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
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
 * Application code has little direct interaction with the {@link Renderer}
 * instance, apart from using {@link #start(Scene, ErrorHandler)} and
 * {@link #terminate()} to start and stop the animation loop. The application
 * will then receive a callback from the renderer during every frame update,
 * for as long as the animation loop is active. These callbacks have access to
 * the underlying renderer via the {@link SceneContext}. This explains why
 * renderer capabilities such as graphics and input devices cannot be accessed
 * directly from the {@link Renderer} instance, since they should only be
 * <p>
 * When it comes to display mode and screen resolution, the renderer has two
 * different concepts: the screen and the "canvas". The former refers to the
 * resolution at which the graphics are <em>displayed</em>, the latter refers
 * to the resolution at which the graphics are <em>rendered</em>. This allows
 * applications to support multiple resolutions that may be different from the
 * native screen resolution. The canvas is accessible to the application during
 * frame updates, via the aforementioned {@link SceneContext}.
 */
public interface Renderer {

    /**
     * Initializes this renderer and starts playing the requested scene.
     * Errors that occur during the application will be forwarded to the
     * specified error handler.
     * <p>
     * As explained in the class documentation, this is the only method in
     * {@link Renderer} that can be safely called from <em>outside</em> the
     * renderer's application loop.
     *
     * @throws IllegalStateException when calling this method even though the
     *         renderer has already been started.
     */
    public void start(Scene initialScene, ErrorHandler errorHandler);

    /**
     * Ends the animation loop, stops this renderer, and quits the application.
     * {@link Renderer} instances cannot be reused, restarting a previously
     * terminated renderer is not possible.
     *
     * @throws UnsupportedOperationException if this renderer does not support
     *         termination, for if the underlying platform does not support
     *         explicitly terminating applications.
     */
    public void terminate();

    public GraphicsMode getGraphicsMode();

    public DisplayMode getDisplayMode();
}
