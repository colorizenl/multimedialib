//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import nl.colorize.multimedialib.math.Rect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A large image that contains a number of sub-images, marked as a region in the
 * large image and identified by name. Sub-image regions are allowed to overlap,
 * the same pixels can be included in multiple sub-images. However, sub-image
 * names are required to be unique.
 */
public class ImageAtlas {

    private Image sourceImage;
    private Map<String, Image> subImages;
    private Map<String, Rect> subImageBounds;
    
    public ImageAtlas(Image sourceImage) {
        this.sourceImage = sourceImage;
        this.subImages = new HashMap<>();
        this.subImageBounds = new HashMap<>();
    }
    
    public void markSubImage(String name, Rect region) {
        Preconditions.checkArgument(!subImages.containsKey(name), "Sub-image already exists: " + name);

        subImages.put(name, sourceImage.getRegion(region));
        subImageBounds.put(name, region);
    }
    
    public Image getSubImage(String name) {
        Image subImage = subImages.get(name);
        Preconditions.checkArgument(subImage != null, "Sub-image not found: " + name);
        return subImage;
    }

    public Rect getSubImageBounds(String name) {
        Rect bounds = subImageBounds.get(name);
        Preconditions.checkArgument(bounds != null, "Sub-image not found: " + name);
        return bounds;
    }

    public boolean containsSubImage(String name) {
        return subImages.containsKey(name);
    }

    public Map<String, Image> getSubImages() {
        return ImmutableMap.copyOf(subImages);
    }

    public List<Image> getSubImages(List<String> names) {
        List<Image> subImages = new ArrayList<>();
        for (String name : names) {
            subImages.add(getSubImage(name));
        }
        return subImages;
    }

    public List<Image> getSubImages(String... names) {
        return getSubImages(ImmutableList.copyOf(names));
    }

    public Image getSourceImage() {
        return sourceImage;
    }
}
