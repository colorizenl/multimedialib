//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.scene.Renderable;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.Updatable;

/**
 * Renders audiovisual data to that can be used to display multimedia applications.
 * <p>
 * When started, the renderer will create the display system and then start the
 * animation loop. Frame updates will be scheduled to match the desired framerate,
 * although this might not be possible depending on the current platform and the
 * amount and complexity of graphics that are drawn.
 * <p>
 * All interaction with the renderer should be done from callbacks that are called
 * during the animation loop. The {@link Scene} interface splits the application
 * into different phases.
 * <p>
 * The renderer has two concepts of display size: the screen and the canvas. The
 * screen is the entire available drawing surface, excluding any title and status 
 * bars and borders. The screen size can change, for example when the window is 
 * resized or when the device changes orientation. The canvas is the drawing area 
 * that the game uses. Transitioning between these two sets of coordinates is
 * handled by the {@link Canvas}.
 */
public interface Renderer {

    public Canvas getCanvas();

    public InputDevice getInputDevice();

    public MediaLoader getMediaLoader();

    public ApplicationData getApplicationData(String appName);

    public void addUpdateCallback(Updatable callback);

    public void addRenderCallback(Renderable callback);
}
