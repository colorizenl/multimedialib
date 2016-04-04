//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import org.junit.Test;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.ImageData;
import nl.colorize.multimedialib.graphics.ImageRegion;

/**
 * Mock implementation of the {@code ImageData} interface.
 */
public class MockImageData implements ImageData {
	
	private int width;
	private int height;
	
	public MockImageData() {
		this.width = 128;
		this.height = 128;
	}

	public void setWidth(int width) {
		this.width = width;
	}
	
	public int getWidth() {
		return width;
	}

	public void setHeight(int height) {
		this.height = height;
	}
	
	public int getHeight() {
		return height;
	}

	public ImageData flip(boolean horizontal, boolean vertical) {
		return this;
	}
	
	public ImageData applyTint(ColorRGB tint) {
		return this;
	}
	
	public ImageRegion toRegion() {
		return ImageRegion.from(this);
	}
	
	@Test
	public void testGradle() {
		// Needed for Gradle bug GRADLE-1682.
	}
	
	public static MockImageData create(int width, int height) {
		MockImageData imageData = new MockImageData();
		imageData.setWidth(width);
		imageData.setHeight(height);
		return imageData;
	}
}
