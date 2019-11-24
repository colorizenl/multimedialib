//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.GraphicsContext;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mock implementation of the {@code Scene} interface.
 */
public class MockScene implements Scene {
    
    private AtomicInteger startCount;
    private AtomicInteger frameUpdateCount;
    private AtomicInteger renderCount;
    
    public MockScene() {
        startCount = new AtomicInteger(0);
        frameUpdateCount = new AtomicInteger(0);
        renderCount = new AtomicInteger(0);
    }

    @Override
    public void start(SceneContext context) {
        startCount.incrementAndGet();
    }

    @Override
    public void update(float deltaTime) {
        frameUpdateCount.incrementAndGet();
    }

    @Override
    public void render(GraphicsContext context) {
        renderCount.incrementAndGet();
    }

    public int getStartCount() {
        return startCount.get();
    }

    public int getFrameUpdateCount() {
        return frameUpdateCount.get();
    }

    public int getRenderCount() {
        return renderCount.get();
    }
}
