//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.util.Cache;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.TextUtils;
import nl.colorize.util.Tuple;
import nl.colorize.util.TupleList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Packs a number of images into a larger image, with each image existing as
 * a named region. Using a sprite atlas generally provides better performance
 * for both loading and rendering images.
 * <p>
 * The sprite atlas contains both regular images and animations defined
 * <em>from</em> those images. Sprite atlases can be defined programmatically,
 * but they can also be loaded from text files using
 * {@link MediaLoader#loadAtlas(ResourceFile)}. MultimediaLib also includes
 * a command line tool for generating these text files during the build,
 * refer to the project's README file for details.
 * <p>
 * Although a sprite atlas is usually expected to consist of one large image,
 * this is not a hard requirement. This class allows the atlas to spread its
 * sub-images across multiple atlases, while still providing central access
 * to all sub-images across all atlas images.
 */
public class SpriteAtlas {

    private Map<String, SubImage> subImages;
    private Cache<String, Image> subImageCache;
    private Map<String, Animation> animations;

    public SpriteAtlas() {
        this.subImages = new HashMap<>();
        this.subImageCache = Cache.from(this::loadSubImage);
        this.animations = new HashMap<>();
    }

    private String normalizeSubImageName(String name) {
        name = TextUtils.removeTrailing(name, ".png");
        name = TextUtils.removeTrailing(name, ".jpg");
        name = TextUtils.removeTrailing(name, ".jpeg");
        return name;
    }

    public void add(String name, Image atlas, Region region) {
        name = normalizeSubImageName(name);

        Preconditions.checkArgument(!name.isEmpty(), "Invalid sub-image name: " + name);
        Preconditions.checkArgument(!subImages.containsKey(name), "Duplicate sub-image: " + name);

        SubImage subImage = new SubImage(name, atlas, region);
        subImages.put(name, subImage);
    }

    private void add(SubImage subImage) {
        add(subImage.name, subImage.atlas, subImage.region);
    }

    /**
     * Defines an animation based on sub-images within this sprite atlas. Every
     * frame in the animation consists of an image, which is obtained using
     * {@link #get(String)}, and a duration in seconds.
     *
     * @throws IllegalArgumentException if an animation with the same name
     *         already exists within this sprite atlas.
     * @throws IllegalStateException if the animation contains frames that
     *         do not exist within this sprite atlas.
     */
    public void addAnimation(String name, TupleList<String, Double> frames, boolean loop) {
        Animation animation = new Animation(loop);
        for (Tuple<String, Double> frame : frames) {
            animation.addFrame(get(frame.left()), frame.right());
        }
        addAnimation(name, animation);
    }

    private void addAnimation(String name, Animation animation) {
        Preconditions.checkArgument(!name.isEmpty(), "Invalid animation name: " + name);
        Preconditions.checkArgument(!animations.containsKey(name), "Duplicate animation: " + name);

        animations.put(name, animation);
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
        return get(_ -> true);
    }

    public Set<String> getSubImageNames() {
        return subImages.keySet();
    }

    public boolean contains(String name) {
        return subImages.containsKey(name);
    }

    /**
     * Returns the animation with the specified name. Any images used within
     * the animation will be cached for subsequent calls.
     *
     * @throws NoSuchElementException if the sprite atlas does not contain a
     *         animation with that name.
     */
    public Animation getAnimation(String name) {
        Animation animation = animations.get(name);
        if (animation == null) {
            throw new NoSuchElementException("No such animation: " + name);
        }
        return animation;
    }

    public Set<String> getAnimationNames() {
        return animations.keySet();
    }

    public boolean containsAnimation(String name) {
        return animations.containsKey(name);
    }

    /**
     * Returns a new sprite atlas with the same sub-images as this one,
     * but that uses the specified function to rename each sub-image. Any
     * animations already present in the sprite atlas will be preserved.
     */
    public SpriteAtlas rename(Function<String, String> nameFunction) {
        return rename(nameFunction, anim -> anim);
    }

    /**
     * Returns a new sprite atlas with the same sub-images and animations
     * as this one, but that uses the specified function to rename each
     * sub-image and each animation.
     */
    public SpriteAtlas rename(
        Function<String, String> nameFunction,
        Function<String, String> animFunction
    ) {
        SpriteAtlas result = new SpriteAtlas();
        for (SubImage subImage : subImages.values()) {
            String renamed = nameFunction.apply(subImage.name);
            result.add(new SubImage(renamed, subImage.atlas, subImage.region));
        }
        for (Map.Entry<String, Animation> entry : animations.entrySet()) {
            String renamed = animFunction.apply(entry.getKey());
            result.addAnimation(renamed, entry.getValue());
        }
        return result;
    }

    /**
     * Returns a new {@link SpriteAtlas} instance that only contains sub-images
     * and animations from this sprite atlas matching the specified predicate.
     *
     * @throws IllegalArgumentException if one of the animations relies on
     *         sub-images that are no longer present after filtering the
     *         sub-images using {@code imageFilter}.
     */
    public SpriteAtlas filter(Predicate<String> imageFilter, Predicate<String> animFilter) {
        SpriteAtlas result = new SpriteAtlas();

        for (Map.Entry<String, SubImage> entry : subImages.entrySet()) {
            if (imageFilter.test(entry.getKey())) {
                result.add(entry.getValue());
            }
        }

        for (Map.Entry<String, Animation> entry : animations.entrySet()) {
            if (animFilter.test(entry.getKey())) {
                result.addAnimation(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    /**
     * Returns a new {@link SpriteAtlas} instance that only contains animations
     * from this sprite atlas matching the specified predicate.
     */
    public SpriteAtlas filterAnimations(Predicate<String> animFilter) {
        return filter(_ -> false, animFilter);
    }

    /**
     * Creates a new sprite atlas that includes all sub-images from both this
     * atlas and the provided other atlas. Each sprite atlas can span multiple
     * images, as explained in the class documentation, so the combined sprite
     * atlas is transparent to the user.
     *
     * @throws IllegalArgumentException If the two atlases both include
     *         sub-images with the same name.
     */
    public SpriteAtlas merge(SpriteAtlas other) {
        SpriteAtlas merged = new SpriteAtlas();
        subImages.values().forEach(merged::add);
        other.subImages.values().forEach(merged::add);
        for (Map.Entry<String, Animation> entry : animations.entrySet()) {
            merged.addAnimation(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Animation> entry : other.animations.entrySet()) {
            merged.addAnimation(entry.getKey(), entry.getValue());
        }
        return merged;
    }

    /**
     * Returns a new sprite atlas containing the combined sub-images. The
     * sub-images are merged by calling {@link #merge(SpriteAtlas)} on
     * each atlas.
     *
     * @throws IllegalArgumentException If no atlases are provided, or if
     *         multiple atlases include sub-images with the same name.
     */
    public static SpriteAtlas merge(List<SpriteAtlas> atlases) {
        Preconditions.checkArgument(!atlases.isEmpty(), "No sprite atlas provided");

        SpriteAtlas result = atlases.getFirst();
        for (int i = 1; i < atlases.size(); i++) {
            result = result.merge(atlases.get(i));
        }

        return result;
    }

    /**
     * One of the sub-images in this sprite atlas. Since the atlas can span
     * multiple images, the entry also needs to include a reference to the
     * atlas image.
     */
    private record SubImage(String name, Image atlas, Region region) {
    }
}
