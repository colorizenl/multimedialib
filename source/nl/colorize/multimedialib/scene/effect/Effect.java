//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Pointer;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.Timer;
import nl.colorize.multimedialib.scene.Updatable;
import nl.colorize.multimedialib.stage.Animation;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.StageNode2D;
import nl.colorize.util.LogHelper;
import nl.colorize.util.animation.Timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

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
 *
 * @deprecated This class is a combination of graphics and logic. The former
 *             is best handled in application code, the latter is best handled
 *             by the various convenience methods to create sub-scenes in
 *             {@link SceneContext}. This class will be removed in a future
 *             version of MultimediaLib.
 */
@Deprecated
public final class Effect implements Scene {

    private List<Updatable> frameHandlers;
    private List<ClickHandler> clickHandlers;
    private List<Runnable> completionHandlers;
    private List<StageNode2D> linkedGraphics;
    private List<BooleanSupplier> completionConditions;
    private boolean completed;

    private static final Logger LOGGER = LogHelper.getLogger(Effect.class);

    /**
     * Creates a new effect that initially does not define any behavior and
     * is not linked to any graphics. Prefer using the static factory methods
     * to create effects with the desired behavior.
     */
    public Effect() {
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

    public Effect addFrameHandler(Runnable handler) {
        frameHandlers.add(deltaTime -> handler.run());
        return this;
    }

    /**
     * Adds a frame handler that will update the specified timer during every
     * update, then invokes the callback function based on the timer's new
     * value. This effect will be marked as completed once the timer ends.
     */
    public Effect addTimerHandler(Timer timer, Consumer<Float> callback) {
        addFrameHandler(deltaTime -> {
            timer.update(deltaTime);
            callback.accept(timer.getTime());
        });
        stopIf(() -> timer.isCompleted());
        return this;
    }

    /**
     * Adds a frame handler that will update the specified timeline during
     * every frame update, then invokes the callback function based on the
     * timeline's new value. The effect will be marked as completed once the
     * timeline ends.
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
        return addClickHandler(() -> bounds, handler);
    }

    public Effect addClickHandler(Supplier<Rect> bounds, Runnable handler) {
        clickHandlers.add(new ClickHandler(bounds, handler));
        return this;
    }

    public Effect addClickHandler(StageNode2D graphic, Runnable handler) {
        return addClickHandler(() -> graphic.getStageBounds(), handler);
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
        Animation currentStateGraphics = sprite.getCurrentStateGraphics();
        float duration = currentStateGraphics.getDuration();

        if (duration == 0f) {
            LOGGER.warning("Cannot bind effect to zero-length animation");
            return this;
        }

        return stopAfter(duration);
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
     * Adds a handler that will mark the effect as completed during the next
     * frame update.
     */
    public Effect stopNow() {
        completionConditions.add(() -> true);
        return this;
    }

    /**
     * Adds a handler that will always return false, and will therefore make
     * the effect continue indefinitely.
     */
    public Effect stopNever() {
        completionConditions.add(() -> false);
        return this;
    }

    /**
     * Links existing graphics to this effect. This means the graphics will be
     * removed from stage when the effect has completed.
     */
    public Effect linkGraphics(StageNode2D... graphics) {
        for (StageNode2D graphic : graphics) {
            linkedGraphics.add(graphic);
        }

        return this;
    }

    /**
     * Links existing graphics to this effect. This means the graphics will be
     * removed from stage when the effect has completed.
     *
     * @deprecated Use {@link #linkGraphics(StageNode2D...)} instead.
     */
    @Deprecated
    public Effect removeAfterwards(StageNode2D... graphics) {
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
                if (pointer.isReleased(clickHandler.bounds.get()) && checkVisible()) {
                    clickHandler.action.run();
                    context.getInput().clearPointerState();
                }
            }
        }
    }

    private boolean checkVisible() {
        if (linkedGraphics.isEmpty()) {
            return true;
        }

        return linkedGraphics.stream()
            .anyMatch(graphic -> graphic.getTransform().isVisible());
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

        for (StageNode2D graphic : linkedGraphics) {
            context.getStage().detach(graphic);
        }
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Deprecated
    public void withLinkedGraphics(Consumer<StageNode2D> callback) {
        linkedGraphics.forEach(callback);
    }

    @Deprecated
    public <T extends StageNode2D> void withLinkedGraphics(Class<T> type, Consumer<T> callback) {
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
     * Defines a click handler from the area that should trigger the action
     * and the action itself.
     */
    private record ClickHandler(Supplier<Rect> bounds, Runnable action) {
    }
}
