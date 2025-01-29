//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Utility class to help with random numbers. This class uses a shared global
 * {@link Random} instance. This prevents situations where application code
 * creates {@link Random} instances across various locations, making it easier
 * to toggle between "real" random and deterministic pseudo-random.
 */
public class RandomGenerator {

    private static Random generator = new Random();

    private RandomGenerator() {
    }

    /**
     * Changes the random number generator used by this class to generate
     * deterministic pseudo-random numbers based on the specified seed value.
     */
    public static void seed(long value) {
        generator = new Random(value);
    }

    /**
     * Changes the random number generator used by this class to generate
     * "true" random numbers. This method can be used to revert the changes
     * made by using {@link #seed(long)}.
     */
    public static void randomSeed() {
        generator = new Random();
    }

    /**
     * Returns a random integer somewhere in the range between the minimum
     * (inclusive) and maximum (exclusive).
     */
    public static int getInt(int min, int max) {
        Preconditions.checkArgument(max >= min, "Invalid range: " + min + " - " + max);
        
        if (min == max) {
            return min;
        }

        return min + generator.nextInt(max - min);
    }

    /**
     * Returns a random float somewhere in the range between the minimum
     * (inclusive) and maximum (exclusive).
     */
    public static float getFloat(float min, float max) {
        Preconditions.checkArgument(max >= min, "Invalid range: " + min + " - " + max);
        
        if (min == max) {
            return min;
        }

        return min + generator.nextFloat() * (max - min);
    }

    /**
     * Produces a random float between 0.0 and 1.0, then compares that number
     * against {@code n} and returns the result. In other words, passing a value
     * of 0.9 against this method will have a 90% chance of returning true.
     */
    public static boolean chance(float n) {
        Preconditions.checkArgument(n >= 0f && n <= 1f, "Number out of range: " + n);

        float value = generator.nextFloat();
        return value <= n;
    }

    /**
     * Picks and returns a random element from the specified list.
     *
     * @throws IllegalArgumentException if the provided list is empty.
     */
    public static <T> T pick(List<T> elements) {
        Preconditions.checkArgument(!elements.isEmpty(), "Cannot pick from empty list");

        int index = getInt(0, elements.size());
        return elements.get(index);
    }
    
    /**
     * Picks and returns a random element from the specified set.
     * @throws IllegalArgumentException if the provided set is empty.
     */
    @SuppressWarnings("unchecked")
    public static <T> T pick(Set<T> elements) {
        Preconditions.checkArgument(!elements.isEmpty(), "Cannot pick from empty set");

        Object[] buffer = elements.toArray(new Object[0]);
        int index = getInt(0, buffer.length);
        return (T) buffer[index];
    }

    /**
     * Picks and returns a random element from the specified iterator.
     *
     * @throws IllegalArgumentException if the provided iterator is empty.
     */
    public static <T> T pick(Iterable<T> elements) {
        return pick(ImmutableList.copyOf(elements));
    }

    /**
     * Picks and returns a random element from the specified stream.
     *
     * @throws IllegalArgumentException if the provided stream is empty.
     */
    public static <T> T pick(Stream<T> elements) {
        return pick(elements.toList());
    }

    /**
     * Picks a random key from the specified map. The chance that one of the
     * elements will be chosen is indicated by the values in the map. So if two
     * keys have the same value, the chance that they will be picked is equally
     * likely. A value of 0 indicates the value will never be chosen.
     *
     * @throws IllegalArgumentException if the provided map is empty.
     */
    public static <T> T pick(Map<T, Integer> choices) {
        Preconditions.checkArgument(!choices.isEmpty(), "Cannot pick from empty map");

        List<T> elements = new ArrayList<>();

        for (T element : choices.keySet()) {
            for (int i = 0; i < choices.get(element); i++) {
                elements.add(element);
            }
        }

        return pick(elements);
    }

    /**
     * Returns a new list that contains the same elements as the original, but
     * with all elements shuffled to be in random order. This method is similar
     * to {@link Collections#shuffle(List)}, but creates a new list instead of
     * trying to modify the original one.
     */
    public static <T> List<T> shuffle(List<T> original) {
        List<T> shuffled = new ArrayList<>(original);
        if (original.size() >= 2) {
            Collections.shuffle(shuffled, generator);
        }
        return shuffled;
    }

    /**
     * Picks a random point somewhere within the specified rectangle.
     */
    public static Point2D pickPoint(Rect bounds) {
        float x = getFloat(bounds.x(), bounds.getEndX());
        float y = getFloat(bounds.y(), bounds.getEndY());
        return new Point2D(x, y);
    }
}
