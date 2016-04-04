//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

/**
 * Callback interface for objects that wish to be notified while the renderer
 * is active. Callback methods are always invoked from thread that is running
 * the animation loop.
 */
public interface RenderCallback {

	/**
	 * Invoked immediately after the renderer is started and before it renders
	 * the first frame.
	 */
	public void onInitialized();
	
	public void onFrame(float deltaTime);
	
	public void onStopped();
	
	/**
	 * Returns true for as long as this callback wishes to be notified of frame
	 * updates.
	 */
	public boolean isActive();
}
