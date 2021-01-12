//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import nl.colorize.multimedialib.renderer.GraphicsContext2D;
import nl.colorize.multimedialib.scene.Application;
import nl.colorize.multimedialib.scene.Scene;

import java.util.concurrent.atomic.AtomicInteger;

public class MockScene implements Scene {
    
    private AtomicInteger startCount;
    private AtomicInteger endCount;
    private AtomicInteger frameUpdateCount;
    private AtomicInteger renderCount;
    
    public MockScene() {
        this.startCount = new AtomicInteger(0);
        this.endCount = new AtomicInteger(0);
        this.frameUpdateCount = new AtomicInteger(0);
        this.renderCount = new AtomicInteger(0);
    }

    @Override
    public void start(Application app) {
        startCount.incrementAndGet();
    }

    @Override
    public void end(Application app) {
        endCount.incrementAndGet();
    }

    @Override
    public void update(Application app, float deltaTime) {
        frameUpdateCount.incrementAndGet();
    }

    @Override
    public void render(Application app, GraphicsContext2D graphics) {
        renderCount.incrementAndGet();
    }

    public int getStartCount() {
        return startCount.get();
    }

    public int getEndCount() {
        return endCount.get();
    }

    public int getFrameUpdateCount() {
        return frameUpdateCount.get();
    }

    public int getRenderCount() {
        return renderCount.get();
    }
}
