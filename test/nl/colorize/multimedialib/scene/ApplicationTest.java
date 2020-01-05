//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.mock.MockRenderer;
import nl.colorize.multimedialib.mock.MockScene;
import org.junit.Test;

import static org.junit.Assert.*;

public class ApplicationTest {

    @Test
    public void testInitialScene() {
        MockScene sceneA = new MockScene();
        MockScene sceneB = new MockScene();

        Application app = new Application(new MockRenderer());
        app.changeScene(sceneA);
        app.update(1f);
        app.update(1f);

        assertEquals(1, sceneA.getStartCount());
        assertEquals(2, sceneA.getFrameUpdateCount());

        assertEquals(0, sceneB.getStartCount());
        assertEquals(0, sceneB.getFrameUpdateCount());
    }

    @Test
    public void testChangeScene() {
        MockScene sceneA = new MockScene();
        MockScene sceneB = new MockScene();

        Application app = new Application(new MockRenderer());
        app.changeScene(sceneA);
        app.update(1f);
        app.changeScene(sceneB);
        app.update(1f);
        app.update(1f);

        assertEquals(1, sceneA.getStartCount());
        assertEquals(1, sceneA.getFrameUpdateCount());

        assertEquals(1, sceneB.getStartCount());
        assertEquals(2, sceneB.getFrameUpdateCount());
    }
}
