//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
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
