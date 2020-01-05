//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

/**
 * Functional area that is part of a larger scene. Subsystems can contain logic,
 * graphics, or both.
 * <p>
 * Subsystems are very similar to scenes, but have a smaller functional scope.
 * Also, multiple subsystems can be active simultaneously, while only one scene
 * can be active at the same time.
 */
public interface Subsystem extends Updatable, Renderable {

    default void init() {
    }
}
