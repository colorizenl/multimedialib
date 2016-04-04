//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

/**
 * Describes the contents of an image that was either loaded from a file, or
 * created programmatically. Image formats guranteed to be supported by all
 * renderer implementations are PNG and JPEG. 
 */
public interface ImageData {

	public int getWidth();
	
	public int getHeight();
	
	/**
	 * Creates an image containing the mirrored contents of this image.
	 */
	public ImageData flip(boolean horizontal, boolean vertical);

	/**
	 * Creates an image containing the contents of this image tinted with the
	 * specified color.
	 */
	public ImageData applyTint(ColorRGB tint);
}
