//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import nl.colorize.multimedialib.mock.MockScene;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EffectTest {

    private HeadlessRenderer context;

    private static final float EPSILON = 0.001f;

    @BeforeEach
    public void before() {
        context = new HeadlessRenderer(false);
        context.changeScene(new MockScene());
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
}
