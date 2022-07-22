//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.mock.MockScene;
import nl.colorize.multimedialib.mock.MockSceneContext;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SceneContextTest {

    @Test
    public void testInitialScene() {
        MockScene sceneA = new MockScene();
        MockScene sceneB = new MockScene();

        SceneContext app = new MockSceneContext();
        app.changeScene(sceneA);
        app.update(1f);
        app.update(1f);

        assertEquals(1, sceneA.getStartCount());
        assertEquals(2, sceneA.getFrameUpdateCount());

        assertEquals(0, sceneB.getStartCount());
        assertEquals(0, sceneB.getFrameUpdateCount());
    }

    @Test
    public void testChangeScene() {
        MockScene sceneA = new MockScene();
        MockScene sceneB = new MockScene();

        SceneContext app = new MockSceneContext();
        app.changeScene(sceneA);
        app.update(1f);
        app.changeScene(sceneB);
        app.update(1f);
        app.update(1f);

        assertEquals(1, sceneA.getStartCount());
        assertEquals(1, sceneA.getFrameUpdateCount());

        assertEquals(1, sceneB.getStartCount());
        assertEquals(2, sceneB.getFrameUpdateCount());
    }

    @Test
    public void testEndScene() {
        MockScene sceneA = new MockScene();
        MockScene sceneB = new MockScene();

        SceneContext app = new MockSceneContext();
        app.changeScene(sceneA);
        app.update(1f);
        app.changeScene(sceneB);
        app.update(1f);

        assertEquals(1, sceneA.getStartCount());
        assertEquals(1, sceneA.getEndCount());
        assertEquals(1, sceneB.getStartCount());
        assertEquals(0, sceneB.getEndCount());
    }

    @Test
    void systemsShouldBeAttachedToParent() {
        MockScene sceneA = new MockScene();
        List<String> tracker = new ArrayList<>();

        SceneContext app = new MockSceneContext();
        app.changeScene(sceneA);
        app.attach((context, deltaTime) -> tracker.add("a"));
        app.attach((context, deltaTime) -> tracker.add("b"));
        app.update(1f);

        assertEquals(ImmutableList.of("a", "b"), tracker);
    }

    @Test
    void systemsShouldEndWhenSceneIsChanged() {
        MockScene sceneA = new MockScene();
        MockScene sceneB = new MockScene();
        List<String> tracker = new ArrayList<>();

        SceneContext app = new MockSceneContext();
        app.changeScene(sceneA);
        app.attach((context, deltaTime) -> tracker.add("a"));
        app.attach((context, deltaTime) -> tracker.add("b"));
        app.update(1f);
        app.update(1f);
        app.changeScene(sceneB);
        app.attach((context, deltaTime) -> tracker.add("c"));
        app.update(1f);

        assertEquals(ImmutableList.of("a", "b", "a", "b", "c"), tracker);
    }

    @Test
    void systemActiveForDuration() {
        SceneContext context = new MockSceneContext();
        context.changeScene(new MockScene());

        List<String> buffer = new ArrayList<>();
        context.attach(ActorSystem.timed(2f, () -> buffer.add("a")));
        context.update(1f);
        context.update(1f);
        context.update(1f);

        assertEquals(ImmutableList.of("a", "a"), buffer);
    }

    @Test
    void addSystemDuringIteration() {
        SceneContext context = new MockSceneContext();
        context.changeScene(new MockScene());

        List<String> buffer = new ArrayList<>();
        context.attach((ctx, dt) -> {
            buffer.add("a");
            if (buffer.size() == 2) {
                context.attach((ctx2, dt2) -> buffer.add("b"));
            }
        });
        context.update(1f);
        context.update(1f);
        context.update(1f);
        context.update(1f);

        assertEquals(ImmutableList.of("a", "a", "a", "b", "a", "b"), buffer);
    }

    @Test
    void delayedAction() {
        SceneContext context = new MockSceneContext();
        context.changeScene(new MockScene());

        List<String> buffer = new ArrayList<>();
        context.attach(deltaTime -> buffer.add("a"));
        context.delay(2f, () -> buffer.add("z"));
        context.update(1f);
        context.update(1f);
        context.update(1f);
        context.update(1f);

        assertEquals(ImmutableList.of("a", "a", "z", "a", "a"), buffer);
    }
}
