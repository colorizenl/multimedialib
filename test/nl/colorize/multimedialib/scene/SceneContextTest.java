//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.mock.MockScene;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SceneContextTest {

    @Test
    public void testInitialScene() {
        MockScene sceneA = new MockScene();
        MockScene sceneB = new MockScene();

        SceneContext app = new SceneContext(null, null, null, null);
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

        SceneContext app = new SceneContext(null, null, null, null);
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

        SceneContext app = new SceneContext(null, null, null, null);
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
    void subScenesShouldBeAttachedToParent() {
        MockScene sceneA = new MockScene();
        List<String> tracker = new ArrayList<>();

        SceneContext app = new SceneContext(null, null, null, null);
        app.changeScene(sceneA);
        app.attachAgent(deltaTime -> tracker.add("a"));
        app.attachAgent(deltaTime -> tracker.add("b"));
        app.update(1f);

        assertEquals(ImmutableList.of("a", "b"), tracker);
    }

    @Test
    void subScenesShouldEndWhenSceneIsChanged() {
        MockScene sceneA = new MockScene();
        MockScene sceneB = new MockScene();
        List<String> tracker = new ArrayList<>();

        SceneContext app = new SceneContext(null, null, null, null);
        app.changeScene(sceneA);
        app.attachAgent(deltaTime -> tracker.add("a"));
        app.attachAgent(deltaTime -> tracker.add("b"));
        app.update(1f);
        app.update(1f);
        app.changeScene(sceneB);
        app.attachAgent(deltaTime -> tracker.add("c"));
        app.update(1f);

        assertEquals(ImmutableList.of("a", "b", "a", "b", "c"), tracker);
    }

    @Test
    void cancelAgent() {
        MockScene sceneA = new MockScene();
        List<String> tracker = new ArrayList<>();
        Agent subSceneA = deltaTime -> tracker.add("a");
        Agent subSceneB = deltaTime -> tracker.add("b");

        SceneContext app = new SceneContext(null, null, null, null);
        app.changeScene(sceneA);
        app.attachAgent(subSceneA);
        app.attachAgent(subSceneB);
        app.update(1f);
        app.cancelAgent(subSceneB);
        app.update(1f);

        assertEquals(ImmutableList.of("a", "b", "a"), tracker);
    }
}
