//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.mock.MockScene;
import nl.colorize.multimedialib.mock.MockStopwatch;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import nl.colorize.multimedialib.scene.effect.Effect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SceneContextTest {

    private Canvas canvas;
    private HeadlessRenderer renderer;

    @BeforeEach
    public void before() {
        canvas = new Canvas(800, 600, ScaleStrategy.scale());
        renderer = new HeadlessRenderer(canvas, 10);
    }

    @Test
    public void testInitialScene() {
        MockScene sceneA = new MockScene();
        MockScene sceneB = new MockScene();

        SceneContext app = renderer.getContext();
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

        SceneContext app = renderer.getContext();
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

        SceneContext app = renderer.getContext();
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

        SceneContext app = renderer.getContext();
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

        SceneContext app = renderer.getContext();
        app.changeScene(sceneA);
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
        SceneContext context = renderer.getContext();
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

        assertEquals(List.of("a", "a", "a", "b", "a", "b"), buffer);
    }

    @Test
    void completedSubSceneIsStopped() {
        MockScene parent = new MockScene();
        MockScene child = new MockScene();
        SceneContext context = renderer.getContext();
        context.changeScene(parent);
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
        SceneContext context = renderer.getContext();
        context.changeScene(parent);
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

        SceneContext context = renderer.getContext();
        context.changeScene(parent);
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

        SceneContext context = renderer.getContext();
        context.changeScene(parent);
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

    @Test
    void globalSceneIsNeverRemoved() {
        MockScene scene1 = new MockScene();
        MockScene scene2 = new MockScene();
        MockScene scene3 = new MockScene();
        MockScene scene4 = new MockScene();

        SceneContext context = renderer.getContext();
        context.changeScene(scene1);
        context.attach(scene2);
        context.attachGlobal(scene3);
        context.update(1f);
        context.changeScene(scene4);
        context.update(1f);

        assertEquals(1, scene1.getFrameUpdateCount());
        assertEquals(1, scene2.getFrameUpdateCount());
        assertEquals(2, scene3.getFrameUpdateCount());
        assertEquals(1, scene4.getFrameUpdateCount());
    }

    @Test
    void delayScene() {
        MockScene parent = new MockScene();
        MockScene child = new MockScene();

        SceneContext context = renderer.getContext();
        context.changeScene(parent);
        context.attach(Effect.delay(2f, () -> child.start(context)));

        context.update(1f);
        context.update(1f);
        context.update(1f);

        assertEquals(1, parent.getStartCount());
        assertEquals(3, parent.getFrameUpdateCount());
        assertEquals(1, child.getStartCount());
        assertEquals(0, child.getFrameUpdateCount());
    }

    @Test
    void nativeFramerate() {
        SceneContext context = renderer.getContext();
        context.replaceTimer(new MockStopwatch(1000, 1100, 1200));
        Counter counter = new Counter();
        context.changeScene(counter);

        for (int i = 0; i < 3; i++) {
            context.syncFrame();
        }

        assertEquals("[start, 0.10, 0.10]", counter.frames.toString());
    }

    @Test
    void applicationFramerateSlowerThanRefreshRate() {
        SceneContext context = renderer.getContext();
        context.replaceTimer(new MockStopwatch(1000, 1100, 1150, 1200, 1300));
        Counter counter = new Counter();
        context.changeScene(counter);

        for (int i = 0; i < 5; i++) {
            context.syncFrame();
        }

        assertEquals("[start, 0.10, 0.10, 0.10]", counter.frames.toString());
    }

    @Test
    void slowerFramerateWithNonExactMatch() {
        SceneContext context = renderer.getContext();
        context.replaceTimer(new MockStopwatch(1000, 1100, 1130, 1160, 1190, 1220, 1250, 1300));
        Counter counter = new Counter();
        context.changeScene(counter);

        for (int i = 0; i < 8; i++) {
            context.syncFrame();
        }

        assertEquals("[start, 0.10, 0.12]", counter.frames.toString());
    }

    @Test
    void applicationFramerateFasterThanRefreshRate() {
        SceneContext context = renderer.getContext();
        context.replaceTimer(new MockStopwatch(1000, 1200, 1400));
        Counter counter = new Counter();
        context.changeScene(counter);

        for (int i = 0; i < 3; i++) {
            context.syncFrame();
        }

        assertEquals("[start, 0.20, 0.20]", counter.frames.toString());
    }

    @Test
    void limitExtremelyLargeDeltaTime() {
        SceneContext context = renderer.getContext();
        context.replaceTimer(new MockStopwatch(1000, 1100, 9999));
        Counter counter = new Counter();
        context.changeScene(counter);

        for (int i = 0; i < 3; i++) {
            context.syncFrame();
        }

        assertEquals("[start, 0.10, 0.20]", counter.frames.toString());
    }

    @Test
    void allowFramesThatAreLittleBitTooShort() {
        SceneContext context = renderer.getContext();
        context.replaceTimer(new MockStopwatch(1000, 1100, 1195, 1300));
        Counter counter = new Counter();
        context.changeScene(counter);

        for (int i = 0; i < 4; i++) {
            context.syncFrame();
        }

        assertEquals("[start, 0.10, 0.09, 0.10]", counter.frames.toString());
    }

    @Test
    void immediatelySwitchSceneFromStartScene() {
        Counter a = new Counter();
        Counter b = new Counter();

        SceneContext context = renderer.getContext();
        context.changeScene(new Scene() {
            @Override
            public void start(SceneContext context) {
                a.start(context);
                context.changeScene(b);
            }

            @Override
            public void end(SceneContext context) {
                a.end(context);
            }

            @Override
            public void update(SceneContext context, float deltaTime) {
                a.update(context, 1f);
            }
        });
        context.replaceTimer(new MockStopwatch(1000, 1100, 1200, 1300, 1400));
        context.syncFrame();
        context.syncFrame();

        assertEquals(List.of("start", "end"), a.frames);
        assertEquals(List.of("start", "0.10", "0.10"), b.frames);
    }

    @Test
    void trackResize() {
        Counter counter = new Counter();

        SceneContext context = renderer.getContext();
        context.replaceTimer(new MockStopwatch(1000, 1100, 1200, 1300, 1400));
        context.changeScene(counter);
        context.syncFrame();
        canvas.resizeScreen(1000, 1000);
        context.syncFrame();
        context.syncFrame();

        assertEquals(List.of("start", "0.10", "resize", "0.10", "0.10"), counter.frames);
    }

    @Test
    void doNotTrackVerySmallResize() {
        Counter counter = new Counter();

        SceneContext context = renderer.getContext();
        context.replaceTimer(new MockStopwatch(1000, 1100, 1200, 1300, 1400));
        context.changeScene(counter);
        context.syncFrame();
        canvas.resizeScreen(810, 610);
        context.syncFrame();

        assertEquals(List.of("start", "0.10", "0.10"), counter.frames);
    }

    @Test
    void avoidStartingAttachedSubSceneTwice() {
        List<String> events = new ArrayList<>();

        Scene child = new Scene() {
            @Override
            public void start(SceneContext context) {
                events.add("child");
            }

            @Override
            public void update(SceneContext context, float deltaTime) {
            }
        };

        SceneContext context = renderer.getContext();
        context.changeScene(new Scene() {
            @Override
            public void start(SceneContext context) {
                events.add("parent");
                context.attach(child);
            }

            @Override
            public void update(SceneContext context, float deltaTime) {
            }
        });
        context.update(1f);
        context.update(1f);

        assertEquals(List.of("parent", "child"), events);
    }

    @Test
    void getDebugInformation() {
        SceneContext context = renderer.getContext();

        String expected = """
            Renderer:  <headless>
            Canvas:  800x600 @ 1.0x
            Framerate:  1000 / 10
            Update time:  0ms
            Render time:  0ms""";

        assertEquals(expected, String.join("\n", context.getDebugInformation()));
    }

    private record Counter(List<String> frames) implements Scene {

        public Counter() {
            this(new ArrayList<>());
        }

        @Override
        public void start(SceneContext context) {
            frames.add("start");
        }

        @Override
        public void end(SceneContext context) {
            frames.add("end");
        }

        @Override
        public void update(SceneContext context, float deltaTime) {
            frames.add(String.format("%.2f", deltaTime));
        }

        @Override
        public void resize(SceneContext context, int canvasWidth, int canvasHeight) {
            frames.add("resize");
        }
    }
}
