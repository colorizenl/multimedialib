//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.multimedialib.mock.MockScene;
import nl.colorize.multimedialib.mock.MockStageVisitor;
import nl.colorize.multimedialib.mock.MockStopwatch;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import nl.colorize.multimedialib.scene.effect.Effect;
import nl.colorize.multimedialib.stage.Animation;
import nl.colorize.multimedialib.stage.Sprite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static nl.colorize.multimedialib.math.Shape.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SceneManagerTest {

    private Canvas canvas;
    private SceneContext context;

    @BeforeEach
    public void before() {
        canvas = new Canvas(800, 600, ScaleStrategy.scale());
        context = new HeadlessRenderer(false);
    }

    @Test
    public void testInitialScene() {
        MockScene sceneA = new MockScene();
        MockScene sceneB = new MockScene();

        SceneManager sceneManager = new SceneManager();
        sceneManager.changeScene(sceneA);
        sceneManager.performFrameUpdate(context, 1f);
        sceneManager.performFrameUpdate(context, 1f);

        assertEquals(1, sceneA.getStartCount());
        assertEquals(2, sceneA.getFrameUpdateCount());

        assertEquals(0, sceneB.getStartCount());
        assertEquals(0, sceneB.getFrameUpdateCount());
    }

    @Test
    public void testChangeScene() {
        MockScene sceneA = new MockScene();
        MockScene sceneB = new MockScene();

        SceneManager sceneManager = new SceneManager();
        sceneManager.changeScene(sceneA);
        sceneManager.performFrameUpdate(context, 1f);
        sceneManager.changeScene(sceneB);
        sceneManager.performFrameUpdate(context, 1f);
        sceneManager.performFrameUpdate(context, 1f);

        assertEquals(1, sceneA.getStartCount());
        assertEquals(1, sceneA.getFrameUpdateCount());

        assertEquals(1, sceneB.getStartCount());
        assertEquals(2, sceneB.getFrameUpdateCount());
    }

    @Test
    public void testEndScene() {
        MockScene sceneA = new MockScene();
        MockScene sceneB = new MockScene();

        SceneManager sceneManager = new SceneManager();
        sceneManager.changeScene(sceneA);
        sceneManager.performFrameUpdate(context, 1f);
        sceneManager.changeScene(sceneB);
        sceneManager.performFrameUpdate(context, 1f);

        assertEquals(1, sceneA.getStartCount());
        assertEquals(1, sceneA.getEndCount());
        assertEquals(1, sceneB.getStartCount());
        assertEquals(0, sceneB.getEndCount());
    }

    @Test
    void systemsShouldBeAttachedToParent() {
        MockScene sceneA = new MockScene();
        List<String> tracker = new ArrayList<>();

        SceneManager sceneManager = new SceneManager();
        sceneManager.changeScene(sceneA);
        sceneManager.attach(context, (context, deltaTime) -> tracker.add("a"));
        sceneManager.attach(context, (context, deltaTime) -> tracker.add("b"));
        sceneManager.performFrameUpdate(context, 1f);

        assertEquals(ImmutableList.of("a", "b"), tracker);
    }

    @Test
    void systemsShouldEndWhenSceneIsChanged() {
        MockScene sceneA = new MockScene();
        MockScene sceneB = new MockScene();
        List<String> tracker = new ArrayList<>();

        SceneManager sceneManager = new SceneManager();
        sceneManager.changeScene(sceneA);
        sceneManager.attach(context, (context, deltaTime) -> tracker.add("a"));
        sceneManager.attach(context, (context, deltaTime) -> tracker.add("b"));
        sceneManager.performFrameUpdate(context, 1f);
        sceneManager.performFrameUpdate(context, 1f);
        sceneManager.changeScene(sceneB);
        sceneManager.attach(context, (context, deltaTime) -> tracker.add("c"));
        sceneManager.performFrameUpdate(context, 1f);

        assertEquals(List.of("a", "b", "a", "b", "c"), tracker);
    }

    @Test
    void addSystemDuringIteration() {
        SceneManager sceneManager = new SceneManager();
        sceneManager.changeScene(new MockScene());

        List<String> buffer = new ArrayList<>();
        sceneManager.attach(context, (ctx, dt) -> {
            buffer.add("a");
            if (buffer.size() == 2) {
                sceneManager.attach(ctx, (ctx2, dt2) -> buffer.add("b"));
            }
        });
        sceneManager.performFrameUpdate(context, 1f);
        sceneManager.performFrameUpdate(context, 1f);
        sceneManager.performFrameUpdate(context, 1f);
        sceneManager.performFrameUpdate(context, 1f);

        assertEquals(List.of("a", "a", "a", "b", "a", "b"), buffer);
    }

    @Test
    void completedSubSceneIsStopped() {
        MockScene parent = new MockScene();
        MockScene child = new MockScene();

        SceneManager sceneManager = new SceneManager();
        sceneManager.changeScene(parent);
        sceneManager.attach(context, child);
        sceneManager.performFrameUpdate(context, 1f);
        sceneManager.performFrameUpdate(context, 1f);
        child.setCompleted(true);
        sceneManager.performFrameUpdate(context, 1f);

        assertFalse(parent.isCompleted());
        assertTrue(child.isCompleted());
        assertEquals(3, parent.getFrameUpdateCount());
        assertEquals(2, child.getFrameUpdateCount());
    }

    @Test
    void completedParentSceneIsNotStopped() {
        MockScene parent = new MockScene();

        SceneManager sceneManager = new SceneManager();
        sceneManager.changeScene(parent);
        sceneManager.performFrameUpdate(context, 1f);
        sceneManager.performFrameUpdate(context, 1f);
        parent.setCompleted(true);
        sceneManager.performFrameUpdate(context, 1f);

        assertTrue(parent.isCompleted());
        assertEquals(3, parent.getFrameUpdateCount());
    }

    @Test
    void startSubScene() {
        MockScene parent = new MockScene();
        MockScene child1 = new MockScene();
        MockScene child2 = new MockScene();

        SceneManager sceneManager = new SceneManager();
        sceneManager.changeScene(parent);
        sceneManager.attach(context, child1);
        sceneManager.performFrameUpdate(context, 1f);
        sceneManager.attach(context, child2);
        sceneManager.performFrameUpdate(context, 1f);

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

        SceneManager sceneManager = new SceneManager();
        sceneManager.changeScene(parent);
        sceneManager.attach(context, child1);
        sceneManager.performFrameUpdate(context, 1f);
        sceneManager.changeScene(newParent);
        sceneManager.attach(context, child2);
        sceneManager.performFrameUpdate(context, 1f);
        sceneManager.attach(context, child1);
        sceneManager.performFrameUpdate(context, 1f);

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

        SceneManager sceneManager = new SceneManager();
        sceneManager.changeScene(scene1);
        sceneManager.attach(context, scene2);
        sceneManager.attachGlobal(context, scene3);
        sceneManager.performFrameUpdate(context, 1f);
        sceneManager.changeScene(scene4);
        sceneManager.performFrameUpdate(context, 1f);

        assertEquals(1, scene1.getFrameUpdateCount());
        assertEquals(1, scene2.getFrameUpdateCount());
        assertEquals(2, scene3.getFrameUpdateCount());
        assertEquals(1, scene4.getFrameUpdateCount());
    }

    @Test
    void delayScene() {
        MockScene parent = new MockScene();
        MockScene child = new MockScene();

        SceneManager sceneManager = new SceneManager();
        sceneManager.changeScene(parent);
        sceneManager.attach(context, Effect.delay(2f, () -> child.start(context)));
        sceneManager.performFrameUpdate(context, 1f);
        sceneManager.performFrameUpdate(context, 1f);
        sceneManager.performFrameUpdate(context, 1f);

        assertEquals(1, parent.getStartCount());
        assertEquals(3, parent.getFrameUpdateCount());
        assertEquals(1, child.getStartCount());
        assertEquals(0, child.getFrameUpdateCount());
    }

    @Test
    void nativeFramerate() {
        SceneManager sceneManager = new SceneManager(new MockStopwatch(1000, 1100, 1200));
        Counter counter = new Counter();
        sceneManager.changeScene(counter);

        for (int i = 0; i < 3; i++) {
            sceneManager.requestFrameUpdate(context);
        }

        assertEquals("[start, 0.10, 0.10]", counter.frames.toString());
    }

    @Test
    void applicationFramerateSlowerThanRefreshRate() {
        SceneManager sceneManager = new SceneManager(new MockStopwatch(1000, 1100, 1150, 1200, 1300));
        Counter counter = new Counter();
        sceneManager.changeScene(counter);

        for (int i = 0; i < 5; i++) {
            sceneManager.requestFrameUpdate(context);
        }

        assertEquals("[start, 0.10, 0.10, 0.10]", counter.frames.toString());
    }

    @Test
    void slowerFramerateWithNonExactMatch() {
        SceneManager sceneManager = new SceneManager(
            new MockStopwatch(1000, 1100, 1130, 1160, 1190, 1220, 1250, 1300));
        Counter counter = new Counter();
        sceneManager.changeScene(counter);

        for (int i = 0; i < 8; i++) {
            sceneManager.requestFrameUpdate(context);
        }

        assertEquals("[start, 0.10, 0.12]", counter.frames.toString());
    }

    @Test
    void applicationFramerateFasterThanRefreshRate() {
        SceneManager sceneManager = new SceneManager(new MockStopwatch(1000, 1200, 1400));
        Counter counter = new Counter();
        sceneManager.changeScene(counter);

        for (int i = 0; i < 3; i++) {
            sceneManager.requestFrameUpdate(context);
        }

        assertEquals("[start, 0.20, 0.20]", counter.frames.toString());
    }

    @Test
    void limitExtremelyLargeDeltaTime() {
        SceneManager sceneManager = new SceneManager(new MockStopwatch(1000, 1100, 9999));
        Counter counter = new Counter();
        sceneManager.changeScene(counter);

        for (int i = 0; i < 3; i++) {
            sceneManager.requestFrameUpdate(context);
        }

        assertEquals("[start, 0.10, 0.20]", counter.frames.toString());
    }

    @Test
    void allowFramesThatAreLittleBitTooShort() {
        SceneManager sceneManager = new SceneManager(new MockStopwatch(1000, 1100, 1195, 1300));
        Counter counter = new Counter();
        sceneManager.changeScene(counter);

        for (int i = 0; i < 4; i++) {
            sceneManager.requestFrameUpdate(context);
        }

        assertEquals("[start, 0.10, 0.09, 0.10]", counter.frames.toString());
    }

    @Test
    void immediatelySwitchSceneFromStartScene() {
        Counter a = new Counter();
        Counter b = new Counter();

        SceneManager sceneManager = new SceneManager(new MockStopwatch(1000, 1100, 1200, 1300, 1400));
        sceneManager.changeScene(new Scene() {
            @Override
            public void start(SceneContext context) {
                a.start(context);
                sceneManager.changeScene(b);
            }

            @Override
            public void end(SceneContext context) {
                a.end(context);
            }

            @Override
            public void update(SceneContext context, float deltaTime) {
                a.update(context, deltaTime);
            }
        });
        sceneManager.requestFrameUpdate(context);
        sceneManager.requestFrameUpdate(context);

        assertEquals(List.of("start", "end"), a.frames);
        assertEquals(List.of("start", "0.10", "0.10"), b.frames);
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

        SceneManager sceneManager = new SceneManager();
        sceneManager.changeScene(new Scene() {
            @Override
            public void start(SceneContext context) {
                events.add("parent");
                context.attach(child);
            }

            @Override
            public void update(SceneContext context, float deltaTime) {
            }
        });
        sceneManager.performFrameUpdate(context, 1f);
        sceneManager.performFrameUpdate(context, 1f);

        assertEquals(List.of("parent", "child"), events);
    }

    @Test
    void getDebugInformation() {
        String expected = """
            Renderer:  Headless renderer
            Canvas:  800x600 @ 1.0x
            Framerate:  1000 / 10
            Update time:  0ms
            Render time:  0ms""";

        assertEquals(expected, String.join("\n", context.getDebugInformation()));
    }

    @Test
    void updateBasedOnGlobalSceneTimer() {
        MockImage a = new MockImage();
        MockImage b = new MockImage();
        Sprite sprite = new Sprite(new Animation(List.of(a, b), 10f, false));
        sprite.animate(Timer.none());

        SceneManager sceneManager = new SceneManager();
        sceneManager.changeScene(new MockScene());
        context.getStage().getRoot().addChild(sprite);

        MockStageVisitor visitor = new MockStageVisitor();

        sceneManager.performFrameUpdate(context, 1f);
        context.getStage().visit(visitor);
        assertEquals(List.of("background", "sprite"), visitor.getRendered());
        assertEquals(1f, sprite.getCurrentStateTimer().getTime(), EPSILON);

        sprite.getTransform().setVisible(false);
        sceneManager.performFrameUpdate(context, 2f);
        context.getStage().visit(visitor);
        assertEquals(List.of("background"), visitor.getRendered());
        assertEquals(1f, sprite.getCurrentStateTimer().getTime(), EPSILON);

        sprite.getTransform().setVisible(true);
        sceneManager.performFrameUpdate(context, 3f);
        context.getStage().visit(visitor);
        assertEquals(List.of("background", "sprite"), visitor.getRendered());
        assertEquals(6f, sprite.getCurrentStateTimer().getTime(), EPSILON);
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
    }
}
