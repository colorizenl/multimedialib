//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.multimedialib.mock.MockScene;
import nl.colorize.multimedialib.mock.MockStageVisitor;
import nl.colorize.multimedialib.mock.MockStopwatch;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import nl.colorize.multimedialib.scene.effect.Effect;
import nl.colorize.multimedialib.stage.Animation;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.util.EventQueue;
import nl.colorize.util.Subject;
import nl.colorize.util.animation.Interpolation;
import nl.colorize.util.animation.Timeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static nl.colorize.multimedialib.math.Shape.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SceneManagerTest {

    private HeadlessRenderer context;

    @BeforeEach
    public void before() {
        context = new HeadlessRenderer(false);
    }

    @Test
    public void testInitialScene() {
        MockScene sceneA = new MockScene();
        MockScene sceneB = new MockScene();

        SceneManager sceneManager = new SceneManager(context);
        sceneManager.changeScene(sceneA);
        sceneManager.performFrameUpdate(1f);
        sceneManager.performFrameUpdate(1f);

        assertEquals(1, sceneA.getStartCount());
        assertEquals(2, sceneA.getFrameUpdateCount());

        assertEquals(0, sceneB.getStartCount());
        assertEquals(0, sceneB.getFrameUpdateCount());
    }

    @Test
    public void testChangeScene() {
        MockScene sceneA = new MockScene();
        MockScene sceneB = new MockScene();

        SceneManager sceneManager = new SceneManager(context);
        sceneManager.changeScene(sceneA);
        sceneManager.performFrameUpdate(1f);
        sceneManager.changeScene(sceneB);
        sceneManager.performFrameUpdate(1f);
        sceneManager.performFrameUpdate(1f);

        assertEquals(1, sceneA.getStartCount());
        assertEquals(1, sceneA.getFrameUpdateCount());

        assertEquals(1, sceneB.getStartCount());
        assertEquals(2, sceneB.getFrameUpdateCount());
    }

    @Test
    public void testEndScene() {
        MockScene sceneA = new MockScene();
        MockScene sceneB = new MockScene();

        SceneManager sceneManager = new SceneManager(context);
        sceneManager.changeScene(sceneA);
        sceneManager.performFrameUpdate(1f);
        sceneManager.changeScene(sceneB);
        sceneManager.performFrameUpdate(1f);

        assertEquals(1, sceneA.getStartCount());
        assertEquals(1, sceneA.getEndCount());
        assertEquals(1, sceneB.getStartCount());
        assertEquals(0, sceneB.getEndCount());
    }

    @Test
    void systemsShouldBeAttachedToParent() {
        MockScene sceneA = new MockScene();
        List<String> tracker = new ArrayList<>();

        SceneManager sceneManager = new SceneManager(context);
        sceneManager.changeScene(sceneA);
        sceneManager.attach((context, deltaTime) -> tracker.add("a"));
        sceneManager.attach((context, deltaTime) -> tracker.add("b"));
        sceneManager.performFrameUpdate(1f);

        assertEquals(ImmutableList.of("a", "b"), tracker);
    }

    @Test
    void systemsShouldEndWhenSceneIsChanged() {
        MockScene sceneA = new MockScene();
        MockScene sceneB = new MockScene();
        List<String> tracker = new ArrayList<>();

        SceneManager sceneManager = new SceneManager(context);
        sceneManager.changeScene(sceneA);
        sceneManager.attach((context, deltaTime) -> tracker.add("a"));
        sceneManager.attach((context, deltaTime) -> tracker.add("b"));
        sceneManager.performFrameUpdate(1f);
        sceneManager.performFrameUpdate(1f);
        sceneManager.changeScene(sceneB);
        sceneManager.attach((context, deltaTime) -> tracker.add("c"));
        sceneManager.performFrameUpdate(1f);

        assertEquals(List.of("a", "b", "a", "b", "c"), tracker);
    }

    @Test
    void addSystemDuringIteration() {
        SceneManager sceneManager = new SceneManager(context);
        sceneManager.changeScene(new MockScene());

        List<String> buffer = new ArrayList<>();
        sceneManager.attach((ctx, dt) -> {
            buffer.add("a");
            if (buffer.size() == 2) {
                sceneManager.attach((ctx2, dt2) -> buffer.add("b"));
            }
        });
        sceneManager.performFrameUpdate(1f);
        sceneManager.performFrameUpdate(1f);
        sceneManager.performFrameUpdate(1f);
        sceneManager.performFrameUpdate(1f);

        assertEquals(List.of("a", "a", "a", "b", "a", "b"), buffer);
    }

    @Test
    void completedSubSceneIsStopped() {
        MockScene parent = new MockScene();
        MockScene child = new MockScene();

        SceneManager sceneManager = new SceneManager(context);
        sceneManager.changeScene(parent);
        sceneManager.attach(child);
        sceneManager.performFrameUpdate(1f);
        sceneManager.performFrameUpdate(1f);
        child.setCompleted(true);
        sceneManager.performFrameUpdate(1f);

        assertFalse(parent.isCompleted());
        assertTrue(child.isCompleted());
        assertEquals(3, parent.getFrameUpdateCount());
        assertEquals(2, child.getFrameUpdateCount());
    }

    @Test
    void completedParentSceneIsNotStopped() {
        MockScene parent = new MockScene();

        SceneManager sceneManager = new SceneManager(context);
        sceneManager.changeScene(parent);
        sceneManager.performFrameUpdate(1f);
        sceneManager.performFrameUpdate(1f);
        parent.setCompleted(true);
        sceneManager.performFrameUpdate(1f);

        assertTrue(parent.isCompleted());
        assertEquals(3, parent.getFrameUpdateCount());
    }

    @Test
    void startSubScene() {
        MockScene parent = new MockScene();
        MockScene child1 = new MockScene();
        MockScene child2 = new MockScene();

        SceneManager sceneManager = new SceneManager(context);
        sceneManager.changeScene(parent);
        sceneManager.attach(child1);
        sceneManager.performFrameUpdate(1f);
        sceneManager.attach(child2);
        sceneManager.performFrameUpdate(1f);

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

        SceneManager sceneManager = new SceneManager(context);
        sceneManager.changeScene(parent);
        sceneManager.attach(child1);
        sceneManager.performFrameUpdate(1f);
        sceneManager.changeScene(newParent);
        sceneManager.attach(child2);
        sceneManager.performFrameUpdate(1f);
        sceneManager.attach(child1);
        sceneManager.performFrameUpdate(1f);

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

        SceneManager sceneManager = new SceneManager(context);
        sceneManager.changeScene(scene1);
        sceneManager.attach(scene2);
        sceneManager.attachGlobalSubScene(scene3);
        sceneManager.performFrameUpdate(1f);
        sceneManager.changeScene(scene4);
        sceneManager.performFrameUpdate(1f);

        assertEquals(1, scene1.getFrameUpdateCount());
        assertEquals(1, scene2.getFrameUpdateCount());
        assertEquals(2, scene3.getFrameUpdateCount());
        assertEquals(1, scene4.getFrameUpdateCount());
    }

    @Test
    void delayScene() {
        MockScene parent = new MockScene();
        MockScene child = new MockScene();

        SceneManager sceneManager = new SceneManager(context);
        sceneManager.changeScene(parent);
        sceneManager.attach(Effect.delay(2f, () -> child.start(context)));
        sceneManager.performFrameUpdate(1f);
        sceneManager.performFrameUpdate(1f);
        sceneManager.performFrameUpdate(1f);

        assertEquals(1, parent.getStartCount());
        assertEquals(3, parent.getFrameUpdateCount());
        assertEquals(1, child.getStartCount());
        assertEquals(0, child.getFrameUpdateCount());
    }

    @Test
    void nativeFramerate() {
        SceneManager sceneManager = new SceneManager(context, new MockStopwatch(1000, 1100, 1200));
        Counter counter = new Counter();
        sceneManager.changeScene(counter);

        for (int i = 0; i < 3; i++) {
            sceneManager.requestFrameUpdate();
        }

        assertEquals("[start, 0.10, 0.10]", counter.frames.toString());
    }

    @Test
    void applicationFramerateSlowerThanRefreshRate() {
        SceneManager sceneManager = new SceneManager(context,
            new MockStopwatch(1000, 1100, 1150, 1200, 1300));
        Counter counter = new Counter();
        sceneManager.changeScene(counter);

        for (int i = 0; i < 5; i++) {
            sceneManager.requestFrameUpdate();
        }

        assertEquals("[start, 0.10, 0.10, 0.10]", counter.frames.toString());
    }

    @Test
    void slowerFramerateWithNonExactMatch() {
        SceneManager sceneManager = new SceneManager(context,
            new MockStopwatch(1000, 1100, 1130, 1160, 1190, 1220, 1250, 1300));
        Counter counter = new Counter();
        sceneManager.changeScene(counter);

        for (int i = 0; i < 8; i++) {
            sceneManager.requestFrameUpdate();
        }

        assertEquals("[start, 0.10, 0.12]", counter.frames.toString());
    }

    @Test
    void applicationFramerateFasterThanRefreshRate() {
        SceneManager sceneManager = new SceneManager(context, new MockStopwatch(1000, 1200, 1400));
        Counter counter = new Counter();
        sceneManager.changeScene(counter);

        for (int i = 0; i < 3; i++) {
            sceneManager.requestFrameUpdate();
        }

        assertEquals("[start, 0.20, 0.20]", counter.frames.toString());
    }

    @Test
    void limitExtremelyLargeDeltaTime() {
        SceneManager sceneManager = new SceneManager(context, new MockStopwatch(1000, 1100, 9999));
        Counter counter = new Counter();
        sceneManager.changeScene(counter);

        for (int i = 0; i < 3; i++) {
            sceneManager.requestFrameUpdate();
        }

        assertEquals("[start, 0.10, 0.20]", counter.frames.toString());
    }

    @Test
    void allowFramesThatAreLittleBitTooShort() {
        SceneManager sceneManager = new SceneManager(context,
            new MockStopwatch(1000, 1100, 1195, 1300));
        Counter counter = new Counter();
        sceneManager.changeScene(counter);

        for (int i = 0; i < 4; i++) {
            sceneManager.requestFrameUpdate();
        }

        assertEquals("[start, 0.10, 0.09, 0.10]", counter.frames.toString());
    }

    @Test
    void immediatelySwitchSceneFromStartScene() {
        Counter a = new Counter();
        Counter b = new Counter();

        SceneManager sceneManager = new SceneManager(context,
            new MockStopwatch(1000, 1100, 1200, 1300, 1400));
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
        sceneManager.requestFrameUpdate();
        sceneManager.requestFrameUpdate();

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

        SceneManager sceneManager = new SceneManager(context);
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
        sceneManager.performFrameUpdate(1f);
        sceneManager.performFrameUpdate(1f);

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

        SceneManager sceneManager = new SceneManager(context);
        sceneManager.changeScene(new MockScene());
        context.getStage().getRoot().addChild(sprite);

        MockStageVisitor visitor = new MockStageVisitor();

        sceneManager.performFrameUpdate(1f);
        context.getStage().visit(visitor);
        assertEquals(List.of("background", "sprite"), visitor.getRendered());
        assertEquals(1f, sprite.getCurrentStateTimer().getTime(), EPSILON);

        sprite.getTransform().setVisible(false);
        sceneManager.performFrameUpdate(2f);
        context.getStage().visit(visitor);
        assertEquals(List.of("background"), visitor.getRendered());
        assertEquals(1f, sprite.getCurrentStateTimer().getTime(), EPSILON);

        sprite.getTransform().setVisible(true);
        sceneManager.performFrameUpdate(3f);
        context.getStage().visit(visitor);
        assertEquals(List.of("background", "sprite"), visitor.getRendered());
        assertEquals(6f, sprite.getCurrentStateTimer().getTime(), EPSILON);
    }

    @Test
    void attachTimerHandler() {
        List<String> events = new ArrayList<>();
        context.attachTimer(3f, () -> events.add("done"));

        context.doFrame(1f);
        assertEquals(List.of(), events);

        context.doFrame(1f);
        assertEquals(List.of(), events);

        context.doFrame(1f);
        assertEquals(List.of("done"), events);

        context.doFrame(1f);
        assertEquals(List.of("done"), events);
    }

    @Test
    void attachTimelineHandler() {
        Timeline timeline = new Timeline(Interpolation.LINEAR);
        timeline.addKeyFrame(0f, 1f);
        timeline.addKeyFrame(1f, 3f);
        timeline.addKeyFrame(3f, 4f);

        List<String> events = new ArrayList<>();
        context.attachTimeline(timeline, value -> events.add(String.format("%.1f", value)));

        context.doFrame(1f);
        assertEquals(List.of("3.0"), events);

        context.doFrame(1f);
        assertEquals(List.of("3.0", "3.5"), events);

        context.doFrame(1f);
        assertEquals(List.of("3.0", "3.5", "4.0"), events);

        context.doFrame(1f);
        assertEquals(List.of("3.0", "3.5", "4.0"), events);
    }

    @Test
    void attachLoopingTimelineHandler() {
        Timeline timeline = new Timeline(Interpolation.LINEAR, true);
        timeline.addKeyFrame(0f, 1f);
        timeline.addKeyFrame(1f, 3f);
        timeline.addKeyFrame(3f, 4f);

        List<String> events = new ArrayList<>();
        context.attachTimeline(timeline, value -> events.add(String.format("%.1f", value)));

        context.doFrame(1f);
        assertEquals(List.of("3.0"), events);

        context.doFrame(1f);
        assertEquals(List.of("3.0", "3.5"), events);

        context.doFrame(1f);
        assertEquals(List.of("3.0", "3.5", "4.0"), events);

        context.doFrame(1f);
        assertEquals(List.of("3.0", "3.5", "4.0", "3.0"), events);
    }

    @Test
    void attachClickHandler() {
        Primitive rect = Primitive.fromRect(100, 100, ColorRGB.RED);
        context.getStage().getRoot().addChild(rect);

        List<String> events = new ArrayList<>();
        context.attachClickHandler(rect, () -> events.add("click"));
        context.setPointer(new Point2D(40, 40));
        context.setPointerReleased(true);
        context.doFrame(1f);

        assertEquals(List.of("click"), events);
    }

    @Test
    void handleEventsDuringFrameUpdates() throws InterruptedException {
        List<String> events = new ArrayList<>();
        List<Throwable> errors = new ArrayList<>();

        Subject<String> subject = Subject.runAsync(() -> {
            Thread.sleep(100);
            return "1";
        });

        EventQueue<String> eventQueue = new EventQueue<>();
        subject.subscribe(eventQueue);

        HeadlessRenderer renderer = new HeadlessRenderer(false);
        renderer.attach(eventQueue, events::add, errors::add);

        Thread.sleep(3000);

        assertEquals(0, events.size());
        renderer.doFrame();
        assertEquals(1, events.size());
        assertEquals(0, errors.size());
    }

    @Test
    void handleErrors() throws InterruptedException {
        List<String> events = new ArrayList<>();
        List<Throwable> errors = new ArrayList<>();

        Subject<String> subject = Subject.runAsync(() -> {
            Thread.sleep(100);
            throw new RuntimeException();
        });

        EventQueue<String> eventQueue = new EventQueue<>();
        subject.subscribe(eventQueue);

        HeadlessRenderer renderer = new HeadlessRenderer(false);
        renderer.attach(eventQueue, events::add, errors::add);

        Thread.sleep(3000);

        assertEquals(0, events.size());
        assertEquals(0, errors.size());
        renderer.doFrame();
        assertEquals(0, events.size());
        assertEquals(1, errors.size());
    }

    @Test
    void eventQueueToFrameUpdateSubject() {
        List<String> events = new ArrayList<>();
        List<Throwable> errors = new ArrayList<>();
        Subject<String> originalSubject = Subject.of("a");
        EventQueue<String> eventQueue = EventQueue.subscribe(originalSubject);

        HeadlessRenderer renderer = new HeadlessRenderer(false);
        renderer.attach(eventQueue).subscribe(events::add, errors::add);

        assertEquals(0, events.size());
        assertEquals(0, errors.size());
        renderer.doFrame();
        assertEquals(1, events.size());
        assertEquals(0, errors.size());
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
