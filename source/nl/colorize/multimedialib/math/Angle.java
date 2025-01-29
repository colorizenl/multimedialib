//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

/**
 * Immutable angle in degrees, in the range between 0° and 360°. Angles are
 * normalized upon creation, so {@code new Angle(20).equals(new Angle(380)}.
 */
public record Angle(float degrees) {

    public static final Angle ORIGIN = new Angle(0f);

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
     * Returns the distance between this angle and the specified other angle,
     * in degrees. The returned value will be in the range between 0 and 180
     * degrees.
     */
    public float difference(Angle other) {
        float phi = Math.abs(other.degrees - degrees) % 360f;
        if (phi > 180f) {
            phi = 360f - phi;
        }
        return phi;
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
}
