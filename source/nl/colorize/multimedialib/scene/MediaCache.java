//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.PolygonMesh;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.MediaLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Wrapper around a {@code MediaLoader} that caches all media, that when the
 * same file is multiple times the results are retrieved from the cache.
 *
 * @deprecated The functionality of this class has been replaced by the
 *             {@link MediaManager}, which serves a similar purpose, but
 *             allows to identify media by name instead of the underlying
 *             files(s).
 */
@Deprecated
public class MediaCache implements MediaLoader {

    private MediaLoader delegate;
    private Map<String, Object> cache;

    public MediaCache(MediaLoader delegate) {
        Preconditions.checkArgument(!(delegate instanceof MediaCache),
            "Cannot use nested instances of MediaCache");

        this.delegate = delegate;
        this.cache = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    private <T> T get(String cacheKey, Supplier<T> mediaSupplier) {
        T result = (T) cache.get(cacheKey);
        if (result == null) {
            result = mediaSupplier.get();
            cache.put(cacheKey, result);
        }
        return result;
    }

    private <T> T get(FilePointer file, Supplier<T> mediaSupplier) {
        return get(file.getPath(), mediaSupplier);
    }

    @Override
    public Image loadImage(FilePointer file) {
        return get(file, () -> delegate.loadImage(file));
    }

    @Override
    public Audio loadAudio(FilePointer file) {
        return get(file, () -> delegate.loadAudio(file));
    }

    @Override
    public TTFont loadFont(FilePointer file, String family, int size, ColorRGB color, boolean bold) {
        // Some renderer implementations need to reload the font
        // for each size/color combination.
        String key = file.getPath() + "@" + size + "@" + color + "@" + bold;
        return get(key, () -> delegate.loadFont(file, family, size, color, bold));
    }

    @Override
    public String loadText(FilePointer file) {
        return get(file, () -> delegate.loadText(file));
    }

    @Override
    public PolygonMesh loadMesh(FilePointer file) {
        return get(file, () -> delegate.loadMesh(file));
    }

    @Override
    public boolean containsResourceFile(FilePointer file) {
        return delegate.containsResourceFile(file);
    }

    public void invalidate() {
        cache.clear();
    }
}
