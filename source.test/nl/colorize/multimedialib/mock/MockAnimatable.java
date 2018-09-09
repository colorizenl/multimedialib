package nl.colorize.multimedialib.mock;

import nl.colorize.util.animation.Animatable;

public class MockAnimatable implements Animatable {

    private int frameCount;
    private boolean completed;

    public MockAnimatable() {
        this.frameCount = 0;
        this.completed = false;
    }

    @Override
    public void onFrame(float deltaTime) {
        frameCount++;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }
}
