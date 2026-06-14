//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.Actor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MockScene implements Scene, Actor {
    
    private AtomicInteger startCount;
    private AtomicInteger endCount;
    private AtomicInteger frameUpdateCount;
    private AtomicBoolean completed;

    public MockScene() {
        this.startCount = new AtomicInteger(0);
        this.endCount = new AtomicInteger(0);
        this.frameUpdateCount = new AtomicInteger(0);
        this.completed = new AtomicBoolean(false);
    }

    @Override
    public void start(SceneContext context) {
        startCount.incrementAndGet();
    }

    @Override
    public void end(SceneContext context) {
        endCount.incrementAndGet();
    }

    @Override
    public void update(SceneContext context, double deltaTime) {
        frameUpdateCount.incrementAndGet();
    }

    @Override
    public void update(double deltaTime) {
        frameUpdateCount.incrementAndGet();
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

    public void setCompleted(boolean completed) {
        this.completed.set(completed);
    }

    @Override
    public boolean isCompleted() {
        return completed.get();
    }
}
