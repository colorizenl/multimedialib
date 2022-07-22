//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import nl.colorize.multimedialib.graphics.Align;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.multimedialib.mock.MockScene;
import nl.colorize.multimedialib.mock.MockSceneContext;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.util.animation.Timeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EffectTest {

    private SceneContext context;

    private static final float EPSILON = 0.001f;

    @BeforeEach
    public void before() {
        context = new MockSceneContext();
        context.changeScene(new MockScene());
    }

    @Test
    public void testEffect() {
        Timeline timeline = new Timeline();
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(1f, 10f);
        timeline.addKeyFrame(2f, 30f);

        Effect effect = Effect.forImage(new MockImage(), timeline);
        effect.modify(value -> effect.getPosition().setY(value));
        effect.attachTo(context);

        effect.update(context, 0.5f);
        assertEquals(5f, effect.getPosition().getY(), EPSILON);
        effect.update(context, 0.5f);
        assertEquals(10f, effect.getPosition().getY(), EPSILON);
        effect.update(context, 0.5f);
        assertEquals(20f, effect.getPosition().getY(), EPSILON);
        effect.update(context, 0.5f);
        assertEquals(30f, effect.getPosition().getY(), EPSILON);
        effect.update(context, 0.5f);
        assertEquals(30f, effect.getPosition().getY(), EPSILON);
    }

    @Test
    public void testForSpriteX() {
        Sprite sprite = new Sprite();
        sprite.addState("mock", new MockImage());

        Timeline timeline = new Timeline();
        timeline.addKeyFrame(0f, 10f);
        timeline.addKeyFrame(2f, 20f);

        Effect effect = Effect.forSpriteX(sprite, timeline);
        effect.attachTo(context);

        assertEquals(0f, effect.getPosition().getX(), EPSILON);
        effect.update(context, 0.00001f);
        assertEquals(10f, effect.getPosition().getX(), EPSILON);
        effect.update(context, 1f);
        assertEquals(15f, effect.getPosition().getX(), EPSILON);
        effect.update(context, 1f);
        assertEquals(20f, effect.getPosition().getX(), EPSILON);
    }

    @Test
    public void testForTextAlpha() {
        Timeline timeline = new Timeline();
        timeline.addKeyFrame(0f, 100f);
        timeline.addKeyFrame(2f, 0f);

        Effect effect = Effect.forTextAlpha("test", new TTFont("test", 12, ColorRGB.BLACK, false),
            Align.LEFT, timeline);
        effect.attachTo(context);

        assertEquals(100f, effect.getTransform().getAlpha(), EPSILON);
        effect.update(context, 0.001f);
        assertEquals(100f, effect.getTransform().getAlpha(), EPSILON);
        effect.update(context, 1f);
        assertEquals(50f, effect.getTransform().getAlpha(), EPSILON);
        effect.update(context, 1f);
        assertEquals(0f, effect.getTransform().getAlpha(), EPSILON);
    }

    @Test
    public void testAddCompletionObserver() {
        Timeline timeline = new Timeline();
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(1f, 10f);

        Effect effect = Effect.forImage(new MockImage(), timeline);
        effect.attachTo(context);
        AtomicInteger counter = new AtomicInteger();
        effect.onComplete(counter::incrementAndGet);

        effect.update(context, 0.3f);
        effect.update(context, 0.3f);
        effect.update(context, 0.3f);
        effect.update(context, 0.3f);

        assertEquals(1, counter.get());
    }

    @Test
    public void testFrameUpdateModifiers() {
        List<Float> values = new ArrayList<>();
        List<Float> frameUpdates = new ArrayList<>();

        Timeline timeline = new Timeline();
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(10f, 1f);

        Effect effect = Effect.forImage(new MockImage(), timeline);
        effect.modify(values::add);
        effect.modifyFrameUpdate(frameUpdates::add);
        effect.attachTo(context);

        effect.update(context, 1f);
        effect.update(context, 1f);

        assertEquals(2, values.size());
        assertEquals(0.1f, values.get(0), EPSILON);
        assertEquals(0.2f, values.get(1), EPSILON);

        assertEquals(2, frameUpdates.size());
        assertEquals(1f, frameUpdates.get(0), EPSILON);
        assertEquals(1f, frameUpdates.get(1), EPSILON);
    }
}
