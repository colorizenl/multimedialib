//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.stage.StageVisitor;

import java.io.File;

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
 * Applications should only interact with the renderer from within this
 * animation loop. The only exceptions are {@link #start(Scene, ErrorHandler)}
 * and {@link #terminate()} which will respectively start and end the animation
 * loop. Then, during the animation loop, the {@link SceneContext} interface is
 * passed to the currently active scene, which allows that scene to interact
 * with the renderer.
 * <p>
 * When it comes to display mode and screen resolution, the renderer has two
 * different concepts: the screen and the "canvas". The former refers to the
 * resolution at which the graphics are <em>displayed</em>, the latter refers
 * to the resolution at which the graphics are <em>rendered</em>. This allows
 * applications to support multiple resolutions that may be different from the
 * native screen resolution.
 */
public interface Renderer {

    /**
     * Initializes this renderer and starts playing the requested scene. Errors
     * that occur during the application will be forwarded to the specified
     * error handler.
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
     *         termination, for example because the underlying platform does
     *         not support the concept of quitting applications.
     */
    public void terminate();

    public GraphicsMode getGraphicsMode();

    public DisplayMode getDisplayMode();

    default Canvas getCanvas() {
        return getDisplayMode().canvas();
    }

    public StageVisitor getGraphics();

    public InputDevice getInput();

    public MediaLoader getMediaLoader();

    public Network getNetwork();

    /**
     * Captures a screenshot of the renderer's currently displayed graphics,
     * then saves the screenshot to a PNG file.
     *
     * @throws UnsupportedOperationException if this renderer does not support
     *         taking screenshots at runtime, or does not support saving
     *         screenshots to files.
     */
    public void takeScreenshot(File outputFile);
}
