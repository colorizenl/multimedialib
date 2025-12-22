//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.mock.MockScene;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FluentSceneTest {

    @Test
    void lifeCycle() {
        List<String> frames = new ArrayList<>();
        FluentScene scene = FluentScene.create()
            .withFrameHandler(_ -> frames.add("frame"))
            .withCompletionCheck(() -> frames.size() >= 3)
            .withCompletionHandler(() -> frames.add("end"));

        HeadlessRenderer context = new HeadlessRenderer(false);
        context.changeScene(new MockScene());
        context.attach(scene);
        context.doFrame(1f);
        context.doFrame(1f);
        context.doFrame(1f);
        context.doFrame(1f);
        context.doFrame(1f);

        assertEquals(List.of("frame", "frame", "frame", "end"), frames);
        assertTrue(scene.isCompleted());
    }

    @Test
    void combineMultipleFrameHandlers() {
        List<String> actions = new ArrayList<>();

        FluentScene scene = FluentScene.create()
            .withFrameHandler(_ -> actions.add("a"))
            .withFrameHandler(_ -> actions.add("b"))
            .withCompletionCheck(() -> actions.size() >= 4)
            .withCompletionHandler(() -> actions.add("c"))
            .withCompletionHandler(() -> actions.add("d"));

        HeadlessRenderer context = new HeadlessRenderer(false);
        context.changeScene(new MockScene());
        context.attach(scene);
        context.doFrame(1f);
        context.doFrame(1f);
        context.doFrame(1f);

        assertEquals(List.of("a", "b", "a", "b", "c", "d"), actions);
    }

    @Test
    void multipleCompletionChecks() {
        List<String> actions = new ArrayList<>();

        FluentScene scene = FluentScene.create()
            .withFrameHandler(_ -> actions.add("a"))
            .withCompletionCheck(() -> actions.size() >= 3)
            .withCompletionCheck(() -> actions.size() >= 2);

        HeadlessRenderer context = new HeadlessRenderer(false);
        context.changeScene(new MockScene());
        context.attach(scene);
        context.doFrame(1f);
        context.doFrame(1f);

        assertFalse(scene.isCompleted());

        context.doFrame(1f);

        assertTrue(scene.isCompleted());
    }
}
