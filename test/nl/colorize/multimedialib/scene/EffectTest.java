//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.multimedialib.mock.MockScene;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import nl.colorize.multimedialib.stage.Animation;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.util.animation.Timeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EffectTest {

    private SceneContext context;

    private static final float EPSILON = 0.001f;

    @BeforeEach
    public void before() {
        HeadlessRenderer renderer = new HeadlessRenderer();
        context = renderer.getContext();
        context.changeScene(new MockScene());
    }

    @Test
    public void testAddCompletionObserver() {
        Timeline timeline = new Timeline();
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(1f, 10f);

        AtomicInteger counter = new AtomicInteger();

        Effect.forTimeline(timeline, value -> {})
            .addCompletionHandler(counter::incrementAndGet)
            .attach(context);

        context.update(0.3f);
        context.update(0.3f);
        context.update(0.3f);
        context.update(0.3f);

        assertEquals(1, counter.get());
    }

    @Test
    public void testFrameUpdateModifiers() {
        List<Float> values = new ArrayList<>();
        List<Float> frameUpdates = new ArrayList<>();

        Timeline timeline = new Timeline();
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(10f, 1f);

        Effect effect = Effect.forFrameHandler(frameUpdates::add)
            .addTimelineHandler(timeline, values::add)
            .attach(context);

        effect.update(context, 1f);
        effect.update(context, 2f);

        assertEquals(2, values.size());
        assertEquals(0.1f, values.get(0), EPSILON);
        assertEquals(0.3f, values.get(1), EPSILON);

        assertEquals(2, frameUpdates.size());
        assertEquals(1f, frameUpdates.get(0), EPSILON);
        assertEquals(2f, frameUpdates.get(1), EPSILON);
    }

    @Test
    void stopAfterDuration() {
        List<String> frames = new ArrayList<>();

        Effect.forFrameHandler(deltaTime -> frames.add("1"))
            .stopAfter(2f)
            .attach(context);

        context.update(1f);
        context.update(1f);
        context.update(1f);

        assertEquals(List.of("1", "1"), frames);
    }

    @Test
    void stopAfterCondition() {
        List<String> frames = new ArrayList<>();

        Effect.forFrameHandler(deltaTime -> frames.add("1"))
            .stopIf(() -> frames.size() >= 2)
            .attach(context);

        context.update(1f);
        context.update(1f);
        context.update(1f);

        assertEquals(List.of("1", "1"), frames);
    }

    @Test
    void delayedAction() {
        List<String> events = new ArrayList<>();
        Effect effect = Effect.delay(2f, () -> events.add("delayed"));
        effect.addFrameHandler(deltaTime -> events.add("frame"));
        context.attach(effect);

        context.update(1f);

        assertFalse(effect.isCompleted());
        assertEquals(List.of("frame"), events);

        context.update(1f);

        assertTrue(effect.isCompleted());
        assertEquals(List.of("frame", "delayed", "frame"), events);

        context.update(1f);

        assertTrue(effect.isCompleted());
        assertEquals(List.of("frame", "delayed", "frame"), events);
    }

    @Test
    void completeAfterSpriteAnimation() {
        List<String> events = new ArrayList<>();

        Animation animation = new Animation(List.of(new MockImage(), new MockImage()), 1f, false);
        Sprite sprite = new Sprite(animation);
        context.getStage().getRoot().addChild(sprite);

        Effect.forFrameHandler(deltaTime -> events.add("frame"))
            .addCompletionHandler(() -> events.add("complete"))
            .stopAfterAnimation(sprite)
            .attach(context);

        context.update(1f);
        context.update(1f);
        context.update(1f);

        assertEquals(List.of("frame", "frame", "complete"), events);
    }

    @Test
    void multipleCompletionConditions() {
        List<String> events = new ArrayList<>();

        Effect.delay(2f, () -> events.add("a"))
            .addFrameHandler(deltaTime -> events.add("b"))
            .stopAfter(3f)
            .attach(context);

        context.update(1f);
        context.update(1f);
        context.update(1f);
        context.update(1f);
        context.update(1f);

        assertEquals(List.of("b", "a", "b", "b"), events);
    }

    @Test
    void scaleToFit() {
        Canvas canvas = new Canvas(800, 600, ScaleStrategy.flexible());

        Sprite sprite = new Sprite();
        sprite.addGraphics("a", new MockImage(100, 100));

        Effect effect = Effect.scaleToFit(sprite, canvas, false);
        context.attach(effect);
        context.update(1f);

        assertEquals(800f, sprite.getTransform().getScaleX(), EPSILON);
        assertEquals(600f, sprite.getTransform().getScaleY(), EPSILON);
    }

    @Test
    void scaleToFitUniform() {
        Canvas canvas = new Canvas(800, 600, ScaleStrategy.flexible());

        Sprite sprite = new Sprite();
        sprite.addGraphics("a", new MockImage(100, 100));

        Effect effect = Effect.scaleToFit(sprite, canvas, true);
        context.attach(effect);
        context.update(1f);

        assertEquals(800f, sprite.getTransform().getScaleX(), EPSILON);
        assertEquals(800f, sprite.getTransform().getScaleY(), EPSILON);
    }
}
