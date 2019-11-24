//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.graphics.Align;
import nl.colorize.multimedialib.graphics.Animation;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.math.Point;
import nl.colorize.multimedialib.renderer.GraphicsContext;
import nl.colorize.util.animation.Timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * An animated graphical effect that can be played as part of a scene. In
 * combination with the {@link EffectManager}, this allows declarative effects
 * that can be played without having to manually update their logic and
 * graphics every frame.
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
 */
public abstract class Effect implements Updatable, Renderable {

    private Timeline timeline;
    private List<Consumer<Float>> modifiers;

    private Point position;
    private Transform transform;

    private Effect(Timeline timeline) {
        this.timeline = timeline;
        this.modifiers = new ArrayList<>();

        this.position = new Point(0, 0);
        this.transform = new Transform();
    }

    /**
     * Registers a callback function that will be called during every frame
     * update, with the effect's current timeline value as the argument. The
     * callback can then be used to update the effect's graphical appearance.
     */
    public void modify(Consumer<Float> modifier) {
        modifiers.add(modifier);
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public Point getPosition() {
        return position;
    }

    public void setTransform(Transform transform) {
        this.transform = transform;
    }

    public Transform getTransform() {
        return transform;
    }

    @Override
    public void update(float deltaTime) {
        timeline.onFrame(deltaTime);

        for (Consumer<Float> modifier : modifiers) {
            modifier.accept(timeline.getValue());
        }
    }

    public boolean isCompleted() {
        return timeline.isCompleted();
    }

    @Override
    public abstract void render(GraphicsContext graphics);

    public static Effect forSprite(Sprite sprite, Timeline timeline) {
        return new Effect(timeline) {
            @Override
            public void render(GraphicsContext graphics) {
                graphics.drawSprite(sprite, getPosition().getX(), getPosition().getY(), getTransform());
            }
        };
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
     * Shorthand for creating an effect that modifies the sprite's alpha value
     * based on a timeline.
     */
    public static Effect forSpriteAlpha(Sprite sprite, Timeline timeline) {
        Effect effect = forSprite(sprite, timeline);
        effect.modify(value -> effect.getTransform().setAlpha(Math.round(value)));
        return effect;
    }

    public static Effect forAnimation(Animation anim, Timeline timeline) {
        Sprite sprite = new Sprite();
        sprite.addState("_effect", anim);
        return forSprite(sprite, timeline);
    }

    public static Effect forImage(Image image, Timeline timeline) {
        Sprite sprite = new Sprite();
        sprite.addState("_effect", image);
        return forSprite(sprite, timeline);
    }

    public static Effect forText(String text, TTFont font, Align align, Timeline timeline) {
        return new Effect(timeline) {
            @Override
            public void render(GraphicsContext graphics) {
                graphics.drawText(text, font, getPosition().getX(), getPosition().getY(),
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
