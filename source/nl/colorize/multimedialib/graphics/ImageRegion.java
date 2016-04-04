//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import nl.colorize.multimedialib.math.Rect;

/**
 * Describes a rectangular area of pixels within an image, which can be either
 * the entire image or part of it.
 */
public class ImageRegion {

	private ImageData image;
	private Rect region;
	
	private ImageRegion(ImageData image, Rect region) {
		this.image = image;
		this.region = region;
	}
	
	public ImageData getImage() {
		return image;
	}
	
	public Rect getRegion() {
		return region;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof ImageRegion) {
			ImageRegion other = (ImageRegion) o;
			return image.equals(other.image) && region.equals(other.region);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return region.hashCode();
	}
	
	public static ImageRegion from(ImageData image) {
		return from(image, new Rect(0, 0, image.getWidth(), image.getHeight())); 
	}
	
	public static ImageRegion from(ImageData image, Rect region) {
		if (region.getX() < 0 || region.getY() < 0 || region.getEndX() > image.getWidth() || 
				region.getEndY() > image.getHeight()) {
			throw new IllegalArgumentException("Region out-of-bounds: " + region);
		}
		
		return new ImageRegion(image, region);
	}
}
