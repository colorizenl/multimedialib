//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.SegmentedLine;
import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.multimedialib.mock.MockMesh;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.multimedialib.renderer.headless.CollectingStageVisitor;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.Group;
import nl.colorize.multimedialib.stage.Light;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.StageNode2D;
import nl.colorize.multimedialib.stage.StageNode3D;
import nl.colorize.multimedialib.stage.StageSubscriber;
import nl.colorize.multimedialib.stage.StageVisitor;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.stage.Transform;
import nl.colorize.multimedialib.stage.Transform3D;
import nl.colorize.util.stats.TupleList;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StageTest {

    private static final Canvas CANVAS = new Canvas(800, 600, ScaleStrategy.flexible());

    @Test
    void visitStage() {
        Sprite spriteA = new Sprite();
        spriteA.addGraphics("a", new MockImage());

        Sprite spriteB = new Sprite();
        spriteB.addGraphics("b", new MockImage());

        Stage stage = new Stage(GraphicsMode.MODE_2D, CANVAS);
        stage.getRoot().addChild(spriteA);
        stage.getRoot().addChild(new Primitive(new Rect(10, 20, 30, 40), ColorRGB.RED));
        stage.getRoot().addChild(spriteB);
        stage.getRoot().addChild(new Text("abc", null));

        List<String> visited = new ArrayList<>();

        stage.visit(new StageVisitor() {
            @Override
            public void prepareStage(Stage stage) {
            }

            @Override
            public boolean shouldVisitAllNodes() {
                return true;
            }

            @Override
            public void visitContainer(Container container, Transform globalTransform) {
            }

            @Override
            public void drawBackground(ColorRGB color) {
            }

            @Override
            public void drawSprite(Sprite sprite, Transform globalTransform) {
                visited.add("sprite");
            }

            @Override
            public void drawLine(Primitive graphic, Line line, Transform globalTransform) {
                visited.add("line");
            }

            @Override
            public void drawSegmentedLine(Primitive graphic, SegmentedLine line, Transform tr) {
                visited.add("segmentedline");
            }

            @Override
            public void drawRect(Primitive graphic, Rect rect, Transform globalTransform) {
                visited.add("rect");
            }

            @Override
            public void drawCircle(Primitive graphic, Circle circle, Transform globalTransform) {
                visited.add("circle");
            }

            @Override
            public void drawPolygon(Primitive graphic, Polygon polygon, Transform globalTransform) {
                visited.add("polygon");
            }

            @Override
            public void drawText(Text text, Transform globalTransform) {
                visited.add("text");
            }

            @Override
            public void visitGroup(Group group, Transform3D globalTransform) {
            }

            @Override
            public void drawMesh(Mesh mesh, Transform3D globalTransform) {
            }

            @Override
            public void drawLight(Light light, Transform3D globalTransform) {
            }
        });

        assertEquals(List.of("sprite", "rect", "sprite", "text"), visited);
    }

    @Test
    void hitTestConsidersShapeSizeAndPosition() {
        Rect rect = new Rect(100, 200, 100, 100);
        Primitive primitive = new Primitive(rect, ColorRGB.RED);

        Stage stage = new Stage(GraphicsMode.MODE_2D, CANVAS);
        stage.getRoot().addChild(primitive);
        stage.recalculateGlobalTransform(primitive);

        assertFalse(primitive.getStageBounds().contains(new Point2D(0, 200)));
        assertTrue(primitive.getStageBounds().contains(new Point2D(100, 200)));
        assertTrue(primitive.getStageBounds().contains(new Point2D(200, 200)));
        assertFalse(primitive.getStageBounds().contains(new Point2D(300, 200)));

        primitive.getTransform().setPosition(100f, 0f);
        stage.recalculateGlobalTransform(primitive);

        assertFalse(primitive.getStageBounds().contains(new Point2D(0, 200)));
        assertFalse(primitive.getStageBounds().contains(new Point2D(100, 200)));
        assertTrue(primitive.getStageBounds().contains(new Point2D(200, 200)));
        assertTrue(primitive.getStageBounds().contains(new Point2D(300, 200)));
        assertFalse(primitive.getStageBounds().contains(new Point2D(400, 200)));
    }

    @Test
    void stringForm() {
        Stage stage = new Stage(GraphicsMode.MODE_2D, CANVAS);
        Container layer = stage.addContainer();
        layer.addChild(new Sprite(new MockImage()));
        layer.addChild(new Primitive(new Rect(10, 10, 200, 200), ColorRGB.RED));
        layer.addChild(new Text("test", null));

        String expected = """
            Stage
                $$root [1]
                    Container [3]
                        Sprite [$$default]
                        Rect [(10, 10, 200, 200)]
                        Text [test]
            """;

        assertEquals(expected, stage.toString());
    }

    @Test
    void spriteBoundsShouldConsiderCurrentGraphicsAndTransform() {
        Sprite sprite = new Sprite();
        sprite.addGraphics("a", new MockImage(100, 100));
        sprite.addGraphics("b", new MockImage(200, 200));
        sprite.changeGraphics("a");

        Stage stage = new Stage(GraphicsMode.MODE_2D, CANVAS);
        stage.getRoot().addChild(sprite);
        stage.recalculateGlobalTransform(sprite);

        assertEquals("(-50, -50, 100, 100)", sprite.getStageBounds().toString());

        sprite.getTransform().setPosition(10, 20);
        stage.recalculateGlobalTransform(sprite);
        assertEquals("(-40, -30, 100, 100)", sprite.getStageBounds().toString());

        sprite.changeGraphics("b");
        stage.recalculateGlobalTransform(sprite);
        assertEquals("(-90, -80, 200, 200)", sprite.getStageBounds().toString());

        sprite.getTransform().setScale(200f);
        stage.recalculateGlobalTransform(sprite);
        assertEquals("(-190, -180, 400, 400)", sprite.getStageBounds().toString());
    }

    @Test
    void detachNode2D() {
        Container a = new Container("a");
        Container b = new Container("b");
        Container c = new Container("c");
        Primitive d = new Primitive(new Rect(1, 1, 100, 100), ColorRGB.RED);
        Primitive e = new Primitive(new Rect(2, 2, 200, 200), ColorRGB.BLUE);

        Stage stage = new Stage(GraphicsMode.MODE_2D, CANVAS);
        stage.getRoot().addChild(a);
        stage.getRoot().addChild(b);
        a.addChild(d);
        b.addChild(c);
        c.addChild(e);

        CollectingStageVisitor visitor = new CollectingStageVisitor();
        stage.visit(visitor);
        assertEquals(List.of(stage.getRoot(), a, d, b, c, e), visitor.getNodes2D());

        stage.detach(d);
        stage.visit(visitor);
        assertEquals(List.of(stage.getRoot(), a, b, c, e), visitor.getNodes2D());

        stage.detach(c);
        stage.visit(visitor);
        assertEquals(List.of(stage.getRoot(), a, b), visitor.getNodes2D());
    }

    @Test
    void detachNode3D() {
        Group a = new Group("a");
        Group b = new Group("b");
        Mesh c = new MockMesh();
        Mesh d = new MockMesh();

        Stage stage = new Stage(GraphicsMode.MODE_3D, CANVAS);
        stage.getRoot3D().addChild(a);
        stage.getRoot3D().addChild(b);
        a.addChild(c);
        b.addChild(d);

        CollectingStageVisitor visitor = new CollectingStageVisitor();
        stage.visit(visitor);
        assertEquals(List.of(stage.getRoot3D(), a, c, b, d), visitor.getNodes3D());

        stage.detach(c);
        stage.visit(visitor);
        assertEquals(List.of(stage.getRoot3D(), a, b, d), visitor.getNodes3D());

        stage.detach(b);
        stage.visit(visitor);
        assertEquals(List.of(stage.getRoot3D(), a), visitor.getNodes3D());
    }

    @Test
    void findStagePath2D() {
        Sprite child = new Sprite(new MockImage(100, 100));
        Container parent = new Container();
        Stage stage = new Stage(GraphicsMode.MODE_2D, CANVAS);
        stage.getRoot().addChild(parent);
        parent.addChild(child);

        assertEquals(List.of(stage.getRoot(), parent), stage.findNodePath(parent));
        assertEquals(List.of(stage.getRoot(), parent, child), stage.findNodePath(child));
    }

    @Test
    void findStagePath3D() {
        MockMesh child = new MockMesh();
        Group parent = new Group();
        Stage stage = new Stage(GraphicsMode.MODE_3D, CANVAS);
        stage.getRoot3D().addChild(parent);
        parent.addChild(child);

        assertEquals(List.of(stage.getRoot3D(), parent), stage.findNodePath(parent));
        assertEquals(List.of(stage.getRoot3D(), parent, child), stage.findNodePath(child));
    }

    @Test
    void recalculateGlobalTransform2D() {
        Sprite child = new Sprite(new MockImage(100, 100));
        child.getTransform().setPosition(30, 40);

        Container parent = new Container();
        parent.getTransform().setPosition(10, 20);
        parent.addChild(child);

        Stage stage = new Stage(GraphicsMode.MODE_2D, CANVAS);
        stage.getRoot().addChild(parent);
        stage.recalculateGlobalTransform(child);

        assertEquals(new Point2D(10, 20), parent.getGlobalTransform().getPosition());
        assertEquals(new Point2D(40, 60), child.getGlobalTransform().getPosition());
    }

    @Test
    void recalculateGlobalTransform3D() {
        MockMesh child = new MockMesh();
        child.getTransform().setPosition(40, 50, 60);

        Group parent = new Group();
        parent.getTransform().setPosition(10, 20, 30);
        parent.addChild(child);

        Stage stage = new Stage(GraphicsMode.MODE_3D, CANVAS);
        stage.getRoot3D().addChild(parent);
        stage.recalculateGlobalTransform(child);

        assertEquals(new Point3D(10, 20, 30), parent.getGlobalTransform().getPosition());
        assertEquals(new Point3D(50, 70, 90), child.getGlobalTransform().getPosition());
    }

    @Test
    void subscribe2D() {
        TupleList<Container, StageNode2D> added = new TupleList<>();

        Stage stage = new Stage(GraphicsMode.MODE_2D, CANVAS);
        stage.subscribe(new StageSubscriber() {
            @Override
            public void onNodeAdded(Container parent, StageNode2D node) {
                added.add(parent, node);
            }
        });

        Container sub = new Container();
        stage.getRoot().addChild(new Primitive(new Circle(0, 0, 10), ColorRGB.RED));
        stage.getRoot().addChild(sub);
        sub.addChild(new Primitive(new Circle(0, 0, 10), ColorRGB.BLUE));

        assertEquals(3, added.size());
    }

    @Test
    void subscribe3D() {
        TupleList<Group, StageNode3D> added = new TupleList<>();

        Stage stage = new Stage(GraphicsMode.MODE_3D, CANVAS);
        stage.subscribe(new StageSubscriber() {
            @Override
            public void onNodeAdded(Group parent, StageNode3D node) {
                added.add(parent, node);
            }
        });

        Group sub = new Group();
        stage.getRoot3D().addChild(new MockMesh());
        stage.getRoot3D().addChild(sub);
        sub.addChild(new MockMesh());

        assertEquals(3, added.size());
    }
}
