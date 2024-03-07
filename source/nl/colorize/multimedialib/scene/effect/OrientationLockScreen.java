//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.stage.Graphic2D;

/**
 * Displays an image and/or message to inform the user to change their device
 * orientation. This can be used for applications that can only be used in
 * landscape mode. The scene will automatically end if the device is changed
 * to landscape orientation.
 * <p>
 * This class can be used as a standalone scene, meaning that the originally
 * active scene is forcefully interrupted when the device is changed to
 * portait mode. It can also be used as a visual effect, meaning the original
 * scene will continue to play in the background. Which approach should be
 * preferred depends on the type of application.
 */
public class OrientationLockScreen implements Scene {

    private Graphic2D graphics;

    public OrientationLockScreen(Graphic2D graphics) {
        this.graphics = graphics;
    }

    @Override
    public void update(SceneContext context, float deltaTime) {
        Canvas canvas = context.getCanvas();
        graphics.getTransform().setVisible(canvas.getWidth() < canvas.getHeight());
        // Reposition all graphics because the canvas might have changed.
        graphics.getTransform().setPosition(canvas.getCenter());
    }
}
