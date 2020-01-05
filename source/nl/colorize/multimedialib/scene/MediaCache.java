//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.MediaLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Wrapper around {@link MediaLoader} that caches all loaded media files. If
 * the same file is requested multiple times, the version from the cache is
 * returned. Obviously, it is also possible to just load the file once and
 * then use the reference, but in practice this tends to be occasionally
 * forgotten, so automated caching will lead to better performance for such
 * situations.
 */
public class MediaCache implements MediaLoader {

    private MediaLoader delegate;
    private Map<FilePointer, Object> cache;

    protected MediaCache(MediaLoader delegate) {
        Preconditions.checkArgument(!(delegate instanceof MediaCache),
            "Cannot use nested instances of MediaCache");
        this.delegate = delegate;
        this.cache = new HashMap<>();
    }

    private <T> T get(FilePointer file, Supplier<T> mediaSupplier) {
        T result = (T) cache.get(file);
        if (result == null) {
            result = mediaSupplier.get();
            cache.put(file, result);
        }
        return result;
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
    public TTFont loadFont(String fontFamily, FilePointer file) {
        return get(file, () -> delegate.loadFont(fontFamily, file));
    }

    @Override
    public String loadText(FilePointer file) {
        return get(file, () -> delegate.loadText(file));
    }

    @Override
    public boolean containsResourceFile(FilePointer file) {
        return delegate.containsResourceFile(file);
    }

    public void invalidate() {
        cache.clear();
    }
}
