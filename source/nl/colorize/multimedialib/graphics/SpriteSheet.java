//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Rect;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Image that contains the graphics for multiple sprites by including them as
 * named regions witin the image.
 */
public class SpriteSheet {

    private Image image;
    private Map<String, Rect> regions;

    public SpriteSheet(Image image) {
        this.image = image;
        this.regions = new HashMap<>();
    }

    public void markRegion(String name, Rect region) {
        Preconditions.checkArgument(new Rect(0, 0, image.getWidth(), image.getHeight()).contains(region),
            "Invalid region: " + region);

        regions.put(name, region);
    }

    public Image get(String name) {
        return image.getRegion(getRegion(name));
    }

    public Rect getRegion(String name) {
        Rect region = regions.get(name);
        Preconditions.checkArgument(region != null, "Unknown region: " + region);
        return region;
    }

    public Set<String> getRegionNames() {
        return regions.keySet();
    }

    public Image getImage() {
        return image;
    }
}
