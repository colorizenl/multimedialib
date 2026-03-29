//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Immutable angle in degrees, in the range between 0° and 360°. Angles are
 * normalized upon creation, so {@code new Angle(20).equals(new Angle(380)}.
 */
public record Angle(float degrees) {

    public static final Angle ORIGIN = new Angle(0f);
    public static final List<Angle> CARDINAL = toAngles(0, 90, 180, 270);
    public static final List<Angle> INTERCARDINAL = toAngles(0, 45, 90, 135, 180, 225, 270, 315);

    public Angle(float degrees) {
        while (degrees < 0f) {
            degrees += 360f;
        }
        this.degrees = degrees % 360f;
    }

    public float getRadians() {
        return (float) Math.toRadians(degrees);
    }

    /**
     * Returns the absolute difference between this angle and the specified
     * other angle, in the range between 0 and 180 degrees. For example,
     * if this angle is 350 degrees, and {@code other} is 10 degrees,
     * this will return 20.
     */
    public float distanceTo(Angle other) {
        float phi = Math.abs(other.degrees - degrees) % 360f;
        if (phi > 180f) {
            phi = 360f - phi;
        }
        return phi;
    }

    /**
     * Returns the difference between this angle and the specified other angle,
     * in the range between -180 and 180 degrees. This method is similar to
     * {@link #distanceTo(Angle)}, but also considers the "direction" of the
     * difference and can therefore return negative values.
     */
    public float angleTo(Angle other) {
        float phi = other.degrees - degrees;

        if (phi > 180f) {
            return phi - 360f;
        } else if (phi <= -180f) {
            return phi + 360f;
        } else {
            return phi;
        }
    }

    public Angle move(Angle other) {
        return new Angle(degrees + other.degrees);
    }

    public Angle move(float byDegrees) {
        return new Angle(degrees + byDegrees);
    }

    /**
     * Returns an {@link Angle} that is the exact opposite of this angle.
     * For example, the opposite angle of 90 degrees is 270 degrees.
     */
    public Angle opposite() {
        return new Angle(degrees + 180f);
    }

    @Override
    public String toString() {
        return Math.round(degrees) + "°";
    }

    /**
     * Factory method to create an {@link Angle} instance from an angle
     * specified in radians, as opposed to the constructor which requires
     * an angle specified in degrees.
     */
    public static Angle fromRadians(float radians) {
        return new Angle((float) Math.toDegrees(radians));
    }

    private static List<Angle> toAngles(int... values) {
        return IntStream.of(values)
            .mapToObj(Angle::new)
            .toList();
    }
}
