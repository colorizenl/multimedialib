//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.math.IntMath;

import java.util.List;

/**
 * Basic math-related functions that are not included in {@link java.lang.Math}
 * or Google Guava.
 */
public final class MathUtils {

    public static final float EPSILON = 0.001f;
    public static final float PI = (float) Math.PI;
    public static final float HALF_PI = 0.5f * PI;

    private MathUtils() {
    }

    public static int floor(float n) {
        return (int) n;
    }
    
    public static int ceiling(float n) {
        int truncated = (int) n;
        float remainder = n - truncated;
        return (remainder >= EPSILON) ? truncated + 1 : truncated;
    }
    
    public static int signum(int n) {
        if (n > 0) return 1;
        if (n < 0) return -1;
        return 0;
    }
    
    public static int signum(float n) {
        if (n > 0f) return 1;
        if (n < 0f) return -1;
        return 0;
    }

    public static int signum(boolean value) {
        return value ? 1 : -1;
    }
    
    public static int sum(int[] values) {
        int sum = 0;
        for (int value : values) {
            sum += value;
        }
        return sum;
    }
    
    public static int clamp(int value, int min, int max) {
        if (value < min) value = min;
        if (value > max) value = max;
        return value;
    }

    public static float clamp(float value, float min, float max) {
        if (value < min) value = min;
        if (value > max) value = max;
        return value;
    }
    
    public static long clamp(long value, long min, long max) {
        if (value < min) value = min;
        if (value > max) value = max;
        return value;
    }
    
    public static float average(List<? extends Number> numbers) {
        float sum = 0f;
        for (Number num : numbers) {
            sum += num.floatValue();
        }
        return sum / numbers.size();
    }
    
    public static boolean isPowerOfTwo(int n) {
        return IntMath.isPowerOfTwo(n);
    }
    
    public static int nextPowerOfTwo(int n) {
        if (isPowerOfTwo(n)) {
            return n;
        }
        
        for (int i = 4; i < 32768; i *= 2) {
            if (i > n) {
                return i;
            }
        }
        
        throw new IllegalArgumentException("Number out of range");
    }

    /**
     * Returns the distance between two angles in degrees. The result will be
     * in the range between 0 and 180.
     */
    public static float angleDistance(float a, float b) {
        float phi = Math.abs(b - a) % 360f;
        if (phi > 180f) {
            return 360f - phi;
        }
        return phi;
    }

    /**
     * Returns true if the specified two float values are either exactly equal,
     * or so similar that they fall within the {@link #EPSILON} range.
     */
    public static boolean equals(float a, float b) {
        return Math.abs(a - b) < EPSILON;
    }
}
