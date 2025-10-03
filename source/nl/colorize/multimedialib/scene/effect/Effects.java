//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.scene.FluentScene;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.Updatable;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.util.TextUtils;
import nl.colorize.util.animation.Interpolation;
import nl.colorize.util.animation.Timeline;

/**
 * Utility methods for creating effects that can be attached to the currently
 * active scene as sub-scenes.
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
    public static Scene scaleToFit(Sprite sprite) {
        return (context, deltaTime) -> {
            Canvas canvas = context.getCanvas();
            float scaleX = (float) canvas.getWidth() / (float) sprite.getCurrentWidth();
            float scaleY = (float) canvas.getHeight() / (float) sprite.getCurrentHeight();
            sprite.getTransform().setScale(Math.max(scaleX, scaleY) * 100f);
        };
    }

    /**
     * Makes text slowly appear over time, with more and more characters
     * appearing on screen over time until the entire text is shown.
     */
    public static Scene appearText(Text text, float duration) {
        Preconditions.checkArgument(duration > 0f, "Invalid duration: " + duration);

        String originalText = TextUtils.LINE_JOINER.join(text.getLines());

        Timeline timeline = new Timeline();
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(duration, originalText.length());

        Updatable onFrame = deltaTime -> {
            timeline.movePlayhead(deltaTime);
            String visibleText = originalText.substring(0, (int) timeline.getValue());
            text.setText(visibleText);
        };

        return new FluentScene(onFrame)
            .withCompletion(timeline::isCompleted);
    }

    /**
     * Makes graphics rotate 360 degrees within the specified duration, with
     * a shorter duration meaning a faster rotation speed.
     */
    public static Scene spin(Sprite graphic, float duration) {
        Preconditions.checkArgument(duration > 0f, "Invalid duration: " + duration);

        Timeline timeline = new Timeline(Interpolation.LINEAR, true);
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(duration, 360f);

        return (context, deltaTime) -> {
            timeline.movePlayhead(deltaTime);
            graphic.getTransform().setRotation(timeline.getValue());
        };
    }
}
