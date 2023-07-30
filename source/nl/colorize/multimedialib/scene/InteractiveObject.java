//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.stage.Container;

/**
 * Interface for objects that both perform scene logic and provide graphics
 * that can be added to the stage. This object therefore acts as a sub-scene
 * and receives frame updates while it is active, while its graphics need to
 * be added to the stage in order to be visible. Attaching logic and graphics
 * can also be done simultaneously using the convenient method
 * {@link SceneContext#attach(InteractiveObject, Container)}.
 * <p>
 * The name of this interface is an homage to the Flash ActionScript concept
 * of the same name.
 */
public interface InteractiveObject extends Scene {

    /**
     * Returns the root container that contains all of this object's graphics.
     */
    public Container getContainer();

    @Override
    default void end(SceneContext context) {
        getContainer().detach();
    }
}
