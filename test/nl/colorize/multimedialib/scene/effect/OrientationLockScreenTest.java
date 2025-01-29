//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import nl.colorize.multimedialib.stage.Sprite;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrientationLockScreenTest {

    @Test
    void doNotShowInLandscapeMode() {
        HeadlessRenderer context = new HeadlessRenderer(false);

        Sprite sprite = new MockImage("orientation lock").toSprite();
        OrientationLockScreen orientationLockScreen = new OrientationLockScreen(sprite);
        orientationLockScreen.update(context, 1f);

        String expected = """
            Stage
                $$root [0]
            """;

        assertEquals(expected, context.getStage().toString());
    }

    @Test
    void showWhenPortraitMode() {
        HeadlessRenderer context = new HeadlessRenderer(false);
        context.getCanvas().resizeScreen(200, 600);

        Sprite sprite = new MockImage("orientation lock").toSprite();
        context.getStage().getRoot().addChild(sprite);

        OrientationLockScreen orientationLockScreen = new OrientationLockScreen(sprite);
        orientationLockScreen.update(context, 1f);

        String expected = """
            Stage
                $$root [1]
                    Sprite [$$default]
            """;

        assertEquals(expected, context.getStage().toString());
    }

    @Test
    void showThenHideWhenSwitching() {
        HeadlessRenderer context = new HeadlessRenderer(false);

        Sprite sprite = new MockImage("orientation lock").toSprite();
        context.getStage().getRoot().addChild(sprite);

        OrientationLockScreen orientationLockScreen = new OrientationLockScreen(sprite);
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
