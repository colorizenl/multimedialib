//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.Align;
import nl.colorize.multimedialib.graphics.Animation;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.Primitive;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.graphics.Text;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.scene.DisplayObject;
import nl.colorize.multimedialib.scene.Layer;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.Updatable;
import nl.colorize.util.animation.Interpolation;
import nl.colorize.util.animation.Timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Animated graphical effects that can be played as part of a scene. Effects
 * are considered systems because they contain logic. This allows declarative
 * effects that can be played without having to manually update their logic
 * and graphics every frame.
 * <p>
 * This class is effectively a builder for {@link DisplayObject}s.
 * <p>
 * Older versions of this class actually extended {@link DisplayObject}, but
 * this is no longer needed since {@link DisplayObject} is now considerably
 * more flexible in terms of defining custom behavior. The old methods remain
 * for backward compatibility, though are now deprecated.
 */
public class Effect {

    protected DisplayObject displayObject;
    protected Timeline timeline;
    private Point2D position;
    private Transform transform;
    private List<Consumer<Float>> timelineCallbacks;

    protected Effect(Timeline timeline) {
        this.displayObject = new DisplayObject();
        this.timeline = timeline;
        this.position = new Point2D(0, 0);
        this.transform = new Transform();
        this.timelineCallbacks = new ArrayList<>();

        displayObject.withTimelineHandler(timeline, value -> {
            for (Consumer<Float> timelineCallback : timelineCallbacks) {
                timelineCallback.accept(value);
            }
        });
    }

    public Effect withLayer(Layer layer) {
        displayObject.withLayer(layer);
        return this;
    }

    public Effect withLayer(String layerName) {
        displayObject.withLayer(layerName);
        return this;
    }

    /**
     * Registers a callback function that will be called during every frame
     * update, with the effect's current timeline value as the argument. The
     * callback can then be used to update the effect's graphical appearance.
     * Returns this effect to allow for method chaining.
     *
     * @deprecated Prefer {@link DisplayObject#withTimelineHandler(Timeline, Consumer)}.
     */
    @Deprecated
    public Effect modify(Consumer<Float> modifier) {
        timelineCallbacks.add(modifier);
        return this;
    }

    /**
     * Registers a callback function that is updated every frame. Unlike
     * "regular" modifiers registered using {@link #modify(Consumer)}, these
     * callbacks do not use the current timeline value. Returns this effect to
     * allow for method chaining.
     *
     * @deprecated Prefer {@link DisplayObject#withFrameHandler(Updatable)}.
     */
    public Effect modifyFrameUpdate(Updatable modifier) {
        displayObject.withFrameHandler(modifier);
        return this;
    }

    /**
     * Registers a callback function that will be notified once this effect
     * has been completed. Returns this effect to allow for method chaining.
     *
     * @deprecated Prefer {@link DisplayObject#withTerminationHandler(Runnable)}.
     */
    @Deprecated
    public Effect onComplete(Runnable observer) {
        displayObject.withTerminationHandler(observer);
        return this;
    }

    @Deprecated
    public Effect setPosition(Point2D position) {
        this.position = position;
        return this;
    }

    @Deprecated
    public Effect setPosition(float x, float y) {
        this.position.set(x, y);
        return this;
    }

    @Deprecated
    public Point2D getPosition() {
        return position;
    }

    @Deprecated
    public Effect setTransform(Transform transform) {
        this.transform = transform;
        return this;
    }

    @Deprecated
    public Transform getTransform() {
        return transform;
    }

    public void attachTo(SceneContext context) {
        displayObject.attachTo(context);
    }

    /**
     * Forwards to {@link DisplayObject#update(SceneContext, float)}.
     */
    @Deprecated
    @VisibleForTesting
    protected void update(SceneContext context, float deltaTime) {
        displayObject.update(context, deltaTime);
    }

    /**
     * Shorthand for creating a graphical effect that will operate on the
     * specified sprite.
     */
    public static Effect forSprite(Sprite sprite, Timeline timeline) {
        Effect effect = new Effect(timeline);
        effect.displayObject.withGraphics(sprite);
        effect.modifyFrameUpdate(deltaTime -> {
            sprite.update(deltaTime);
            sprite.setPosition(effect.getPosition());
            sprite.setTransform(effect.getTransform());
        });
        return effect;
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

    public static Effect forPrimitive(Primitive primitive, Timeline timeline) {
        Effect effect = new Effect(timeline);
        effect.displayObject.withGraphics(primitive);
        effect.modifyFrameUpdate(deltaTime -> {
            primitive.update(deltaTime);
            primitive.getPosition().set(effect.getPosition());
        });
        return effect;
    }

    public static Effect forText(String text, TTFont font, Align align, Timeline timeline) {
        Text textObject = new Text(text, font, align);
        Effect effect = new Effect(timeline);
        effect.displayObject.withGraphics(textObject);
        effect.modifyFrameUpdate(deltaTime -> {
            textObject.getPosition().set(effect.getPosition());
            textObject.setAlpha(effect.getTransform().getAlpha());
        });
        return effect;
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

        Text textObject = new Text(text, font, align);

        Effect effect = new Effect(timeline);
        effect.displayObject.withGraphics(textObject);
        effect.modifyFrameUpdate(deltaTime -> {
            textObject.setText(text.substring(0, (int) timeline.getValue()));
            textObject.getPosition().set(effect.getPosition());
            textObject.setAlpha(effect.getTransform().getAlpha());
        });
        return effect;
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
