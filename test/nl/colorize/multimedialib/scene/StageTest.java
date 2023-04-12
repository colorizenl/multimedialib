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
import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.headless.HeadlessFont;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Graphic2D;
import nl.colorize.multimedialib.stage.Layer2D;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.StageObserver;
import nl.colorize.multimedialib.stage.StageVisitor;
import nl.colorize.multimedialib.stage.Text;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StageTest {

    private static final Canvas CANVAS = Canvas.forNative(800, 600);

    @Test
    void visitStage() {
        Sprite spriteA = new Sprite();
        spriteA.addState("a", new MockImage());

        Sprite spriteB = new Sprite();
        spriteB.addState("b", new MockImage());

        Stage stage = new Stage(GraphicsMode.MODE_2D, CANVAS, new FrameStats());
        stage.getDefaultLayer().add(spriteA);
        stage.getDefaultLayer().add(Primitive.of(new Rect(10, 20, 30, 40), ColorRGB.RED));
        stage.getDefaultLayer().add(spriteB);
        stage.getDefaultLayer().add(new Text("abc", new HeadlessFont()));

        List<String> visited = new ArrayList<>();

        stage.visit(new StageVisitor() {
            @Override
            public void prepareLayer(Layer2D layer) {
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
        Primitive primitive = Primitive.of(rect, ColorRGB.RED);

        Stage stage = new Stage(GraphicsMode.MODE_2D, CANVAS, new FrameStats());
        stage.getDefaultLayer().add(primitive);

        assertFalse(primitive.hitTest(new Point2D(0, 200)));
        assertTrue(primitive.hitTest(new Point2D(100, 200)));
        assertTrue(primitive.hitTest(new Point2D(200, 200)));
        assertFalse(primitive.hitTest(new Point2D(300, 200)));

        primitive.setPosition(100f, 0f);

        assertFalse(primitive.hitTest(new Point2D(0, 200)));
        assertFalse(primitive.hitTest(new Point2D(100, 200)));
        assertTrue(primitive.hitTest(new Point2D(200, 200)));
        assertTrue(primitive.hitTest(new Point2D(300, 200)));
        assertFalse(primitive.hitTest(new Point2D(400, 200)));
    }

    @Test
    void observerAlsoListensForLayerChanges() {
        Stage stage = new Stage(GraphicsMode.MODE_2D, CANVAS, new FrameStats());
        Layer2D layer = stage.addLayer("test");

        List<Graphic2D> added1 = new ArrayList<>();
        stage.getObservers().add(createObserver(added1));

        layer.add(Primitive.of(new Rect(10, 10, 100, 100), ColorRGB.RED));

        List<Graphic2D> added2 = new ArrayList<>();
        stage.getObservers().add(createObserver(added2));

        layer.add(Primitive.of(new Rect(10, 10, 100, 100), ColorRGB.GREEN));

        assertEquals(2, added1.size());
        assertEquals(1, added2.size());
    }

    @Test
    void stringForm() {
        Stage stage = new Stage(GraphicsMode.MODE_2D, CANVAS, new FrameStats());
        Layer2D layer = stage.addLayer("test");
        layer.add(new Sprite(new MockImage()));
        layer.add(Primitive.of(new Rect(10, 10, 200, 200), ColorRGB.RED));
        layer.add(new Text("test", null));

        String expected = """
            Stage
                2D graphics layer [test]
                    Text [test]
                    Primitive [10, 10, 200, 200]
                    Sprite [MockImage @ (0, 0)]
                2D graphics layer [$$default]
            """;

        assertEquals(expected, stage.toString());
    }

    private StageObserver createObserver(List<Graphic2D> added) {
        return new StageObserver() {
            @Override
            public void onLayerAdded(Layer2D layer) {
            }

            @Override
            public void onGraphicAdded(Layer2D layer, Graphic2D graphic) {
                added.add(graphic);
            }

            @Override
            public void onGraphicRemoved(Layer2D layer, Graphic2D graphic) {
            }

            @Override
            public void onStageCleared() {
            }
        };
    }
}
