//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import nl.colorize.multimedialib.stage.ColorRGB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParticleWipeTest {

    @Test
    void renderParticles() {
        HeadlessRenderer renderer = new HeadlessRenderer();
        renderer.start(new ParticleWipe(new MockImage(), ColorRGB.RED, 10f, false), null);
        renderer.doFrame();
        renderer.doFrame();

        String expected = """
            Stage
                Container
                    Container
                        Sprite [$$default@0.1]
                        Sprite [$$default@0.1]
                        Sprite [$$default@0.1]
                        Sprite [$$default@0.1]
                        Sprite [$$default@0.1]
                        Sprite [$$default@0.1]
                        Sprite [$$default@0.1]
                        Sprite [$$default@0.1]
                        Sprite [$$default@0.1]
                        Sprite [$$default@0.1]
                        Sprite [$$default@0.1]
            """;

        assertEquals(expected, renderer.getContext().getStage().toString());
    }
}
