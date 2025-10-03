//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.mock.MockScene;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FluentSceneTest {

    @Test
    void lifeCycle() {
        List<String> frames = new ArrayList<>();
        FluentScene scene = new FluentScene(deltaTime -> frames.add("frame"))
            .withCompletion(() -> frames.size() >= 3, () -> frames.add("end"));

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
}
