//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import org.junit.Test;

import nl.colorize.multimedialib.graphics.AudioData;
import nl.colorize.multimedialib.graphics.ImageData;
import nl.colorize.multimedialib.graphics.ImageRegion;
import nl.colorize.multimedialib.graphics.Shape2D;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.AbstractRenderer;
import nl.colorize.multimedialib.renderer.AudioQueue;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.RenderCallback;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.util.ResourceFile;

/**
 * Mock implementation of the {@code Renderer} interface.
 */
public class MockRenderer extends AbstractRenderer {

	private Rect screenBounds;

	public MockRenderer() {
		super(ScaleStrategy.flexible(640, 480), 25);
		screenBounds = new Rect(0, 0, 1280, 800);
	}
	
	protected void startRenderer() {
		for (RenderCallback callback : callbacks) {
			callback.onInitialized();
		}
	}
	
	protected void stopRenderer() {
		for (RenderCallback callback : callbacks) {
			callback.onStopped();
		}
	}
	
	public void doFrame(float deltaTime) {
		for (RenderCallback callback : callbacks) {
			callback.onFrame(deltaTime);
		}
	}

	public void setScreenBounds(Rect screenBounds) {
		this.screenBounds = screenBounds;
	}
	
	public Rect getScreenBounds() {
		return screenBounds;
	}
	
	public void drawImage(ImageData image, int x, int y, Transform transform) {
	}
	
	public void drawImageRegion(ImageRegion imageRegion, int x, int y, Transform transform) {
	}

	public void drawShape(Shape2D shape) {
	}
	
	public ImageData loadImage(ResourceFile source) {
		return new MockImageData();
	}

	@Override
	public AudioData loadAudio(ResourceFile source) {
		return null;
	}
	
	public InputDevice getInputDevice() {
		//TODO
		return null;
	}
	
	public AudioQueue getAudioQueue() {
		//TODO
		return null;
	}

	@Override
	protected void onDisplayChanged() {
	}
	
	@Test
	public void testGradle() {
		// Needed for Gradle bug GRADLE-1682.
	}
}
