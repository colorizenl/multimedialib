//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.app;

import nl.colorize.multimedialib.mock.MockRenderer;
import nl.colorize.multimedialib.mock.MockScene;
import nl.colorize.multimedialib.renderer.Renderer;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for the {@code AnimationLoop} class.
 */
public class TestAnimationLoop {
	
	private Renderer renderer;

	@Before
	public void before() {
		renderer = new MockRenderer();
	}
	
	@Test
	public void testSceneLifecycle() {
		MockScene firstScene = new MockScene();
		MockScene secondScene = new MockScene();
		AnimationLoop loop = new AnimationLoop(renderer, firstScene);
		
		assertEquals(0, firstScene.getStartCount());
		
		loop.start();
		loop.onFrame(1f);
		loop.onFrame(1f);
		loop.changeScene(secondScene);
		loop.onFrame(1f);
		loop.changeScene(secondScene);
		
		assertEquals(1, firstScene.getStartCount());
		assertEquals(2, firstScene.getFrameCount());
		assertEquals(1, firstScene.getEndCount());
		assertEquals(1, secondScene.getStartCount());
		assertEquals(1, secondScene.getFrameCount());
		assertEquals(0, secondScene.getEndCount());
	}
	
	@Test
	public void testInitialSceneStartIsCalled() {
		MockScene initialScene = new MockScene();
		AnimationLoop loop = new AnimationLoop(renderer, initialScene);
		loop.start();
		
		assertEquals(1, initialScene.getStartCount());
	}
}
