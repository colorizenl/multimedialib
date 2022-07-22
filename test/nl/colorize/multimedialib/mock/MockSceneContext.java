//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.scene.SceneContext;

public class MockSceneContext extends SceneContext {

    public MockSceneContext() {
        super(new DisplayMode(Canvas.flexible(800, 600), 60), null, null, null);
    }
}
