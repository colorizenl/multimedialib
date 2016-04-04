//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.app;

import nl.colorize.multimedialib.graphics.DisplayList;

/**
 * Represents a discrete part of an application that is active for some period
 * of time.
 */
public interface Scene {

	public void onSceneStart(AnimationLoop animationLoop);
	
	/**
	 * Updates the contents of this scene. Called every frame, as close to the
	 * targeted framerate as possible, as long as the animation loop is running.
	 * @param deltaTime Time passed since the last frame upate, in seconds.
	 */
	public void onFrame(AnimationLoop animationLoop, float deltaTime);
	
	/**
	 * Sends the returned display list to the renderer so that it can be drawn.
	 * Called every frame while the animation loop is running, though it might
	 * be called at a different rate than {@link #onFrame(AnimationLoop, float)} 
	 * if the renderer has difficulty to meet the targeted framerate.
	 */
	public DisplayList onRender(AnimationLoop animationLoop);
	
	public void onSceneEnd(AnimationLoop animationLoop);
}
