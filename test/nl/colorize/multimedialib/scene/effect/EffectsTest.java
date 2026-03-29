//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Text;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        context.attach(Effects.scaleToFit(sprite));
        context.doFrame(1f);

        assertEquals(600f, sprite.getTransform().getScaleX(), EPSILON);
        assertEquals(600f, sprite.getTransform().getScaleY(), EPSILON);
    }

    @Test
    void appearText() {
        Text text = new Text("This is a test", null);
        Scene effect = Effects.appearText(text, 2f);

        effect.update(null, 0f);
        assertEquals(List.of(""), text.getLines());

        effect.update(null, 1f);
        assertEquals(List.of("This is"), text.getLines());

        effect.update(null, 2f);
        assertEquals(List.of("This is a test"), text.getLines());

        effect.update(null, 3f);
        assertEquals(List.of("This is a test"), text.getLines());
    }

    @Test
    void doNotShowOrientationLockInLandscapeMode() {
        HeadlessRenderer context = new HeadlessRenderer();

        Sprite sprite = new MockImage("orientation lock").toSprite();
        Scene orientationLockScreen = Effects.showOrientationLock(sprite);
        orientationLockScreen.update(context, 1f);

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

        Scene orientationLockScreen = Effects.showOrientationLock(sprite);
        orientationLockScreen.update(context, 1f);

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

        Scene orientationLockScreen = Effects.showOrientationLock(sprite);
        orientationLockScreen.update(context, 1f);
        context.getCanvas().resizeScreen(200, 600);
        orientationLockScreen.update(context, 1f);
        context.getCanvas().resizeScreen(600, 200);
        orientationLockScreen.update(context, 1f);

        String expected = """
            Stage
                $$root [1]
                    Sprite [$$default]
            """;

        assertEquals(expected, context.getStage().toString());
    }
}
