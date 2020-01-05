//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.math.IntMath;

import java.util.Collection;

/**
 * Basic math-related functions that are not included in {@link java.lang.Math}
 * or Google Guava.
 */
public final class MathUtils {

    public static final float EPSILON = 0.001f;
    
    private MathUtils() {
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
        return value ? 1 : 0;
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
    
    public static float average(Collection<? extends Number> numbers) {
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
}
