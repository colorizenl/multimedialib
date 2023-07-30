//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;

import java.math.RoundingMode;
import java.text.NumberFormat;

/**
 * Basic math-related functions that are not included in {@link java.lang.Math}
 * or Google Guava.
 */
public final class MathUtils {

    public static final float EPSILON = 0.001f;

    private MathUtils() {
    }

    public static int floor(float n) {
        return (int) n;
    }

    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    public static float clamp(float value, float min, float max) {
        return Math.min(Math.max(value, min), max);
    }
    
    public static long clamp(long value, long min, long max) {
        return Math.min(Math.max(value, min), max);
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
     * Formats a number with the specified number of decimal places. This is an
     * alternative to {@code String.format}, which is not fully supported on
     * some platforms.
     */
    public static String format(float n, int decimals) {
        Preconditions.checkArgument(decimals >= 0,
            "Invalid number of decimals: " + decimals);

        NumberFormat format = NumberFormat.getInstance();
        format.setMinimumFractionDigits(decimals);
        format.setMaximumFractionDigits(decimals);
        format.setRoundingMode(RoundingMode.UP);
        return format.format(n);
    }
}
