//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.mock.MockScene;
import org.junit.Test;

import static org.junit.Assert.*;

public class SceneManagerTest {

    @Test
    public void testInitialScene() {
        MockScene sceneA = new MockScene();
        MockScene sceneB = new MockScene();

        SceneManager sceneManager = new SceneManager(null, sceneA);
        sceneManager.onFrame(1f, null);
        sceneManager.onFrame(1f, null);

        assertEquals(1, sceneA.getStartCount());
        assertEquals(0, sceneA.getEndCount());
        assertEquals(2, sceneA.getFrameUpdateCount());

        assertEquals(0, sceneB.getStartCount());
        assertEquals(0, sceneB.getEndCount());
        assertEquals(0, sceneB.getFrameUpdateCount());
    }

    @Test
    public void testChangeScene() {
        MockScene sceneA = new MockScene();
        MockScene sceneB = new MockScene();

        SceneManager sceneManager = new SceneManager(null, sceneA);
        sceneManager.onFrame(1f, null);
        sceneManager.changeScene(sceneB);
        sceneManager.onFrame(1f, null);
        sceneManager.onFrame(1f, null);

        assertEquals(1, sceneA.getStartCount());
        assertEquals(1, sceneA.getEndCount());
        assertEquals(1, sceneA.getFrameUpdateCount());

        assertEquals(1, sceneB.getStartCount());
        assertEquals(0, sceneB.getEndCount());
        assertEquals(2, sceneB.getFrameUpdateCount());
    }
}