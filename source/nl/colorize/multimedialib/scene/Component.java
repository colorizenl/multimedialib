//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

/**
 * Components store data that are part of an {@link Actor}. Components are
 * "pure" data structures, they should not contain any behavior.
 * <p>
 * This class <em>does</em> implement the {@link Updatable} interface, but
 * this is purely to support data fields that require frame updates, such as
 * timers, and should not be used to implement entity logic.
 */
public interface Component extends Updatable {

    /**
     * Updates all component fields that are dependent on frame updates. The
     * default implementation of this method does nothing.
     */
    @Override
    default void update(float deltaTime) {
    }
}
