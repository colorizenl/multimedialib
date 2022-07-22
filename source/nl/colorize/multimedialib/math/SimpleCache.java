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
public class SimpleCache<K, V> {

    private LinkedList<K> keys;
    private Map<K, V> entries;
    private Function<K, V> loader;
    private int capacity;

    private SimpleCache(Function<K, V> loader, int capacity) {
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

    public static <K, V> SimpleCache<K, V> create(Function<K, V> loader, int capacity) {
        return new SimpleCache<K, V>(loader, capacity);
    }
}
