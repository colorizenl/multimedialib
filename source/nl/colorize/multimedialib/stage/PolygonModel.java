//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.scene.Updatable;

/**
 * An instance of a 3D polygon model that can added to the stage. Multiple
 * instances of the model can be created, so that multiple instances can share
 * the same geometry and textures. Models can be created programmatically,
 * typically when using simple primitives, or loaded from external files.
 */
public interface PolygonModel extends Updatable {

    public Transform3D getTransform();

    public ModelAnimation getAnimation(String name);

    public void playAnimation(ModelAnimation animation);

    default void playAnimation(String name) {
        ModelAnimation animation = getAnimation(name);
        playAnimation(animation);
    }

    /**
     * Creates a copy of this model. The copy will share the same geometry and
     * textures, but will have its own transform. Note that creating the copy
     * does *not* automatically add the copy to the stage.
     */
    public PolygonModel copy();
}
