//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

/**
 * Defines a standardized interface for all objects that should be updated
 * every frame during the animation loop.
 */
@FunctionalInterface
public interface Updatable {

    /**
     * Updates this object for the current frame.
     * @param deltaTime Elapsed time since the last frame, in seconds.
     */
    public void update(float deltaTime);
}
