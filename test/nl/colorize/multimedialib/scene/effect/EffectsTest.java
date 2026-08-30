//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import nl.colorize.multimedialib.scene.Actor;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Text;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static nl.colorize.multimedialib.math.Point2D.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EffectsTest {

    private HeadlessRenderer context;

    @BeforeEach
    public void before() {
        context = new HeadlessRenderer();
    }

    @Test
    void scaleToFit() {
        Sprite sprite = new Sprite(new MockImage(200, 100));
        context.getStage().getRoot().addChild(sprite);
        context.attach(Effects.scaleToFit(sprite, context.getCanvas()));
        context.doFrame(1f);

        assertEquals(600f, sprite.getTransform().getScaleX(), EPSILON);
        assertEquals(600f, sprite.getTransform().getScaleY(), EPSILON);
    }

    @Test
    void appearText() {
        Text text = new Text("This is a test", null);
        Actor effect = Effects.appearText(text, 2f);

        effect.update(0f);
        assertEquals(List.of(""), text.getLines());

        effect.update(1f);
        assertEquals(List.of("This is"), text.getLines());

        effect.update(2f);
        assertEquals(List.of("This is a test"), text.getLines());

        effect.update(3f);
        assertEquals(List.of("This is a test"), text.getLines());
    }

    @Test
    void doNotShowOrientationLockInLandscapeMode() {
        HeadlessRenderer context = new HeadlessRenderer();

        Sprite sprite = new MockImage("orientation lock").toSprite();
        Actor orientationLockScreen = Effects.showOrientationLock(sprite, context.getCanvas());
        orientationLockScreen.update(1f);

        String expected = """
            Stage
                $$root [0]
            """;

        assertEquals(expected, context.getStage().toString());
    }

    @Test
    void showOrientationLockWhenPortraitMode() {
        HeadlessRenderer context = new HeadlessRenderer();
        context.getCanvas().resizeScreen(200, 600);

        Sprite sprite = new MockImage("orientation lock").toSprite();
        context.getStage().getRoot().addChild(sprite);

        Actor orientationLockScreen = Effects.showOrientationLock(sprite, context.getCanvas());
        orientationLockScreen.update(1f);

        String expected = """
            Stage
                $$root [1]
                    Sprite [$$default]
            """;

        assertEquals(expected, context.getStage().toString());
    }

    @Test
    void showThenHideOrientationLockWhenSwitching() {
        HeadlessRenderer context = new HeadlessRenderer();

        Sprite sprite = new MockImage("orientation lock").toSprite();
        context.getStage().getRoot().addChild(sprite);

        Actor orientationLockScreen = Effects.showOrientationLock(sprite, context.getCanvas());
        orientationLockScreen.update(1f);
        context.getCanvas().resizeScreen(200, 600);
        orientationLockScreen.update(1f);
        context.getCanvas().resizeScreen(600, 200);
        orientationLockScreen.update(1f);

        String expected = """
            Stage
                $$root [1]
                    Sprite [$$default]
            """;

        assertEquals(expected, context.getStage().toString());
    }

    @Test
    void keepPosition() {
        HeadlessRenderer context = new HeadlessRenderer();
        Canvas canvas = context.getCanvas();

        List<Sprite> sprites = IntStream.range(0, 8)
            .mapToObj(_ -> new Sprite(new MockImage(100, 100)))
            .peek(sprite -> context.getStage().getRoot().addChild(sprite))
            .toList();

        context.attach(Effects.keepMiddleLeft(sprites.get(0), canvas, 10, 20));
        context.attach(Effects.keepBottomLeft(sprites.get(1), canvas, 10, 20));
        context.attach(Effects.keepTopCenter(sprites.get(2), canvas, 10));
        context.attach(Effects.keepCenter(sprites.get(3), canvas, 10));
        context.attach(Effects.keepBottomCenter(sprites.get(4), canvas, 10));
        context.attach(Effects.keepTopRight(sprites.get(5), canvas, 10, 20));
        context.attach(Effects.keepMiddleRight(sprites.get(6), canvas, 10, 20));
        context.attach(Effects.keepBottomRight(sprites.get(7), canvas, 10, 20));
        context.doFrame(1.0);

        assertEquals(800, canvas.getWidth());
        assertEquals(600, canvas.getHeight());
        assertEquals("(10, 320)", sprites.get(0).getTransform().getPosition().toString());
        assertEquals("(10, 580)", sprites.get(1).getTransform().getPosition().toString());
        assertEquals("(400, 10)", sprites.get(2).getTransform().getPosition().toString());
        assertEquals("(400, 310)", sprites.get(3).getTransform().getPosition().toString());
        assertEquals("(400, 590)", sprites.get(4).getTransform().getPosition().toString());
        assertEquals("(790, 20)", sprites.get(5).getTransform().getPosition().toString());
        assertEquals("(790, 320)", sprites.get(6).getTransform().getPosition().toString());
        assertEquals("(790, 580)", sprites.get(7).getTransform().getPosition().toString());
    }
}
