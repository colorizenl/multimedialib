//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.graphics.Animation;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.PolygonMesh;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.multimedialib.renderer.MediaLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized loading and access for media assets. This is mainly useful for
 * larger applications, where the same assets are used across multiple scenes.
 * All assets are identified by string keys, which are assumed to be unique.
 */
public class MediaManager {

    private MediaLoader mediaLoader;
    private Map<String, Object> assets;

    public MediaManager(MediaLoader mediaLoader) {
        this.mediaLoader = mediaLoader;
        this.assets = new HashMap<>();
    }

    private <T> T store(String key, T asset) {
        if (assets.containsKey(key)) {
            throw new MediaException("Multiple media assets with the same key: " + key);
        }

        assets.put(key, asset);
        return asset;
    }

    public Image loadImage(String key, FilePointer file) {
        return store(key, mediaLoader.loadImage(file));
    }

    public Audio loadAudio(String key, FilePointer file) {
        return store(key, mediaLoader.loadAudio(file));
    }

    public TTFont loadFont(String key, FilePointer file, String family, int size,
            ColorRGB color, boolean bold) {
        return store(key, mediaLoader.loadFont(file, family, size, color, bold));
    }

    public PolygonMesh loadMesh(String key, FilePointer file) {
        return store(key, mediaLoader.loadMesh(file));
    }

    public String loadText(String key, FilePointer file) {
        return store(key, mediaLoader.loadText(file));
    }

    public void storeAnimation(String key, Animation anim) {
        store(key, anim);
    }

    public void storeSprite(String key, Sprite sprite) {
        store(key, sprite);
    }

    private Object get(String key) {
        Object asset = assets.get(key);
        if (asset == null) {
            throw new MediaException("Unknown media asset: " + key);
        }
        return asset;
    }

    public Image getImage(String key) {
        return (Image) get(key);
    }

    public Animation getAnimation(String key) {
        return (Animation) get(key);
    }

    public Sprite getSprite(String key) {
        Sprite sprite = (Sprite) get(key);
        return sprite.copy();
    }

    public Audio getAudio(String key) {
        return (Audio) get(key);
    }

    public TTFont getFont(String key) {
        return (TTFont) get(key);
    }

    public PolygonMesh getMesh(String key) {
        return (PolygonMesh) get(key);
    }

    public String getText(String key) {
        return (String) get(key);
    }

    public void unload(String key) {
        assets.remove(key);
    }

    public void unloadAll(String key) {
        assets.clear();
    }
}
