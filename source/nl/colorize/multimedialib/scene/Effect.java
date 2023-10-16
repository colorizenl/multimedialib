//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.Pointer;
import nl.colorize.multimedialib.stage.Animation;
import nl.colorize.multimedialib.stage.Graphic2D;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.util.animation.Interpolation;
import nl.colorize.util.animation.Timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Effects are short-lived sub-scenes that can be defined in a declarative
 * style. Effects are not intended to contain general application logic, they
 * are intended for small, self-contained effects that are active for a
 * limited period of time and are independent of other things happening in
 * the scene.
 * <p>
 * Behavior can be added to effects in the form of <em>handlers</em>. These
 * handlers operate on different points in the effect's lifecycle, for
 * example during frame updates or after the effect is marked as completed.
 * <p>
 * Effects can be "linked" to graphics. When the effect ends, it will
 * automatically remove all linked graphics from the stage. This allows the
 * effect to control the life cycle for both the effect logic and the
 * associated graphics.
 */
public final class Effect implements Scene {

    private List<Updatable> frameHandlers;
    private List<ClickHandler> clickHandlers;
    private List<Runnable> completionHandlers;
    private List<Graphic2D> linkedGraphics;
    private List<BooleanSupplier> completionConditions;
    private boolean completed;

    /**
     * Creates a new effect that initially does not define any behavior and
     * is not linked to any graphics. Use the static factory methods to create
     * effects with the desired behavior, this constructor is for internal use
     * only.
     */
    private Effect() {
        this.frameHandlers = new ArrayList<>();
        this.clickHandlers = new ArrayList<>();
        this.completionHandlers = new ArrayList<>();
        this.linkedGraphics = new ArrayList<>();
        this.completionConditions = new ArrayList<>();
        this.completed = false;
    }

    public Effect addFrameHandler(Updatable handler) {
        frameHandlers.add(handler);
        return this;
    }

    /**
     * Adds a frame handler that will update the specified timeline every frame,
     * and then invoke the callback function based on the timeline's new value.
     * When the timeline ends, this effect will be marked as completed.
     */
    public Effect addTimelineHandler(Timeline timeline, Consumer<Float> callback) {
        addFrameHandler(deltaTime -> {
            timeline.movePlayhead(deltaTime);
            callback.accept(timeline.getValue());
        });
        stopIf(() -> timeline.isCompleted() && !timeline.isLoop());
        return this;
    }

    public Effect addClickHandler(Rect bounds, Runnable handler) {
        clickHandlers.add(new ClickHandler(() -> bounds, handler));
        return this;
    }

    public Effect addClickHandler(Graphic2D graphic, Runnable handler) {
        clickHandlers.add(new ClickHandler(graphic::getStageBounds, handler));
        return this;
    }

    public Effect addCompletionHandler(Runnable handler) {
        completionHandlers.add(handler);
        return this;
    }

    /**
     * Adds a handler that will mark the effect as completed once the specified
     * period of time has elapsed.
     */
    public Effect stopAfter(float duration) {
        Timer timer = new Timer(duration);
        addFrameHandler(timer);
        stopIf(timer::isCompleted);
        return this;
    }

    /**
     * Adds a handler that will mark the effect as completed once the specified
     * sprite animation has ended.
     */
    public Effect stopAfterAnimation(Sprite sprite) {
        return stopIf(() -> {
            Animation animation = sprite.getCurrentStateGraphics();
            Timer timer = sprite.getCurrentStateTimer();
            return animation.getDuration() > 0f && timer.getTime() >= animation.getDuration();
        });
    }

    /**
     * Adds a handler that will mark the effect as completed once the specified
     * condition is met.
     */
    public Effect stopIf(BooleanSupplier condition) {
        completionConditions.add(condition);
        return this;
    }

    /**
     * Links existing graphics to this effect. This means the graphics will be
     * removed from stage when the effect has completed.
     */
    public Effect linkGraphics(Graphic2D... graphics) {
        for (Graphic2D graphic : graphics) {
            linkedGraphics.add(graphic);
        }

        return this;
    }

    /**
     * Links existing graphics to this effect. This means the graphics will be
     * removed from stage when the effect has completed.
     *
     * @deprecated Use {@link #linkGraphics(Graphic2D...)} instead.
     */
    @Deprecated
    public Effect removeAfterwards(Graphic2D... graphics) {
        return linkGraphics(graphics);
    }

    @Override
    public void update(SceneContext context, float deltaTime) {
        if (!completed) {
            updateHandlers(context, deltaTime);

            if (checkCompleted()) {
                completed = true;
            }
        }
    }

    private void updateHandlers(SceneContext context, float deltaTime) {
        for (Updatable frameHandler : frameHandlers) {
            frameHandler.update(deltaTime);
        }

        for (Pointer pointer : context.getInput().getPointers()) {
            for (ClickHandler clickHandler : clickHandlers) {
                if (pointer.isReleased(clickHandler.bounds.get())) {
                    clickHandler.action.run();
                    context.getInput().clearPointerState();
                }
            }
        }
    }

    private boolean checkCompleted() {
        if (completionConditions.isEmpty()) {
            return false;
        }

        return completionConditions.stream()
            .allMatch(BooleanSupplier::getAsBoolean);
    }

    @Override
    public void end(SceneContext context) {
        for (Runnable completionHandler : completionHandlers) {
            completionHandler.run();
        }

        for (Graphic2D graphic : linkedGraphics) {
            graphic.detach();
        }
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Deprecated
    public void withLinkedGraphics(Consumer<Graphic2D> callback) {
        linkedGraphics.forEach(callback);
    }

    @Deprecated
    public <T extends Graphic2D> void withLinkedGraphics(Class<T> type, Consumer<T> callback) {
        linkedGraphics.stream()
            .filter(graphic -> graphic.getClass().equals(type))
            .forEach(graphic -> callback.accept((T) graphic));
    }

    /**
     * Attaches this effect to the specified scene context. Using this method
     * is identical to {@code sceneContext.attach(effect)}, but is more
     * readable when creating effects using the fluent API.
     */
    public Effect attach(SceneContext context) {
        context.attach(this);
        return this;
    }

    /**
     * Creates an effect that will use the specified callback every frame for
     * as long the effect is active.
     */
    public static Effect forFrameHandler(Updatable action) {
        Effect effect = new Effect();
        effect.addFrameHandler(action);
        return effect;
    }

    /**
     * Creates an effect that will use the specified callback every frame for
     * as long the effect is active.
     */
    public static Effect forFrameHandler(Runnable action) {
        return forFrameHandler(deltaTime -> action.run());
    }

    /**
     * Creates an effect that will first wait for the specified period of
     * time, and will then perform an action.
     */
    public static Effect delay(float duration, Runnable action) {
        Timer timer = new Timer(duration);
        AtomicBoolean done = new AtomicBoolean(false);

        Effect effect = new Effect();
        effect.addFrameHandler(deltaTime -> {
            timer.update(deltaTime);
            if (timer.isCompleted() && !done.get()) {
                action.run();
                done.set(true);
            }
        });
        effect.stopIf(timer::isCompleted);
        return effect;
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

    public static Effect forClickHandler(Graphic2D graphic, Runnable handler) {
        Effect effect = new Effect();
        effect.linkGraphics(graphic);
        effect.addClickHandler(graphic, handler);
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
     * Changes a sprite's scale until it fits the canvas. The {@code uniform}
     * parameter controls how the sprite should handle situations where the
     * aspect ratio of {@code bound} differs from the sprite's own aspect
     * ratio. When true, the sprite's horizontal and vertical scale will
     * always be set to the same values. When false, they can be different,
     * meaning the sprite could appear as stretched or squashed in certain
     * situations.
     */
    public static Effect scaleToFit(Sprite sprite, Canvas canvas, boolean uniform) {
        return Effect.forFrameHandler(() -> {
            float scaleX = (float) canvas.getWidth() / (float) sprite.getCurrentWidth();
            float scaleY = (float) canvas.getHeight() / (float) sprite.getCurrentHeight();

            if (uniform) {
                sprite.getTransform().setScale(Math.max(scaleX, scaleY) * 100f);
            } else {
                sprite.getTransform().setScale(scaleX * 100f, scaleY * 100f);
            }
        });
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

    /**
     * Defines a click handler from the area that should trigger the action
     * and the action itself.
     */
    private record ClickHandler(Supplier<Rect> bounds, Runnable action) {
    }
}
