//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
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
