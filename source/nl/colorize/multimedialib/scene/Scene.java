//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

/**
 * Applications are split into *scenes*. Simple applications may consist of a
 * single scene, but larger applications can be split into multiple scenes,
 * each representing a discrete phase of the application.
 * <p>
 * Only one scene can be active at the same time, but it is possible to attach
 * sub-scenes to the currently active scene. These sub-scenes can contain
 * their own logic, but cannot outlive their parent scene. When the active
 * scene is changed, both the scene itself and its sub-scenes will be
 * terminated and the stage will be cleared in preparation for the next scene.
 * <p>
 * The currently active scene (and its sub-scenes) receive access to the
 * {@link SceneContext}, which is provided by the renderer via callback
 * methods.
 */
@FunctionalInterface
public interface Scene {

    /**
     * Initialization logic that should be performed when the scene is
     * started. Note that this method is called *every* time the scene is
     * started, not just the first time.
     * <p>
     * This method is optional, the default implementation does nothing.
     */
    default void start(SceneContext context) {
    }

    /**
     * Called during every frame update for as long as the scene is active.
     * {@code deltaTime} indicates the elapsed time since the last frame, in
     * seconds.
     */
    public void update(SceneContext context, float deltaTime);

    /**
     * Clean-up logic that is performed every time the scene ends.
     * <p>
     * This method is optional, the default implementation does nothing.
     */
    default void end(SceneContext context) {
    }

    /**
     * Indicates the scene has been completed and no longer wishes to receive
     * frame updates.
     * <p>
     * If this scene is the currently active scene, it might not actually end
     * until a new scene is requested.
     * <p>
     * If this scene is a completed sub-scene, meaning there is a parent scene
     * which is still active, this sub-scene will end after the current frame.
     */
    default boolean isCompleted() {
        return false;
    }
}
