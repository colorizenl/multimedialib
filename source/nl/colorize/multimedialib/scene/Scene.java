//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

/**
 * MultimediaLib applications are divided into a number of *scenes*. Each
 * scene represents a discrete part or phase of an application that is active
 * for some period of time. Only one scene can be active at any point in time.
 * Simple applications may consist of a single scene, while larger applications
 * will typically have many. The currently active scene will receive frame
 * updates for as long as it is active.
 * <p>
 * The {@link Scene} interface is similar to {@link Agent}. The main difference
 * is that only one scene can be active at the same time, while the scene can
 * include many agents. Moreover, {@code Scene}s are considered the central
 * construct and receive access to the entire {@link SceneContext}, while
 * {@link Agent}s tend to have a smaller scope and do not have full access.
 */
public interface Scene {

    /**
     * Initialization logic that should be performed when the scene is
     * started. Note that this method is called *every* time the scene is
     * started, not only the first time.
     */
    public void start(SceneContext context);

    /**
     * Called during every frame update for as long as the scene is active.
     * @param deltaTime Elapsed time since the last frame, in seconds.
     */
    public void update(SceneContext context, float deltaTime);

    /**
     * Clean-up logic that is performed every time the scene ends. Implementing
     * this method is optional, the default implementation does nothing.
     */
    default void end(SceneContext context) {
    }
}
