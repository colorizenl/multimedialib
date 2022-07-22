//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Primitive;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.graphics.Text;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.multimedialib.renderer.Canvas;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StageTest {

    @Test
    void visitStage() {
        Sprite spriteA = new Sprite();
        spriteA.addState("a", new MockImage());

        Sprite spriteB = new Sprite();
        spriteB.addState("b", new MockImage());

        Stage stage = new Stage(Canvas.flexible(800, 600));
        stage.add(spriteA);
        stage.add(Primitive.of(new Rect(10, 20, 30, 40), ColorRGB.RED));
        stage.add(spriteB);
        stage.add(new Text("abc", new TTFont("test", 10, ColorRGB.BLACK, false)));

        List<String> visited = new ArrayList<>();

        stage.visit(new StageVisitor() {
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

        Stage stage = new Stage(Canvas.flexible(800, 600));
        stage.add(primitive);

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
}
