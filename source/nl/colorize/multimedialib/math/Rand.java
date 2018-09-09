//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import java.util.List;
import java.util.Random;

/**
 * Utility class for working with random numbers. This class uses an instance
 * of {@link java.util.Random} internally, but adds a number of convenience
 * methods on top of it.
 */
public final class Rand {
    
    private static final Random RANDOM = new Random();

    private Rand() {
    }
    
    /**
     * Returns an integer between 0 and {@code upper} (exclusive).
     */
    public static int nextInt(int upper) {
        return nextInt(0, upper);
    }
    
    /**
     * Returns a random integer between {@code lower} (inclusive) and
     * {@code upper} (exclusive). 
     */
    public static int nextInt(int lower, int upper) {
        int range = upper - lower;
        return RANDOM.nextInt(range) + lower;
    }
    
    /**
     * Returns a random float between 0 (inclusive) and 1 (exclusive).
     */
    public static float nextFloat() {
        return RANDOM.nextFloat();
    }
    
    /**
     * Returns a random float between {@code lower} (inclusive) and
     * {@code upper} (exclusive). 
     */
    public static float nextFloat(float lower, float upper) {
        float range = upper - lower;
        return RANDOM.nextFloat() * range + lower;
    }
    
    public static boolean nextBoolean() {
        return RANDOM.nextBoolean(); 
    }
    
    /**
     * Generates a random number and compares it to a threshold value. The
     * threshold is a number between 0 and 1. For example, if the argument is
     * 0.7 this method has a 70 percent chance of returning true.
     * @throws IllegalArgumentException if {@code f} is outside the range 0..1.
     */
    public static boolean chance(float f) {
        if (f < 0f || f > 1f) {
            throw new IllegalArgumentException("Argument out of range 0..1: " + f);
        }
        // > instead of >= to prevent passing 0 sometimes returning
        // true. Passing 1 will never return false because Random
        // is exclusive.
        return f > RANDOM.nextFloat();
    }
    
    /**
     * Returns a random element from a list.
     * @throws IllegalArgumentException if the list is empty.
     */
    public static <E> E oneFrom(List<E> elements) {
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("List empty");
        }
        
        int index = nextInt(0, elements.size());
        return elements.get(index);
    }
    
    /**
     * Returns a random element from an array.
     * @throws IllegalArgumentException if the array is empty.
     */
    public static <E> E oneFrom(E[] elements) {
        if (elements.length == 0) {
            throw new IllegalArgumentException("array empty");
        }
        
        int index = nextInt(0, elements.length);
        return elements[index];
    }
}
