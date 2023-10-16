//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import nl.colorize.multimedialib.stage.Sprite;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrientationLockScreenTest {

    @Test
    void doNotShowInLandscapeMode() {
        HeadlessRenderer renderer = new HeadlessRenderer();
        SceneContext context = renderer.getContext();

        Sprite sprite = new MockImage("orientation lock").toSprite();
        OrientationLockScreen orientationLockScreen = new OrientationLockScreen(sprite);
        context.getStage().getRoot().addChild(orientationLockScreen.getContainer());
        orientationLockScreen.update(context, 1f);

        String expected = """
            Stage
                Container
            """;

        assertEquals(expected, context.getStage().toString());
    }

    @Test
    void showWhenPortraitMode() {
        HeadlessRenderer renderer = new HeadlessRenderer();
        SceneContext context = renderer.getContext();
        context.getCanvas().resizeScreen(200, 600);

        Sprite sprite = new MockImage("orientation lock").toSprite();
        OrientationLockScreen orientationLockScreen = new OrientationLockScreen(sprite);
        context.getStage().getRoot().addChild(orientationLockScreen.getContainer());
        orientationLockScreen.update(context, 1f);

        String expected = """
            Stage
                Container
                    Container
                        Sprite [$$default@0.0]
            """;

        assertEquals(expected, context.getStage().toString());
    }

    @Test
    void showThenHideWhenSwitching() {
        HeadlessRenderer renderer = new HeadlessRenderer();
        SceneContext context = renderer.getContext();

        Sprite sprite = new MockImage("orientation lock").toSprite();
        OrientationLockScreen orientationLockScreen = new OrientationLockScreen(sprite);
        context.getStage().getRoot().addChild(orientationLockScreen.getContainer());
        orientationLockScreen.update(context, 1f);
        context.getCanvas().resizeScreen(200, 600);
        orientationLockScreen.update(context, 1f);
        context.getCanvas().resizeScreen(600, 200);
        orientationLockScreen.update(context, 1f);

        String expected = """
            Stage
                Container
            """;

        assertEquals(expected, context.getStage().toString());
    }
}
