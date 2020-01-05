//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import nl.colorize.multimedialib.renderer.GraphicsContext;
import nl.colorize.multimedialib.scene.Scene;

import java.util.concurrent.atomic.AtomicInteger;

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
    public void start() {
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
