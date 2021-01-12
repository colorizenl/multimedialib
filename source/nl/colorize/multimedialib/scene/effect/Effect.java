//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.Align;
import nl.colorize.multimedialib.graphics.Animation;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.renderer.Drawable;
import nl.colorize.multimedialib.renderer.GraphicsContext2D;
import nl.colorize.multimedialib.renderer.Updatable;
import nl.colorize.multimedialib.scene.SubScene;
import nl.colorize.util.animation.Interpolation;
import nl.colorize.util.animation.Timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * An animated graphical effect that can be played as part of a scene. Effects
 * implement the {@link SubScene} interface and can be attached to the currently
 * active scene. This allows declarative effects that can be played without
 * having to manually update their logic and graphics every frame.
 * <p>
 * The effect consists of the graphics that are animated according to the
 * timeline. A number of modifiers can be added to the effect, that will use
 * the timeline's current value to update how the effect should be displayed.
 * These modifiers are represented by callback functions, that are called every
 * frame based on the timeline's current value. The effect's graphics are then
 * rendered based on its updated state.
 * <p>
 * There are two types of effects that can be created using the factory methods
 * in this class. The first are "plain" effects that do not define any behavior.
 * The second are shorthand versions, for example to move a sprite or to change
 * text's alpha value. Note that these shorthand effects can still be extended
 * by adding additional modifiers, so that more complex effects can be created
 * from these starting points.
 * <p>
 * Observers can be added to the effect, and will be notified when the effect
 * has completed. This makes it easier to schedule follow-up events without
 * having to poll the status of the event every frame.
 */
public abstract class Effect implements SubScene {

    private Timeline timeline;
    private List<Consumer<Float>> modifiers;
    private List<Updatable> frameUpdateHandlers;
    private List<Runnable> completeHandlers;

    private Point2D position;
    private Transform transform;
    private boolean background;

    protected Effect(Timeline timeline) {
        this.timeline = timeline;
        this.modifiers = new ArrayList<>();
        this.frameUpdateHandlers = new ArrayList<>();
        this.completeHandlers = new ArrayList<>();

        this.position = new Point2D(0, 0);
        this.transform = new Transform();
        this.background = false;
    }

    protected Effect(float duration) {
        this(new Timeline().addKeyFrame(0f, 0f).addKeyFrame(duration, 1f));
    }

    /**
     * Registers a callback function that will be called during every frame
     * update, with the effect's current timeline value as the argument. The
     * callback can then be used to update the effect's graphical appearance.
     * Returns this effect to allow for method chaining.
     */
    public Effect modify(Consumer<Float> modifier) {
        modifiers.add(modifier);
        return this;
    }

    /**
     * Registers a callback function that is updated every frame. Unlike
     * "regular" modifiers registered using {@link #modify(Consumer)}, these
     * callbacks do not use the current timeline value. Returns this effect to
     * allow for method chaining.
     */
    public Effect modifyFrameUpdate(Updatable modifier) {
        frameUpdateHandlers.add(modifier);
        return this;
    }

    /**
     * Registers a callback function that will be notified once this effect
     * has been completed. Returns this effect to allow for method chaining.
     */
    public Effect onComplete(Runnable observer) {
        completeHandlers.add(observer);
        return this;
    }

    protected Timeline getTimeline() {
        return timeline;
    }

    public void setPosition(Point2D position) {
        this.position = position;
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    public Point2D getPosition() {
        return position;
    }

    public void setTransform(Transform transform) {
        this.transform = transform;
    }

    public Transform getTransform() {
        return transform;
    }

    /**
     * Indicates that this effect should be rendered as part of the scene's
     * background graphics. By default, effects are drawn in the foreground,
     * in front of the scene's own graphics.
     */
    public void inBackground() {
        background = true;
    }

    @Override
    public boolean hasBackgroundGraphics() {
        return background;
    }

    @Override
    public void update(float deltaTime) {
        if (isCompleted()) {
            return;
        }

        timeline.onFrame(deltaTime);
        modifiers.forEach(modifier -> modifier.accept(timeline.getValue()));
        frameUpdateHandlers.forEach(callback -> callback.update(deltaTime));

        if (isCompleted()) {
            completeHandlers.forEach(Runnable::run);
        }
    }

    @Override
    public abstract void render(GraphicsContext2D graphics);

    @Override
    public boolean isCompleted() {
        return timeline.isCompleted() && !timeline.isLoop();
    }

    /**
     * Shorthand for creating a graphical effect that will draw graphics using
     * the provided callback function.
     */
    public static Effect forGraphics(float duration, Drawable callback) {
        return new Effect(duration) {
            @Override
            public void render(GraphicsContext2D graphics) {
                callback.render(graphics);
            }
        };
    }

    /**
     * Shorthand for creating a graphical effect that will operate on the
     * specified sprite.
     */
    public static Effect forSprite(Sprite sprite, Timeline timeline) {
        return new Effect(timeline) {
            @Override
            public void update(float deltaTime) {
                super.update(deltaTime);
                sprite.update(deltaTime);
            }

            @Override
            public void render(GraphicsContext2D graphics) {
                sprite.setPosition(getPosition());
                sprite.setTransform(getTransform());
                graphics.drawSprite(sprite);
            }
        };
    }
    
    public static Effect forSprite(Sprite sprite, float duration) {
        Timeline timeline = new Timeline();
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(duration, 1f);

        return forSprite(sprite, timeline);
    }

    /**
     * Shorthand for creating an effect that modifies the sprite's X position
     * based on a timeline.
     */
    public static Effect forSpriteX(Sprite sprite, Timeline timeline) {
        Effect effect = forSprite(sprite, timeline);
        effect.modify(value -> effect.getPosition().setX(value));
        return effect;
    }

    /**
     * Shorthand for creating an effect that modifies the sprite's Y position
     * based on a timeline.
     */
    public static Effect forSpriteY(Sprite sprite, Timeline timeline) {
        Effect effect = forSprite(sprite, timeline);
        effect.modify(value -> effect.getPosition().setY(value));
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

        Effect effect = forSprite(sprite, timeline);
        effect.modify(value -> effect.getTransform().setRotation(Math.round(value)));
        return effect;
    }

    /**
     * Shorthand for creating an effect that modifies the sprite's alpha value
     * based on a timeline.
     */
    public static Effect forSpriteAlpha(Sprite sprite, Timeline timeline) {
        Effect effect = forSprite(sprite, timeline);
        effect.modify(value -> effect.getTransform().setAlpha(Math.round(value)));
        return effect;
    }

    /**
     * Creates an effect based on an animation, that will remain active based on
     * the specified timeline.
     */
    public static Effect forAnimation(Animation anim, Timeline timeline) {
        Sprite sprite = new Sprite();
        sprite.addState("_effect", anim);
        return forSprite(sprite, timeline);
    }

    /**
     * Creates an effect based on an animation, that will remain active for the
     * specified duration in seconds.
     */
    public static Effect forAnimation(Animation anim, float duration) {
        Timeline timeline = new Timeline();
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(duration, 0f);

        return forAnimation(anim, timeline);
    }
    
    /**
     * Creates an effect based on an animation, that will remain active for the
     * duration of the animation.
     */
    public static Effect forAnimation(Animation anim) {
        return forAnimation(anim, anim.getDuration());
    }

    public static Effect forImage(Image image, Timeline timeline) {
        return forAnimation(new Animation(image), timeline);
    }

    public static Effect forImage(Image image, float duration) {
        return forAnimation(new Animation(image), duration);
    }

    public static Effect forText(String text, TTFont font, Align align, Timeline timeline) {
        return new Effect(timeline) {
            @Override
            public void render(GraphicsContext2D graphics) {
                graphics.drawText(text, font, getPosition().getX(), getPosition().getY(),
                    align, getTransform());
            }
        };
    }

    public static Effect forText(String text, TTFont font, Align align, float duration) {
        Timeline timeline = new Timeline()
            .addKeyFrame(0f, 0f)
            .addKeyFrame(duration, 1f);

        return forText(text, font, align, timeline);
    }

    /**
     * Shorthand for creating an effect that will make the text slowly appear
     * over time, with more and more characters appearing on screen over time
     * until the entire text is shown.
     */
    public static Effect forTextAppear(String text, TTFont font, Align align, float duration) {
        Preconditions.checkArgument(text.length() > 0, "Cannot animate empty text");
        Preconditions.checkArgument(duration > 0f, "Invalid duration: " + duration);

        Timeline timeline = new Timeline();
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(duration, text.length());

        return new Effect(timeline) {
            @Override
            public void render(GraphicsContext2D graphics) {
                String visibleText = text.substring(0, (int) timeline.getValue());
                graphics.drawText(visibleText, font, getPosition().getX(), getPosition().getY(),
                    align, getTransform());
            }
        };
    }

    /**
     * Shorthand for creating an effect that modifies the text's alpha value
     * based on a timeline.
     */
    public static Effect forTextAlpha(String text, TTFont font, Align align, Timeline timeline) {
        Effect effect = forText(text, font, align, timeline);
        effect.modify(value -> effect.getTransform().setAlpha(Math.round(value)));
        return effect;
    }
}
