//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;

/**
 * Basic cache implementation that simply removes the oldest entry if the
 * maximum size has been exceeded.
 * <p>
 * This class is in no way comparable to Google Guava's {@code CacheBuilder}
 * implementation. It is provided for environments with a limited Java standard
 * library, where Guava's cache is not fully available.
 */
public class Cache<K, V> {

    private LinkedList<K> keys;
    private Map<K, V> entries;
    private Function<K, V> loader;
    private int capacity;

    public Cache(Function<K, V> loader, int capacity) {
        Preconditions.checkArgument(capacity >= 1, "Invalid cache capacity");

        this.keys = new LinkedList<>();
        this.entries = new HashMap<>();
        this.loader = loader;
        this.capacity = capacity;
    }

    public V get(K key) {
        if (entries.containsKey(key)) {
            return entries.get(key);
        } else {
            V value = loader.apply(key);
            entries.put(key, value);
            keys.add(key);

            if (keys.size() > capacity) {
                K evicted = keys.removeFirst();
                entries.remove(evicted);
            }

            return value;
        }
    }

    @VisibleForTesting
    protected boolean isCached(K key) {
        return entries.containsKey(key);
    }

    public void invalidate(K key) {
        entries.remove(key);
    }

    public void invalidateAll() {
        entries.clear();
    }

    /**
     * Creates a new cache with a limited capacity. If the cache grows beyond
     * this size, the oldest entries are removed in order to make space.
     */
    public static <K, V> Cache<K, V> create(Function<K, V> loader, int capacity) {
        return new Cache<>(loader, capacity);
    }

    /**
     * Creates a new cache with unlimited capacity. Entries are never removed,
     * the cache will continue to grow until the application is out of memory.
     */
    public static <K, V> Cache<K, V> createUnlimited(Function<K, V> loader) {
        return new Cache<>(loader, Integer.MAX_VALUE);
    }
}
