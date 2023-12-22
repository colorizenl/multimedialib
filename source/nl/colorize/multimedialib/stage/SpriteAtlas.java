//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.util.TextUtils;
import nl.colorize.util.stats.Cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Packs a number of images into a larger image, with each image existing as
 * a named region. Using a sprite atlas generally provides better performance
 * for both loading and rendering images.
 * <p>
 * Although a sprite atlas is usually expected to consist of one large image,
 * this is not a hard requirement. This class allows the atlas to spread its
 * sub-images across multiple atlases, while still providing central access
 * to all sub-images across all atlas images.
 */
public class SpriteAtlas {

    private Map<String, SubImage> subImages;
    private Cache<String, Image> subImageCache;

    public SpriteAtlas() {
        this.subImages = new HashMap<>();
        this.subImageCache = Cache.from(this::loadSubImage, 1024);
    }

    private String normalizeSubImageName(String name) {
        name = TextUtils.removeTrailing(name, ".png");
        name = TextUtils.removeTrailing(name, ".jpg");
        name = TextUtils.removeTrailing(name, ".jpeg");
        return name;
    }

    public void add(String name, Image atlas, Region region) {
        name = normalizeSubImageName(name);

        Preconditions.checkArgument(!subImages.containsKey(name),
            "Duplicate sub-image: " + name);

        Preconditions.checkArgument(subImages.size() < subImageCache.getCapacity(),
            "Maximum number of sub-images exceeded");

        SubImage subImage = new SubImage(name, atlas, region);
        subImages.put(name, subImage);
    }

    private void add(SubImage subImage) {
        add(subImage.name, subImage.atlas, subImage.region);
    }

    /**
     * Returns the sub-image with the specified name. If the same sub-image is
     * requested multiple times, the image data will be cached and returned on
     * subsequent requests.
     *
     * @throws NoSuchElementException if the sprite atlas does not contain a
     *         sub-image with that name.
     */
    public Image get(String name) {
        name = normalizeSubImageName(name);
        return subImageCache.get(name);
    }

    private Image loadSubImage(String name) {
        SubImage subImage = subImages.get(name);
        if (subImage == null) {
            throw new NoSuchElementException("No such sub-image: " + name);
        }
        return subImage.atlas.extractRegion(subImage.region);
    }

    /**
     * Returns a list of sub-images that match the requested names. This is a
     * bulk version of {@link #get(String)}.
     */
    public List<Image> get(List<String> names) {
        return names.stream()
            .map(this::get)
            .toList();
    }

    /**
     * Returns all sub-images that match the predicate. The sub-images will be
     * returned in alphabetical order based on their name.
     */
    public List<Image> get(Predicate<String> filter) {
        return subImages.keySet().stream()
            .filter(filter)
            .sorted()
            .map(this::get)
            .toList();
    }

    /**
     * Returns all sub-images in this sprite atlas. The sub-images will be
     * returned in alphabetical order based on their name.
     */
    public List<Image> getAll() {
        return get(name -> true);
    }

    public boolean contains(String name) {
        return subImages.containsKey(name);
    }

    /**
     * Creates a new sprite atlas that includes all sub-images from both this
     * atlas and the provided other atlas. Each sprite atlas can span multiple
     * images, as explained in the class documentation, so the combined sprite
     * atlas is transparent to the user.
     *
     * @throws IllegalArgumentException If the two atlases do not include
     *         sub-images with the same name.
     */
    public SpriteAtlas merge(SpriteAtlas other) {
        SpriteAtlas merged = new SpriteAtlas();
        subImages.values().forEach(merged::add);
        other.subImages.values().forEach(merged::add);
        return merged;
    }

    /**
     * One of the sub-images in this sprite atlas. Since the atlas can span
     * multiple images, the entry also needs to include a reference to the
     * atlas image.
     */
    private record SubImage(String name, Image atlas, Region region) {

        public SubImage {
            Preconditions.checkArgument(!name.isEmpty(), "Invalid sub-image name: " + name);
        }
    }
}
