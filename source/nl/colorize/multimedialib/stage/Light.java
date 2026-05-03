//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Point3D;

/**
 * Light source that influences 3D graphics on the stage. Light sources are
 * "global" in the 3D world, they are not part of the scene graph and need
 * to be added to the stage separately. Also, light sources that are part of
 * the stage exist <em>in addition</em> to the global ambient light. The latter
 * can be changed using {@link Stage#setAmbientLightColor(ColorRGB)}.
 * <p>
 * Light sources consist of a position, a color, and an intensity value. The
 * latter is represented by a number between 0 and 100. All properties are
 * dynamic.
 */
@Getter
@Setter
public class Light {

    private Point3D position;
    private ColorRGB color;
    private float intensity;

    public Light(Point3D position, ColorRGB color, float intensity) {
        setPosition(position);
        setColor(color);
        setIntensity(intensity);
    }

    public void setIntensity(float intensity) {
        this.intensity = Math.clamp(intensity, 0f, 100f);
    }

    @Override
    public String toString() {
        return "Light";
    }
}
