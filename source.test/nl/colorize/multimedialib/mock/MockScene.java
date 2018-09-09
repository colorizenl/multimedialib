//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.RenderContext;
import nl.colorize.multimedialib.scene.Scene;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mock implementation of the {@code Scene} interface.
 */
public class MockScene implements Scene {
    
    private AtomicInteger startCount;
    private AtomicInteger endCount;
    private AtomicInteger frameUpdateCount;
    private AtomicInteger renderCount;
    
    public MockScene() {
        startCount = new AtomicInteger(0);
        endCount = new AtomicInteger(0);
        frameUpdateCount = new AtomicInteger(0);
        renderCount = new AtomicInteger(0);
    }

    @Override
    public void onSceneStart(MediaLoader mediaLoader) {
        startCount.incrementAndGet();
    }

    @Override
    public void onFrame(float deltaTime, InputDevice input) {
        frameUpdateCount.incrementAndGet();
    }

    @Override
    public void onRender(RenderContext context) {
        renderCount.incrementAndGet();
    }

    @Override
    public void onSceneEnd() {
        endCount.incrementAndGet();
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
