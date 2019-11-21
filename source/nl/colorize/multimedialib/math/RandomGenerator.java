//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Random;

/**
 * Utility class to help with random numbers. This class should be used instead
 * of {@code Math.random()} and similar methods, as using class allows for "fake"
 * random numbers that ensure deterministic behavior.
 */
public class RandomGenerator {

    private Random generator;

    public RandomGenerator() {
        this.generator = new Random();
    }

    /**
     * Returns a random integer somewhere in the range between the minimum
     * (inclusive) and maximum (exclusive).
     */
    public int getInt(int min, int max) {
        Preconditions.checkArgument(max > min, "Invalid range: " + min + " - " + max);

        return min + generator.nextInt(max - min);
    }

    /**
     * Returns a random float somewhere in the range between the minimum
     * (inclusive) and maximum (exclusive).
     */
    public float getFloat(float min, float max) {
        Preconditions.checkArgument(max > min, "Invalid range: " + min + " - " + max);

        return min + generator.nextFloat() * (max - min);
    }

    /**
     * Produces a random float between 0.0 and 1.0, then compares that number
     * against {@code n} and returns the result. In other words, passing a value
     * of 0.9 against this method will have a 90% chance of returning true.
     */
    public boolean chance(float n) {
        Preconditions.checkArgument(n >= 0f && n <= 1f, "Number out of range: " + n);

        float value = generator.nextFloat();
        return n <= value;
    }

    /**
     * Picks and returns a random element from the specified list.
     * @throws IllegalArgumentException if the provided list is empty.
     */
    public <T> T pick(List<T> elements) {
        Preconditions.checkArgument(!elements.isEmpty(), "Cannot pick from empty list");

        int index = getInt(0, elements.size());
        return elements.get(index);
    }
}
