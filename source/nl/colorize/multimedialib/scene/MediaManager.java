//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.Animation;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.PolygonModel;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.MediaLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Centralized loading and access for media assets. This is mainly useful for
 * larger applications, where the same assets are used across multiple scenes.
 * Though all media assets still identified by their source file, they are
 * only loaded the first time, and additional attempts to load the asset will
 * return a cached version instead of reloading it.
 */
public class MediaManager {

    private MediaLoader mediaLoader;
    private Map<String, Object> assets;

    public MediaManager(MediaLoader mediaLoader) {
        this.mediaLoader = mediaLoader;
        this.assets = new HashMap<>();
    }

    private <T> T access(String key, Supplier<T> loader) {
        if (assets.containsKey(key)) {
            return (T) assets.get(key);
        } else {
            T asset = loader.get();
            assets.put(key, asset);
            return asset;
        }
    }

    private <T> T access(FilePointer key, Supplier<T> loader) {
        return access(key.getPath(), loader);
    }

    public Image getImage(FilePointer file) {
        return access(file, () -> mediaLoader.loadImage(file));
    }

    public Audio getAudio(FilePointer file) {
        return access(file, () -> mediaLoader.loadAudio(file));
    }

    public TTFont loadFont(FilePointer file, String family, int size, ColorRGB color, boolean bold) {
        String key = file.getPath() + "@" + size + "@" + color + "@" + bold;
        return access(key, () -> mediaLoader.loadFont(file, family, size, color, bold));
    }

    public PolygonModel getModel(FilePointer file) {
        return access(file, () -> mediaLoader.loadModel(file));
    }

    public String getText(FilePointer file) {
        return access(file, () -> mediaLoader.loadText(file));
    }

    public void storeAnimation(String key, Animation anim) {
        assets.put(key, anim);
    }

    public Animation getAnimation(String key) {
        Preconditions.checkArgument(assets.containsKey(key), "No such animation: " + key);
        return (Animation) assets.get(key);
    }

    public void storeSprite(String key, Sprite sprite) {
        assets.put(key, sprite);
    }

    public Sprite getSprite(String key) {
        Preconditions.checkArgument(assets.containsKey(key), "No such sprite: " + key);
        return (Sprite) assets.get(key);
    }

    public void unload(FilePointer file) {
        assets.remove(file.getPath());
    }

    public void unloadAll() {
        assets.clear();
    }
}
