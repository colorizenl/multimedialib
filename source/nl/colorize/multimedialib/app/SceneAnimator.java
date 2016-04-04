//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.app;

import nl.colorize.util.animation.Animatable;
import nl.colorize.util.animation.Animator;

/**
 * Animates objects within the currently active scene. All animations that are
 * started are tied to the scene. This means that if the scene ends or pauses,
 * so will all active animations. 
 */
public class SceneAnimator extends Animator implements Animatable {
	
	private boolean active;

	public void start() {
		active = true;
	}

	public void stop() {
		active = false;
	}

	public void onFrame(float deltaTime) {
		doFrameUpdate(deltaTime);
	}
	
	@Override
	protected boolean isCurrentlyActive(Animatable anim) {
		return active;
	}
	
	public void setActiveScene(Scene scene) {
		cancelAll();
	}
}
