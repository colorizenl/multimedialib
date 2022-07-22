//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import nl.colorize.multimedialib.scene.Updatable;

import java.util.Map;

/**
 * An instance of a 3D polygon model that can added to the stage. Multiple
 * instances of the model can be created, so that multiple instances can share
 * the same geometry and textures. Models can be created programmatically,
 * typically when using simple primitives, or loaded from external files.
 */
public interface PolygonModel extends Updatable {

    public Transform3D getTransform();

    public Map<String, AnimationInfo> getAnimations();

    public void playAnimation(String animation, boolean loop);

    default void playAnimation(String animation) {
        playAnimation(animation, false);
    }

    /**
     * Creates a copy of this model. The copy will share the same geometry and
     * textures, but will have its own transform. Note that creating the copy
     * does *not* automatically add the copy to the stage.
     */
    public PolygonModel copy();
}
