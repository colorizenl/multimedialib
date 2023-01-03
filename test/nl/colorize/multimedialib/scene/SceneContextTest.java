//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.mock.MockScene;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SceneContextTest {

    @Test
    public void testInitialScene() {
        MockScene sceneA = new MockScene();
        MockScene sceneB = new MockScene();

        SceneContext app = new SceneContext(new HeadlessRenderer(), sceneA);
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

        SceneContext app = new SceneContext(new HeadlessRenderer(), sceneA);
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

        SceneContext app = new SceneContext(new HeadlessRenderer(), sceneA);
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

        SceneContext app = new SceneContext(new HeadlessRenderer(), sceneA);
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

        SceneContext app = new SceneContext(new HeadlessRenderer(), sceneA);
        app.attach((context, deltaTime) -> tracker.add("a"));
        app.attach((context, deltaTime) -> tracker.add("b"));
        app.update(1f);
        app.update(1f);
        app.changeScene(sceneB);
        app.attach((context, deltaTime) -> tracker.add("c"));
        app.update(1f);

        assertEquals(List.of("a", "b", "a", "b", "c"), tracker);
    }

    @Test
    void addSystemDuringIteration() {
        SceneContext context = new SceneContext(new HeadlessRenderer(), new MockScene());

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

        assertEquals(List.of("a", "a", "a", "b", "a", "b"), buffer);
    }

    @Test
    void completedSubSceneIsStopped() {
        MockScene parent = new MockScene();
        MockScene child = new MockScene();
        SceneContext context = new SceneContext(new HeadlessRenderer(), parent);
        context.attach(child);
        context.update(1f);
        context.update(1f);
        child.setCompleted(true);
        context.update(1f);

        assertFalse(parent.isCompleted());
        assertTrue(child.isCompleted());
        assertEquals(3, parent.getFrameUpdateCount());
        assertEquals(2, child.getFrameUpdateCount());
    }

    @Test
    void completedParentSceneIsNotStopped() {
        MockScene parent = new MockScene();
        SceneContext context = new SceneContext(new HeadlessRenderer(), parent);
        context.update(1f);
        context.update(1f);
        parent.setCompleted(true);
        context.update(1f);

        assertTrue(parent.isCompleted());
        assertEquals(3, parent.getFrameUpdateCount());
    }

    @Test
    void startSubScene() {
        MockScene parent = new MockScene();
        MockScene child1 = new MockScene();
        MockScene child2 = new MockScene();

        SceneContext context = new SceneContext(new HeadlessRenderer(), parent);
        context.attach(child1);
        context.update(1f);
        context.attach(child2);
        context.update(1f);

        assertEquals(1, parent.getStartCount());
        assertEquals(1, child1.getStartCount());
        assertEquals(1, child2.getStartCount());
    }

    @Test
    void startSubSceneAttachedToUpcomingScene() {
        MockScene parent = new MockScene();
        MockScene child1 = new MockScene();
        MockScene newParent = new MockScene();
        MockScene child2 = new MockScene();

        SceneContext context = new SceneContext(new HeadlessRenderer(), parent);
        context.attach(child1);
        context.update(1f);
        context.changeScene(newParent);
        context.attach(child2);
        context.update(1f);
        context.attach(child1);
        context.update(1f);

        assertEquals(1, parent.getStartCount());
        assertEquals(2, child1.getStartCount());
        assertEquals(1, newParent.getStartCount());
        assertEquals(1, child2.getStartCount());
    }
}
