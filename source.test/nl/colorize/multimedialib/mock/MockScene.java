//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import nl.colorize.multimedialib.app.AnimationLoop;
import nl.colorize.multimedialib.app.Scene;
import nl.colorize.multimedialib.graphics.DisplayList;

/**
 * Mock implementation of the {@code Scene} interface.
 */
public class MockScene implements Scene {
	
	private DisplayList displayList;
	
	private AtomicInteger startCount;
	private AtomicInteger endCount;
	private AtomicInteger frameCount;
	
	public MockScene() {
		displayList = new DisplayList();
		
		startCount = new AtomicInteger(0);
		endCount = new AtomicInteger(0);
		frameCount = new AtomicInteger(0);
	}

	public void onSceneStart(AnimationLoop animationLoop) {
		startCount.incrementAndGet();
	}

	public void onFrame(AnimationLoop animationLoop, float deltaTime) {
		frameCount.incrementAndGet();
	}

	public DisplayList onRender(AnimationLoop animationLoop) {
		return displayList;
	}

	public void onSceneEnd(AnimationLoop animationLoop) {
		endCount.incrementAndGet();
	}
	
	public DisplayList getDisplayList() {
		return displayList;
	}
	
	public int getStartCount() {
		return startCount.get();
	}
	
	public int getEndCount() {
		return endCount.get();
	}
	
	public int getFrameCount() {
		return frameCount.get();
	}
	
	@Test
	public void testGradle() {
		// Needed for Gradle bug GRADLE-1682.
	}
}
