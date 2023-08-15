//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.SegmentedLine;
import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.Graphic2D;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.StageVisitor;
import nl.colorize.multimedialib.stage.Text;
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
        spriteA.addState("a", new MockImage());

        Sprite spriteB = new Sprite();
        spriteB.addState("b", new MockImage());

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
            public void onGraphicAdded(Container parent, Graphic2D graphic) {
            }

            @Override
            public void onGraphicRemoved(Container parent, Graphic2D graphic) {
            }

            @Override
            public boolean visitGraphic(Graphic2D graphic) {
                return true;
            }

            @Override
            public void drawBackground(ColorRGB color) {
            }

            @Override
            public void drawSprite(Sprite sprite) {
                visited.add("sprite");
            }

            @Override
            public void drawLine(Primitive graphic, Line line) {
                visited.add("line");
            }

            @Override
            public void drawSegmentedLine(Primitive graphic, SegmentedLine line) {
                visited.add("segmentedline");
            }

            @Override
            public void drawRect(Primitive graphic, Rect rect) {
                visited.add("rect");
            }

            @Override
            public void drawCircle(Primitive graphic, Circle circle) {
                visited.add("circle");
            }

            @Override
            public void drawPolygon(Primitive graphic, Polygon polygon) {
                visited.add("polygon");
            }

            @Override
            public void drawText(Text text) {
                visited.add("text");
            }
        });

        assertEquals(ImmutableList.of("sprite", "rect", "sprite", "text"), visited);
    }

    @Test
    void hitTestConsidersShapeSizeAndPosition() {
        Rect rect = new Rect(100, 200, 100, 100);
        Primitive primitive = new Primitive(rect, ColorRGB.RED);

        Stage stage = new Stage(GraphicsMode.MODE_2D, CANVAS);
        stage.getRoot().addChild(primitive);

        assertFalse(primitive.getStageBounds().contains(new Point2D(0, 200)));
        assertTrue(primitive.getStageBounds().contains(new Point2D(100, 200)));
        assertTrue(primitive.getStageBounds().contains(new Point2D(200, 200)));
        assertFalse(primitive.getStageBounds().contains(new Point2D(300, 200)));

        primitive.getTransform().setPosition(100f, 0f);

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
                Container
                    Container
                        Sprite [MockImage]
                        Primitive [(10, 10, 200, 200)]
                        Text [test]
            """;

        assertEquals(expected, stage.toString());
    }

    @Test
    void spriteBoundsShouldConsiderCurrentGraphicsAndTransform() {
        Sprite sprite = new Sprite();
        sprite.addState("a", new MockImage(100, 100));
        sprite.addState("b", new MockImage(200, 200));
        sprite.changeState("a");

        assertEquals("(-50, -50, 100, 100)", sprite.getStageBounds().toString());

        sprite.getTransform().setPosition(10, 20);

        assertEquals("(-40, -30, 100, 100)", sprite.getStageBounds().toString());

        sprite.changeState("b");

        assertEquals("(-90, -80, 200, 200)", sprite.getStageBounds().toString());

        sprite.getTransform().setScale(200f);

        assertEquals("(-190, -180, 400, 400)", sprite.getStageBounds().toString());
    }
}
