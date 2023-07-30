//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.multimedialib.mock.MockScene;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.util.Stopwatch;
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
        context = new SceneContext(new HeadlessRenderer(), new Stopwatch());
        context.changeScene(new MockScene());
    }

    @Test
    public void testAddCompletionObserver() {
        Timeline timeline = new Timeline();
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(1f, 10f);

        AtomicInteger counter = new AtomicInteger();

        Effect effect = new Effect();
        effect.addTimelineHandler(timeline, value -> {});
        effect.addCompletionHandler(counter::incrementAndGet);
        context.attach(effect);

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

        Effect effect = new Effect();
        effect.addFrameHandler(frameUpdates::add);
        effect.addTimelineHandler(timeline, values::add);
        context.attach(effect);

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
    void terminateHandler() {
        List<String> frames = new ArrayList<>();

        Effect effect = new Effect();
        effect.addFrameHandler(deltaTime -> frames.add("1"));
        effect.addCompletionHandler(() -> frames.add("2"));
        context.attach(effect);

        context.update(1f);
        context.update(1f);
        effect.complete();
        context.update(1f);

        assertEquals(List.of("1", "1", "2"), frames);
    }

    @Test
    void stopAfterDuration() {
        List<String> frames = new ArrayList<>();

        Effect effect = new Effect();
        effect.addFrameHandler(deltaTime -> frames.add("1"));
        effect.stopAfter(2f);
        context.attach(effect);

        context.update(1f);
        context.update(1f);
        context.update(1f);

        assertEquals(List.of("1", "1"), frames);
    }

    @Test
    void stopAfterCondition() {
        List<String> frames = new ArrayList<>();

        Effect effect = new Effect();
        effect.addFrameHandler(deltaTime -> frames.add("1"));
        effect.stopIf(() -> frames.size() >= 2);
        context.attach(effect);

        context.update(1f);
        context.update(1f);
        context.update(1f);

        assertEquals(List.of("1", "1"), frames);
    }

    @Test
    void effectThatRunsOnTimer() {
        List<String> events = new ArrayList<>();
        Effect effect = Effect.forTimer(2f, () -> events.add("complete"));
        effect.addFrameHandler(deltaTime -> events.add("frame"));
        context.attach(effect);

        context.update(1f);

        assertFalse(effect.isCompleted());
        assertEquals(List.of("frame"), events);

        context.update(1f);

        assertTrue(effect.isCompleted());
        assertEquals(List.of("frame", "frame", "complete"), events);

        context.update(1f);

        assertTrue(effect.isCompleted());
        assertEquals(List.of("frame", "frame", "complete"), events);
    }
}
