//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.graphics.AudioData;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.ImageData;
import nl.colorize.multimedialib.graphics.ImageRegion;
import nl.colorize.multimedialib.graphics.Shape2D;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.Text;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.util.ResourceFile;

/**
 * Exposes the underlying platform so that multimedia applications can be displayed.
 * <p>
 * When started, the renderer will create the display system and then start the
 * animation loop. Frame updates will be scheduled to match the desired framerate,
 * although this might not be possible depending on the current platform and the
 * amount and complexity of graphics that are drawn.
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
 * Apart from displaying graphics and running the animation loop, the renderer also
 * exposes the platform-specific access to playing audio, input devices, and the 
 * file system.
 * <p>
 * The renderer typically operates in a separate thread, so that it can still
 * capture user input while the animation loop is active. Objects, should they 
 * wish to be notified while the renderer is active, can do so by implementing
 * the {@link RenderCallback} interface. 
 */
public interface Renderer {

	/**
	 * Creates the display system and starts the animation loop.
	 * @throws RendererException if the renderer is already active.
	 */
	public void initialize();
	
	/**
	 * Stops the animation loop and closes the display system. The renderer may
	 * finish the current frame before closing. Calling this method when the
	 * renderer has already stopped has no effect.
	 */
	public void stop();
	
	public boolean isActive();
	
	public void registerCallback(RenderCallback callback);
	
	public void unregisterCallback(RenderCallback callback);
		
	public int getCanvasWidth();
	
	public int getCanvasHeight();
	
	public Rect getCanvasBounds();
	
	public Rect getScreenBounds();
	
	public void setScaleStrategy(ScaleStrategy scaleStrategy);
	
	public ScaleStrategy getScaleStrategy();
	
	public void setTargetFramerate(int framerate);
	
	public int getTargetFramerate();
	
	public void setBackgroundColor(ColorRGB backgroundColor);
	
	public ColorRGB getBackgroundColor();
	
	public void drawImage(ImageData image, int x, int y, Transform transform);
	
	public void drawImageRegion(ImageRegion imageRegion, int x, int y, Transform transform);

	public void drawSprite(Sprite sprite);
	
	public void drawShape(Shape2D shape);
	
	public void drawText(Text text);
	
	/**
	 * Loads an image from one of the formats supported by this renderer.
	 * @throws RendererException if the renderer cannot load the image.
	 */
	public ImageData loadImage(ResourceFile source);
	
	/**
	 * Loads an audio clip from one of the formats supported by this renderer.
	 * @throws RendererException if the renderer cannot load the audio clip.
	 */
	public AudioData loadAudio(ResourceFile source);
	
	public InputDevice getInputDevice();
	
	public AudioQueue getAudioQueue();
	
	public RenderStatistics getStats();
}
