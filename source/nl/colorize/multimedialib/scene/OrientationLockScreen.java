//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.stage.Graphic2D;
import nl.colorize.multimedialib.stage.Layer2D;
import nl.colorize.multimedialib.stage.Stage;

import java.util.List;

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
public class OrientationLockScreen extends Effect {

    private boolean active;
    private List<Graphic2D> graphics;

    private static final String LAYER = "$$OrientationLockScreen";

    public OrientationLockScreen(Graphic2D... graphics) {
        Preconditions.checkArgument(graphics.length > 0,
            "Orientation lock screen must have graphics");

        this.active = false;
        this.graphics = ImmutableList.copyOf(graphics);

        addFrameHandler(this::update);
    }

    private void update(float deltaTime) {
        Canvas canvas = getContext().getCanvas();
        Stage stage = getContext().getStage();

        if (active && !shouldBeActive(canvas)) {
            Layer2D layer = stage.retrieveLayer(LAYER);
            graphics.forEach(layer::remove);
            active = false;
        } else if (!active && shouldBeActive(canvas)) {
            Layer2D layer = stage.retrieveLayer(LAYER);
            graphics.forEach(layer::add);
            active = true;
        }

        // Reposition all graphics because the canvas might have changed.
        if (active) {
            for (Graphic2D graphic : graphics) {
                graphic.setPosition(canvas.getCenter());
            }
        }
    }

    private boolean shouldBeActive(Canvas canvas) {
        return canvas.getWidth() < canvas.getHeight();
    }
}
