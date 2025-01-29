//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

/**
 * Standardized interface for all objects that should be updated every frame
 * during the animation loop.
 */
@FunctionalInterface
public interface Updatable {

    /**
     * Updates this object for the current frame. {@code deltaTime} indicates
     * the elapsed time since the last frame update, in seconds.
     */
    public void update(float deltaTime);
}
