//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.mock.MockRenderer;
import nl.colorize.multimedialib.mock.MockScene;
import org.junit.Test;

import static org.junit.Assert.*;

public class SceneManagerTest {

    @Test
    public void testInitialScene() {
        MockScene sceneA = new MockScene();
        MockScene sceneB = new MockScene();

        SceneManager sceneManager = SceneManager.attach(new MockRenderer());
        sceneManager.changeScene(sceneA);
        sceneManager.update(1f);
        sceneManager.update(1f);

        assertEquals(1, sceneA.getStartCount());
        assertEquals(2, sceneA.getFrameUpdateCount());

        assertEquals(0, sceneB.getStartCount());
        assertEquals(0, sceneB.getFrameUpdateCount());
    }

    @Test
    public void testChangeScene() {
        MockScene sceneA = new MockScene();
        MockScene sceneB = new MockScene();

        SceneManager sceneManager = SceneManager.attach(new MockRenderer());
        sceneManager.changeScene(sceneA);
        sceneManager.update(1f);
        sceneManager.changeScene(sceneB);
        sceneManager.update(1f);
        sceneManager.update(1f);

        assertEquals(1, sceneA.getStartCount());
        assertEquals(1, sceneA.getFrameUpdateCount());

        assertEquals(1, sceneB.getStartCount());
        assertEquals(2, sceneB.getFrameUpdateCount());
    }
}
