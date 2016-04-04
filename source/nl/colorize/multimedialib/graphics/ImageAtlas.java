//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import nl.colorize.multimedialib.math.Rect;

/**
 * A large image that contains a number of sub-images, which are named rectangular
 * regions within the large image. It is generally faster to load the single atlas
 * image than loading all sub-images separately. Sub-image regions are allowed to
 * overlap, the same pixels can be included in multiple sub-images.
 */
public class ImageAtlas {

	private ImageData atlas;
	private Map<String, ImageRegion> subImages;
	
	public ImageAtlas(ImageData atlas) {
		this.atlas = atlas;
		this.subImages = new HashMap<String, ImageRegion>();
	}
	
	public void markSubImage(String name, Rect region) {
		if (subImages.containsKey(name)) {
			throw new IllegalArgumentException("Sub-image with name already exists: " + name);
		}
		subImages.put(name, ImageRegion.from(atlas, region));
	}
	
	public ImageRegion getSubImage(String name) {
		if (!subImages.containsKey(name)) {
			throw new IllegalArgumentException("No sub-image with name exists: " + name);
		}
		return subImages.get(name);
	}
	
	public List<ImageRegion> getSubImages(List<String> names) {
		List<ImageRegion> subImages = new ArrayList<ImageRegion>();
		for (String name : names) {
			subImages.add(getSubImage(name));
		}
		return subImages;
	}
	
	public List<ImageRegion> getSubImages(String... names) {
		return getSubImages(ImmutableList.copyOf(names));
	}
	
	public Set<String> getSubImageNames() {
		return subImages.keySet();
	}
	
	public boolean containsSubImage(String name) {
		return subImages.containsKey(name);
	}
	
	public ImageData getAtlas() {
		return atlas;
	}
}
