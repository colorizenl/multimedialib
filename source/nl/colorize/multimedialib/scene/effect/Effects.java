//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.scene.FluentActor;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.Actor;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Spatial2D;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.util.TextUtils;
import nl.colorize.util.animation.Interpolation;
import nl.colorize.util.animation.Timeline;

/**
 * Utility methods for creating visual effects that affect graphics. The
 * effects are created as actors that can be attached to the currently
 * active scene.
 */
public final class Effects {

    private Effects() {
    }

    /**
     * Changes a sprite's scale so that it fits the canvas. This method uses
     * uniform scaling, i.e. the sprite's original aspect ratio will be
     * retained.
     * <p>
     * This method returns a {@link Scene} that should be registered as a
     * sub-scene. This is because the sprite needs to be rescaled every frame,
     * as the sprite's graphics might change over time.
     */
    public static Actor scaleToFit(Sprite sprite, Canvas canvas) {
        return _ -> {
            double scaleX = (double) canvas.getWidth() / (double) sprite.getCurrentWidth();
            double scaleY = (double) canvas.getHeight() / (double) sprite.getCurrentHeight();
            sprite.getTransform().setScale(Math.max(scaleX, scaleY) * 100f);
        };
    }

    /**
     * Makes text slowly appear over time, with more and more characters
     * appearing on screen over time until the entire text is shown.
     */
    public static Actor appearText(Text text, double duration) {
        Preconditions.checkArgument(duration > 0f, "Invalid duration: " + duration);

        String originalText = TextUtils.LINE_JOINER.join(text.getLines());

        Timeline timeline = new Timeline();
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(duration, originalText.length());

        Actor onFrame = deltaTime -> {
            timeline.movePlayhead(deltaTime);
            String visibleText = originalText.substring(0, (int) timeline.getValue());
            text.setText(visibleText);
        };

        return FluentActor.create()
            .withFrameHandler(onFrame)
            .withCompletionCheck(timeline::isCompleted);
    }

    /**
     * Makes graphics rotate 360 degrees within the specified duration, with
     * a shorter duration meaning a faster rotation speed.
     */
    public static Actor spin(Sprite graphic, double duration) {
        Preconditions.checkArgument(duration > 0f, "Invalid duration: " + duration);

        Timeline timeline = new Timeline(Interpolation.LINEAR, true);
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(duration, 360f);

        return deltaTime -> {
            timeline.movePlayhead(deltaTime);
            graphic.getTransform().setRotation(timeline.getValue());
        };
    }

    /**
     * Displays the specified graphics until the user changes their device
     * orientation to landscape.
     */
    public static Actor showOrientationLock(Spatial2D graphics, Canvas canvas) {
        return _ -> {
            graphics.getTransform().setVisible(canvas.getWidth() < canvas.getHeight());
            // Reposition all graphics because the canvas might have changed.
            graphics.getTransform().setPosition(canvas.getCenter());
        };
    }
}
