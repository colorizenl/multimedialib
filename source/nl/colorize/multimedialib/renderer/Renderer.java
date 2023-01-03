//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.stage.StageVisitor;

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
 * The renderer has two concepts of display size: the screen and the canvas. This
 * difference exists to allow applications to support multiple resolutions that
 * may be different from the native screen resolution. The resolution at which
 * the graphics are rendered is referred to as the canvas.
 */
public interface Renderer {

    /**
     * Initializes this renderer and starts playing the requested scene. Errors
     * that occur during the application will be forwarded to the specified
     * error handler.
     * <p>
     * Renderer implementations may run the applications in a separate renderer
     * thread. Application logic should therefore be located in the scene, and
     * not rely on accessing this {@link Renderer} instance from the original
     * thread.
     */
    public void start(Scene initialScene, ErrorHandler errorHandler);

    public GraphicsMode getGraphicsMode();

    public DisplayMode getDisplayMode();

    public StageVisitor accessGraphics();

    public InputDevice accessInputDevice();

    public MediaLoader accessMediaLoader();

    public Network accessNetwork();

    /**
     * Takes a screenshots of the renderer's current graphics, and saves it to
     * an image. The image is returned as a data URL for a PNG image.
     */
    public String takeScreenshot();
}
