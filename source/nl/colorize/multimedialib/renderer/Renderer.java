//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.scene.Scene;

/**
 * The renderer acts as the entry point for accessing platform-specific
 * behavior such as running the animation loop, displaying 2D and 3D graphics,
 * playing audio, and capturing user input from input devices.
 * <p>
 * When started, the renderer will create the display system and then start the
 * animation loop. Frame updates will be scheduled to match the desired framerate,
 * although this might not be possible depending on the current platform and the
 * amount and complexity of graphics that are drawn.
 * <p>
 * For 3D graphics, the renderer will draw all objects that are part of the
 * <em>stage</em>. Note that some renderer implementations may be limited to 2D
 * graphics and do not support 3D graphics.
 * <p>
 * For 2D graphics, the renderer will draw a number of layers. If 2D and 3D
 * graphicd are mixed, all 2D graphics are drawn on top of the 3D graphics.
 * <p>
 * The renderer has two concepts of display size: the screen and the canvas. This
 * difference exists to allow applications to support multiple resolutions that
 * may be different from the native screen resolution. The resolution at which
 * the graphics are rendered is referred to as the canvas.
 */
public interface Renderer {

    public void start(Scene initialScene);

    public DisplayMode getDisplayMode();

    /**
     * Takes a screenshots of the renderer's current graphics, and saves it to
     * an image. The image is returned as a data URL for a PNG image.
     */
    public String takeScreenshot();
}
