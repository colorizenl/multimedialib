//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.scene.Timer;

/**
 * Light source that influences 3D graphics on the stage. Note these lights
 * exist in addition to the ambient light, which can be changed using
 * {@link Stage#setAmbientLightColor(ColorRGB)}.
 */
@Getter
public class Light implements StageNode3D {

    private Transform3D transform;
    private Transform3D globalTransform;
    @Setter private ColorRGB color;
    @Setter private float intensity;

    public Light(ColorRGB color, float intensity) {
        this.transform = new Transform3D();
        this.globalTransform = new Transform3D();
        this.color = color;
        this.intensity = intensity;
    }

    @Override
    public void animate(Timer animationTimer) {
    }

    @Override
    public String toString() {
        return "Light";
    }
}
