//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheTest {

    @Test
    void useLoader() {
        Cache<Integer, String> cache = Cache.create(key -> "" + key, 10);

        assertEquals("1", cache.get(1));
        assertEquals("2", cache.get(2));
    }

    @Test
    void evictOldEntries() {
        Cache<Integer, String> cache = Cache.create(key -> "" + key, 3);
        cache.get(1);
        cache.get(2);

        assertTrue(cache.isCached(1));
        assertTrue(cache.isCached(2));
        assertFalse(cache.isCached(3));
        assertFalse(cache.isCached(4));

        cache.get(3);
        cache.get(4);

        assertFalse(cache.isCached(1));
        assertTrue(cache.isCached(2));
        assertTrue(cache.isCached(3));
        assertTrue(cache.isCached(4));
    }
}
