//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import nl.colorize.multimedialib.math.Shape;

/**
 * Marker interface for objects that can be drawn by the renderer. Graphics are
 * drawn at x and y coordinates that represent the horizontal and vertical position
 * of the graphic's center, with the (0, 0) coordinate being in the top-left corner 
 * of the canvas. The graphic's z index represents the "depth" of the graphic, where
 * graphics with a higher z index will overlap graphics with a lower z index.   
 */
public interface Graphic {

	public int getX();
	
	public int getY();
	
	public int getZIndex();
	
	/**
	 * Returns the transformation that should be displayed to this graphic before
	 * it is displayed. Returns {@code null} if this graphic cannot be transformed.
	 */
	public Transform getTransform();

	/**
	 * Returns a shape that completely contains this graphic. The bounding shape 
	 * can be used to perform non-pixel perfect collision detection.
	 */
	public Shape getBounds();
}
