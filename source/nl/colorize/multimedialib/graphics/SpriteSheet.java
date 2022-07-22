//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Rect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Image that contains the graphics for multiple sprites by including them as
 * named regions witin the image.
 */
public class SpriteSheet {

    private Image image;
    private Map<String, Rect> regions;
    private Map<String, Image> subImageCache;

    public SpriteSheet(Image image) {
        this.image = image;
        this.regions = new HashMap<>();
        this.subImageCache = new HashMap<>();
    }

    /**
     * Marks a region within the sprite sheet and returns the corresponding
     * sun-image. The sub-image can also be retrieved at a later time using
     * {@link #get(String)}.
     *
     * @throws IllegalArgumentException if the region does not fit within
     *         the sprite sheet image, or if a region with the same name
     *         already exists.
     */
    public Image markRegion(String name, Rect region) {
        Preconditions.checkArgument(!regions.containsKey(name),
            "Sprite sheet already contains a region with the same name: " + name);
        Preconditions.checkArgument(isValidRegion(region),
            "Invalid region: " + name + " @ " + region);

        regions.put(name, region);
        return get(name);
    }

    protected boolean isValidRegion(Rect region) {
        if (image.getWidth() > 0 && image.getHeight() > 0) {
            Rect imageBounds = new Rect(0, 0, image.getWidth(), image.getHeight());
            return imageBounds.contains(region);
        } else {
            return true;
        }
    }

    /**
     * Returns the image for the marked region with the specified name.
     * @throws IllegalArgumentException if no such region has been marked.
     */
    public Image get(String name) {
        Preconditions.checkArgument(regions.containsKey(name), "Unknown region: " + name);

        if (subImageCache.containsKey(name)) {
            return subImageCache.get(name);
        } else {
            Rect region = getRegion(name);
            Image subImage = image.extractRegion(region);
            subImageCache.put(name, subImage);
            return subImage;
        }
    }

    /**
     * Returns a sequence of images that correspond to names regions.
     * @throws IllegalArgumentException if no such region has been marked.
     */
    public List<Image> get(String firstRegionName, String secondRegionName, String... rest) {
        List<Image> result = new ArrayList<>();
        result.add(get(firstRegionName));
        result.add(get(secondRegionName));
        for (String name : rest) {
            result.add(get(name));
        }
        return result;
    }

    public Rect getRegion(String name) {
        Rect region = regions.get(name);
        Preconditions.checkArgument(region != null, "Unknown region: " + name);
        return region;
    }

    public Set<String> getRegionNames() {
        return regions.keySet();
    }

    @Deprecated
    public Image getImage() {
        return image;
    }
}
