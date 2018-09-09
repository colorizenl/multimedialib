//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.scene.Scene;

/**
 * Renders audiovisual data to create multimedia applications.
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
 * that the game uses. The way the canvas scales is determined by the scale strategy
 * that is being used by the renderer. Some scale strategies allow the canvas to be
 * resized with the screen, others do not. Depending on the scale strategy pixels
 * on the canvas may not directly correspond to pixels on the screen. Any pixel
 * coordinates used by the renderer are the coordinates on the canvas, not 
 * coordinates on the screen.
 * <p>
 * All drawing operations use X and Y coordinates that are relative to the canvas,
 * with the (0, 0) coordinate representing the top left corner. When drawing objects,
 * the X and Y coordinates indicate where the object's center will be drawn. The
 * Z coordinate (depth) is implicit: later drawing operations will draw over earlier
 * drawing operations.
 */
public interface Renderer {

    public void initialize();
    
    public void terminate();

    public int getCanvasWidth();

    public int getCanvasHeight();

    public ScaleStrategy getScaleStrategy();

    public int getTargetFramerate();

    public MediaLoader getMediaLoader();

    public RenderStats getStats();

    public void registerCallback(RenderCallback callback);

    public void unregisterCallback(RenderCallback callback);
}
