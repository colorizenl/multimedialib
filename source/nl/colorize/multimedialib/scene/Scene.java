//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

/**
 * Scenes are the top-level structure to divide an application into different
 * "chapters". Only one scene can be active at the same time. Simple
 * applications may consist of a single scene, but larger applications can be
 * split into multiple scenes, with each scene representing a different
 * chapter or phase.
 * <p>
 * The renderer uses callback methods to update the currently active scene.
 * These callback methods provide the {@link SceneContext} for access to the
 * underlying renderer.
 */
public interface Scene {

    /**
     * Called by the renderer, allows the scene to initialize itself when
     * the renderer starts this scene. Note that this method is called
     * <em>every</em> time the scene is started, not just the first time.
     */
    public void start(SceneContext context);

    /**
     * Called by the renderer during every frame update for as long as this
     * scene is active. This is the main method for scene logic, and has full
     * access to the {@link SceneContext}. If the scene contains
     * {@link Actor}s, this scene is updated <em>before</em> its actors.
     *
     * @param deltaTime Elapsed time since the last frame update, in seconds.
     */
    public void update(SceneContext context, double deltaTime);

    /**
     * Called by the renderer every time this scene ends, either because the
     * next scene starts or because the application is terminated.
     * <p>
     * This method is optional, the default implementation does nothing.
     */
    default void end(SceneContext context) {
    }
}
