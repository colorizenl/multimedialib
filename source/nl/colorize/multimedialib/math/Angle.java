//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

/**
 * Immutable representation of an angle in degrees. Angles are normalized to
 * the range between 0 and 360, so an angle of 20 degrees is considered
 * equivalent to an angle of 380 degrees.
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

    @Override
    public String toString() {
        return Math.round(degrees) + "\u00B0";
    }
}
