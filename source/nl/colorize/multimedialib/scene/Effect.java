//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.graphics.Animation;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Point;
import nl.colorize.multimedialib.renderer.GraphicsContext;
import nl.colorize.util.animation.Timeline;

import java.util.function.BiConsumer;

/**
 * An animated graphical effect that can be played as part of a scene. In
 * combination with the {@link EffectManager}, this allows declarative effects
 * that can be played without having to manually update their logic and
 * graphics every frame.
 * <p>
 * The effect consists of a sprite that is animated according to a timeline.
 * When the effect is created, a callback function is passed that is used to
 * determine how the sprite should be modified based on the timeline's value.
 * This callback is then called every frame, with the effect and the timeline's
 * current value as the arguments.. Afterwards, the sprite is rendered.
 */
public class Effect implements Updatable, Renderable {

    private Sprite sprite;
    private Point position;
    private Transform transform;

    private Timeline timeline;
    private BiConsumer<Effect, Float> callback;

    public Effect(Sprite sprite, Timeline timeline, BiConsumer<Effect, Float> callback) {
        this.sprite = sprite;
        this.position = new Point(0, 0);
        this.transform = new Transform();

        this.timeline = timeline;
        this.callback = callback;
    }

    public Effect(Animation anim, Timeline timeline, BiConsumer<Effect, Float> callback) {
        this(createSprite(anim), timeline, callback);
    }

    public Effect(Image image, Timeline timeline, BiConsumer<Effect, Float> callback) {
        this(createSprite(image), timeline, callback);
    }

    public Point getPosition() {
        return position;
    }

    public Transform getTransform() {
        return transform;
    }

    @Override
    public void update(float deltaTime) {
        timeline.onFrame(deltaTime);
        callback.accept(this, timeline.getValue());
    }

    public boolean isCompleted() {
        return timeline.isCompleted();
    }

    @Override
    public void render(GraphicsContext graphics) {
        graphics.drawSprite(sprite, position.getX(), position.getY(), transform);
    }

    private static Sprite createSprite(Animation graphics) {
        Sprite sprite = new Sprite();
        sprite.addState("_effect", graphics);
        return sprite;
    }

    private static Sprite createSprite(Image graphics) {
        Sprite sprite = new Sprite();
        sprite.addState("_effect", graphics);
        return sprite;
    }
}
