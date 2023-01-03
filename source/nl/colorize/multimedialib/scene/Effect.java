//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.stage.Graphic2D;
import nl.colorize.multimedialib.stage.Group;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.util.animation.Interpolation;
import nl.colorize.util.animation.Timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Effects are short-lived sub-scenes that can be used to define graphical
 * effects in a declarative way. Effects are not intended to contain general
 * application logic, they are intended for self-contained effects that are
 * active for a limited duration. This allows for declarative effects that
 * can be played without having to manually update their logic and graphics
 * every frame.
 * <p>
 * Effects can be defined by creating a new instance of this class and attaching
 * the desired behavior. Alternatively, a number of factory methods for common
 * graphical effects can be used. These out-of-the-box effects can then be
 * extended with additional behavior if needed.
 */
public class Effect implements Scene {

    private List<Runnable> startHandlers;
    private List<Updatable> frameHandlers;
    private List<Consumer<SceneContext>> clickHandlers;
    private List<Runnable> completionHandlers;
    private List<Graphic2D> linkedGraphics;
    private boolean completed;

    public Effect() {
        this.startHandlers = new ArrayList<>();
        this.frameHandlers = new ArrayList<>();
        this.clickHandlers = new ArrayList<>();
        this.completionHandlers = new ArrayList<>();
        this.linkedGraphics = new ArrayList<>();
        this.completed = false;
    }

    public void addStartHandler(Runnable handler) {
        startHandlers.add(handler);
    }

    public void addFrameHandler(Updatable handler) {
        frameHandlers.add(handler);
    }

    public void addClickHandler(Rect bounds, Runnable handler) {
        clickHandlers.add(context -> {
            InputDevice input = context.getInputDevice();
            if (input.isPointerReleased(bounds)) {
                handler.run();
            }
        });
    }

    public void addClickHandler(Graphic2D graphic, Runnable handler) {
        addClickHandler(graphic.getBounds(), handler);
    }

    public void addCompletionHandler(Runnable handler) {
        completionHandlers.add(handler);
    }

    /**
     * Adds a frame handler that will update the specified timeline every frame,
     * and then invoke the callback function based on the timeline's new value.
     * When the timeline ends, this effect will be marked as completed.
     */
    public void addTimelineHandler(Timeline timeline, Consumer<Float> callback) {
        addFrameHandler(deltaTime -> {
            timeline.movePlayhead(deltaTime);
            callback.accept(timeline.getValue());

            if (timeline.isCompleted() && !timeline.isLoop()) {
                complete();
            }
        });
    }

    public void stopAfter(float duration) {
        Timer timer = new Timer(duration);

        addFrameHandler(deltaTime -> {
            timer.update(deltaTime);
            if (timer.isCompleted()) {
                complete();
            }
        });
    }

    public void stopIf(Supplier<Boolean> condition) {
        addFrameHandler(deltaTime -> {
            if (condition.get()) {
                complete();
            }
        });
    }

    /**
     * Links the specified graphics to this effect, meaning they will be removed
     * from the stage when this effect is completed.
     */
    public void removeAfterwards(Graphic2D... graphics) {
        for (Graphic2D graphic : graphics) {
            linkedGraphics.add(graphic);
        }
    }

    /**
     * Links the specified graphics to this effect, meaning they will be removed
     * from the stage when this effect is completed.
     */
    public void removeAfterwards(Group group) {
        for (Graphic2D graphic : group) {
            linkedGraphics.add(graphic);
        }
    }

    @Override
    public void start(SceneContext context) {
        startHandlers.forEach(Runnable::run);
    }

    @Override
    public void update(SceneContext context, float deltaTime) {
        if (!completed) {
            frameHandlers.forEach(handler -> handler.update(deltaTime));
            clickHandlers.forEach(handler -> handler.accept(context));
        }
    }

    @Override
    public void end(SceneContext context) {
        linkedGraphics.forEach(graphic -> context.getStage().remove(graphic));
        completionHandlers.forEach(Runnable::run);
    }

    public void complete() {
        completed = true;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    public void withLinkedGraphics(Consumer<Graphic2D> callback) {
        linkedGraphics.forEach(callback);
    }

    /**
     * Shorthand for an effect that runs on a timer. The effect will be marked
     * as completed once the timer's duration has been reached.
     */
    public static Effect forTimer(float duration) {
        return forTimer(duration, null);
    }

    /**
     * Shorthand for an effect that runs on a timer, performing the specified
     * action once the timer has been completed.
     */
    public static Effect forTimer(float duration, Runnable action) {
        Effect effect = new Effect();
        Timer timer = new Timer(duration);

        if (action != null) {
            effect.addCompletionHandler(action);
        }

        effect.addFrameHandler(deltaTime -> {
            timer.update(deltaTime);
            if (timer.isCompleted()) {
                effect.complete();
            }
        });

        return effect;
    }

    /**
     * Shorthand for creating an effect that will invoke a callback function
     * based on the timeline's current value. The effect is completed once the
     * timeline has reached the end.
     */
    public static Effect forTimeline(Timeline timeline, Consumer<Float> callback) {
        Effect effect = new Effect();
        effect.addTimelineHandler(timeline, callback);
        return effect;
    }

    /**
     * Shorthand for creating an effect that modifies the sprite's X position
     * based on a timeline.
     */
    public static Effect forSpriteX(Sprite sprite, Timeline timeline) {
        Effect effect = new Effect();
        effect.addTimelineHandler(timeline, value -> sprite.getPosition().setX(value));
        effect.removeAfterwards(sprite);
        return effect;
    }

    /**
     * Shorthand for creating an effect that modifies the sprite's Y position
     * based on a timeline.
     */
    public static Effect forSpriteY(Sprite sprite, Timeline timeline) {
        Effect effect = new Effect();
        effect.addTimelineHandler(timeline, value -> sprite.getPosition().setY(value));
        effect.removeAfterwards(sprite);
        return effect;
    }

    /**
     * Shorthand for creating an effect that rotates a sprite.
     */
    public static Effect forSpriteRotation(Sprite sprite, float duration) {
        Preconditions.checkArgument(duration > 0f, "Invalid duration: " + duration);

        Timeline timeline = new Timeline(Interpolation.LINEAR, true);
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(duration, 360f);

        Effect effect = new Effect();
        effect.addTimelineHandler(timeline, value -> sprite.getTransform().setRotation(value));
        effect.removeAfterwards(sprite);
        return effect;
    }

    /**
     * Shorthand for creating an effect that scales a sprite based on the
     * specified timeline.
     */
    public static Effect forSpriteScale(Sprite sprite, Timeline timeline) {
        Effect effect = new Effect();
        effect.addTimelineHandler(timeline, value -> sprite.getTransform().setScale(value));
        effect.removeAfterwards(sprite);
        return effect;
    }

    /**
     * Shorthand for creating an effect that modifies the sprite's alpha value
     * based on a timeline.
     */
    public static Effect forSpriteAlpha(Sprite sprite, Timeline timeline) {
        Effect effect = new Effect();
        effect.addTimelineHandler(timeline, value -> sprite.getTransform().setAlpha(value));
        effect.removeAfterwards(sprite);
        return effect;
    }

    /**
     * Shorthand for creating an effect that modifies the primitive's alpha
     * value based on a timeline.
     */
    public static Effect forPrimitiveAlpha(Primitive primitive, Timeline timeline) {
        Effect effect = new Effect();
        effect.addTimelineHandler(timeline, value -> primitive.setAlpha(value));
        effect.removeAfterwards(primitive);
        return effect;
    }

    /**
     * Shorthand for creating an effect that modifies the text's alpha value
     * based on a timeline.
     */
    public static Effect forTextAlpha(Text text, Timeline timeline) {
        Effect effect = new Effect();
        effect.addTimelineHandler(timeline, value -> text.setAlpha(value));
        effect.removeAfterwards(text);
        return effect;
    }

    /**
     * Shorthand for creating an effect that will make the text slowly appear
     * over time, with more and more characters appearing on screen over time
     * until the entire text is shown.
     */
    public static Effect forTextAppear(Text text, float duration) {
        Preconditions.checkArgument(duration > 0f, "Invalid duration: " + duration);

        String originalText = text.getText();

        Timeline timeline = new Timeline();
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(duration, originalText.length());

        Effect effect = new Effect();
        effect.addTimelineHandler(timeline, deltaTime -> {
            String visibleText = originalText.substring(0, (int) timeline.getValue());
            text.setText(visibleText);
        });
        return effect;
    }
}
