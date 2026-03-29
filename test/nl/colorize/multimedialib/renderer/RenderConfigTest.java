//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.math.Size;
import nl.colorize.multimedialib.mock.MockScene;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RenderConfigTest {

    @Test
    void launchRenderer() {
        Canvas canvas = new Canvas(800, 600, ScaleStrategy.flexible());
        MockScene scene = new MockScene();
        RenderConfig.headless(GraphicsMode.HEADLESS, canvas).start(scene);

        assertEquals(1, scene.getStartCount());
    }

    @Test
    void launchSimulationMode() {
        Canvas canvas = new Canvas(800, 600, ScaleStrategy.flexible());
        MockScene scene = new MockScene();
        RenderConfig config = RenderConfig.headless(GraphicsMode.HEADLESS, canvas)
            .withSimulationMode("ios");
        config.start(scene);

        assertEquals("350x760",
            config.getWindowOptions().getWindowSize().map(Size::toString).orElse(""));
    }
}
