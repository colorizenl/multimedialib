//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import org.junit.Test;

import nl.colorize.multimedialib.graphics.StandardGraphic;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Shape;

/**
 * Mock implementation of the {@code Graphic} interface.
 */
public class MockGraphic extends StandardGraphic {
	
	private Shape bounds;
	
	public MockGraphic() {
		bounds = new Rect(0, 0, 128, 128);
	}
	
	public void setBounds(Shape bounds) {
		this.bounds = bounds;
	}

	public Shape getBounds() {
		return bounds;
	}
	
	@Test
	public void testGradle() {
		// Needed for Gradle bug GRADLE-1682.
	}
}
