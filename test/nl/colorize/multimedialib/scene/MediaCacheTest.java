//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.java2d.StandardMediaLoader;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class MediaCacheTest {

    @Test
    public void testLoadFromCache() {
        AtomicInteger count = new AtomicInteger(0);
        MediaLoader countingMediaLoader = new StandardMediaLoader() {
            @Override
            public String loadText(FilePointer file) {
                count.incrementAndGet();
                return "test";
            }
        };

        MediaCache cache = new MediaCache(countingMediaLoader);
        cache.loadText(null);
        cache.loadText(null);

        assertEquals(1, count.get());
    }

    @Test
    public void testInvalidateCache() {
        AtomicInteger count = new AtomicInteger(0);
        MediaLoader countingMediaLoader = new StandardMediaLoader() {
            @Override
            public String loadText(FilePointer file) {
                count.incrementAndGet();
                return "test";
            }
        };

        MediaCache cache = new MediaCache(countingMediaLoader);
        cache.loadText(null);
        cache.invalidate();
        cache.loadText(null);

        assertEquals(2, count.get());
    }
}
