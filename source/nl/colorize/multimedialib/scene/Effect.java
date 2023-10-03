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
 * Effects are short-lived sub-scenes that can be defined in a declarative
 * way. Effects are not intended to contain general application logic, they
 * are intended for small, self-contained effects that are active for a
 * limited period of time and are independent of other things happening in
 * the scene.
 * <p>
 * Behavior can be added to effects in the form of <em>handlers</em>. These
 * handlers operate on different points in the effect's lifecycle, for
 * example during frame updates or after the effect is marked as completed.
 * <p>
 * Effects can influence graphics in two different ways. The first is by
 * adding graphics to the effect itself. The second  allows the effect to
 * influence graphics that are on stage but are not part of the effect itself.
 * Using {@link #linkGraphics(Graphic2D...)} will "link" these eisting
 * graphics to the effect, meaning they will be removed when the effect has
 * completed.
 */
public final class Effect implements Scene {

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
            InputDevice input = context.getInput();
            if (input.isPointerReleased(bounds)) {
                handler.run();
                input.clearPointerReleased();
            }
        });
    }

    public void addClickHandler(Graphic2D graphic, Runnable handler) {
        clickHandlers.add(context -> {
            InputDevice input = context.getInput();
            if (input.isPointerReleased(graphic.getStageBounds())) {
                handler.run();
                input.clearPointerReleased();
            }
        });
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

    /**
     * Immediately marks this effect as completed. Note this will not
     * immediately run the effect's completion handlers, that will only happen
     * when the effect receives its next frame update.
     */
    public void complete() {
        completed = true;
    }

    /**
     * Adds a handler that will mark the effect as completed once the specified
     * period of time has elapsed.
     */
    public void stopAfter(float duration) {
        Timer timer = new Timer(duration);

        addFrameHandler(deltaTime -> {
            timer.update(deltaTime);
            if (timer.isCompleted()) {
                complete();
            }
        });
    }

    /**
     * Adds a handler that will mark the effect as completed once the specified
     * condition is met.
     */
    public void stopIf(Supplier<Boolean> condition) {
        addFrameHandler(deltaTime -> {
            if (condition.get()) {
                complete();
            }
        });
    }

    /**
     * Links existing graphics to this effect. This means the graphics will be
     * removed from stage when the effect has completed.
     */
    public void linkGraphics(Graphic2D... graphics) {
        for (Graphic2D graphic : graphics) {
            linkedGraphics.add(graphic);
        }
    }

    /**
     * Links existing graphics to this effect. This means the graphics will be
     * removed from stage when the effect has completed.
     */
    public void removeAfterwards(Graphic2D... graphics) {
        //TODO deprecate this in favor of linkGraphics().
        linkGraphics(graphics);
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
        linkedGraphics.forEach(Graphic2D::detach);
        completionHandlers.forEach(Runnable::run);
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    public void withLinkedGraphics(Consumer<Graphic2D> callback) {
        linkedGraphics.forEach(callback);
    }

    public <T extends Graphic2D> void withLinkedGraphics(Class<T> type, Consumer<T> callback) {
        linkedGraphics.stream()
            .filter(graphic -> graphic.getClass().equals(type))
            .forEach(graphic -> callback.accept((T) graphic));
    }

    /**
     * Attaches this effect to the specified scene context. Using this method
     * is identical to {@code sceneContext.attach(effect)}, but is more
     * readable when creating effects in a declarative way.
     */
    public void attach(SceneContext context) {
        context.attach(this);
    }

    /**
     * Creates an effect that will first wait for the specified period of
     * time, and will then perform an action.
     */
    public static Effect delay(float duration, Runnable action) {
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
     * Creates an effect that will first wait for the specified period of
     * time, and will then perform an action.
     *
     * @deprecated This method is identical to {@link #delay(float, Runnable)},
     *             use that instead because of its more descriptive name.
     */
    @Deprecated
    public static Effect forTimer(float duration, Runnable action) {
        return delay(duration, action);
    }

    /**
     * Creates an effect that will invoke a callback function based on the
     * timeline's current value. The effect is completed once the timeline
     * has reached the end.
     */
    public static Effect forTimeline(Timeline timeline, Consumer<Float> callback) {
        Effect effect = new Effect();
        effect.addTimelineHandler(timeline, callback);
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
        effect.addTimelineHandler(timeline, value -> primitive.getTransform().setAlpha(value));
        effect.removeAfterwards(primitive);
        return effect;
    }

    /**
     * Shorthand for creating an effect that modifies the text's alpha value
     * based on a timeline.
     */
    public static Effect forTextAlpha(Text text, Timeline timeline) {
        Effect effect = new Effect();
        effect.addTimelineHandler(timeline, value -> text.getTransform().setAlpha(value));
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

    public static Effect forClickHandler(Graphic2D graphic, Runnable handler) {
        Effect effect = new Effect();
        effect.linkGraphics(graphic);
        effect.addClickHandler(graphic, handler);
        return effect;
    }
}
