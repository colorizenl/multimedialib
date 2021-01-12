//-----------------------------------------------------------------------------
// Ape Attack
// Copyright 2005-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.renderer.Drawable;
import nl.colorize.multimedialib.renderer.GraphicsContext2D;
import nl.colorize.multimedialib.renderer.Updatable;
import nl.colorize.multimedialib.scene.effect.Timer;

/**
 * Logic and/or graphics that are part of a scene. The application can only play
 * one "main" scene at the same time. However, in some cases placing all of a
 * scene's logic and graphics into a scene would make it too large. This
 * interface can be used to divided such scenes into different parts. To achieve
 * some level of separation of concerns, sub-scenes (unlike regular scenes) do
 * not get direct access to the entire application. Also, sub-scenes do not have
 * an initialization method, since they are assumed to be initialized by their
 * parent scene.
 */
public interface SubScene extends Updatable, Drawable {

    /**
     * Sub-scenes initially start as active. Once they have been completed, they
     * will be detached from the current scene by the application. Note that
     * this method does not influence the link between scenes and sub-scenes:
     * if the active scene is changed, all active sub-scenes will be stopped
     * regardless of whether they have been completed or not.
     */
    public boolean isCompleted();

    /**
     * When true, indicates that this effect should be rendered as part of the
     * scene's background graphics. By default, graphics are drawn in the
     * foreground, in front of the scene's own graphics.
     */
    default boolean hasBackgroundGraphics() {
        return false;
    }

    /**
     * Convenience method to create a {@code SubScene} instance from the
     * specified callbacks.
     */
    public static SubScene from(Updatable logic, Drawable graphics) {
        return from(logic, graphics, Float.MAX_VALUE);
    }

    /**
     * Convenience method to create a {@code SubScene} instance from the
     * specified callbacks, that will only stay active for a limited duration.
     */
    public static SubScene from(Updatable logic, Drawable graphics, float duration) {
        Timer timer = new Timer(duration);

        return new SubScene() {
            @Override
            public void update(float deltaTime) {
                logic.update(deltaTime);
                timer.update(deltaTime);
            }

            @Override
            public void render(GraphicsContext2D gc) {
                graphics.render(gc);
            }

            @Override
            public boolean isCompleted() {
                return timer.isCompleted();
            }
        };
    }

    /**
     * Convenience method to create a {@code SubScene} containing logic but not
     * graphics.
     */
    public static SubScene fromLogic(Updatable logic) {
        return from(logic, g -> {});
    }

    /**
     * Convenience method to create a {@code SubScene} containing graphics but
     * no logic.
     */
    public static SubScene fromGraphics(Drawable graphics) {
        return from(deltaTime -> {}, graphics);
    }
}
