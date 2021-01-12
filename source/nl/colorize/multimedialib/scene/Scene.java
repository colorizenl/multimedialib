//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.renderer.GraphicsContext2D;

/**
 * Represents a discrete part or phase of an application, that is active for
 * some period of time. Simple applications might consist of a single scene,
 * complex applications can have dozens. Scenes can contain logic, rendering,
 * though they will typically do both.
 * <p>
 * Scenes will receive updates at various points throughout their life cycle:
 * when the scene is started (which can occur multiple times depending on the
 * application flow), and then frame updates and rendering when the scene is
 * active.
 * <p>
 * Scenes contain methods that are similar to {@code Updatable} and
 * {@code Drawable}, for performing frame updates and drawing graphics
 * respectively. The main difference with those interfaces is that the scene
 * gets full access to the application. Larger scenes can either use these
 * interfaces (to have a hard split between logic and graphics), or use the
 * {@link SubScene} interface.
 */
public interface Scene {

    /**
     * Initialization logic that should be performed every time the scene is
     * started. This method is called by the application that contains the
     * scene.
     */
    public void start(Application app);

    public void update(Application app, float deltaTime);

    public void render(Application app, GraphicsContext2D graphics);

    /**
     * Clean-up logic that is performed every time the scene ends. Implementing
     * this method is optional, the default implementation does nothing.
     */
    default void end(Application app) {
    }
}
