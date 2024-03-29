//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

/**
 * MultimediaLib applications are split into a number of scenes, each
 * representing a discrete phase of the application. Only one scene can be
 * active at the same time, but sub-scenes can be attached to split large or
 * complex scenes into different parts. The currently active scene, and its
 * attached sub-scenes, will receive frame updates for as long as the parent
 * scene is active.
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
     * Called when the canvas size has changed, for example because the window
     * has been resized or because the device orientation has changed. Note
     * {@code canvasWidth} and {@code canvasHeight} are based on the current
     * canvas size, which might be different from the "native" screen size.
     */
    default void resize(SceneContext context, int canvasWidth, int canvasHeight) {
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
