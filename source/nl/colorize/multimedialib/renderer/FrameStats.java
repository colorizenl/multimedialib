//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.annotations.VisibleForTesting;
import nl.colorize.util.Stopwatch;

/**
 * Performance statistics that are tracked for each frame. The renderer is
 * responsible for marking the start of each frame, the moment the frame
 * logic/update has been completed, and the moment the frame has been rendered.
 * This class will then use these timestamps to calculate overall performance
 * statistics using a sliding window.
 */
public class FrameStats {

    private Stopwatch timer;
    private int targetFramerate;

    // Current frame statistics
    private long currentFrameUpdateTime;
    private long currentFrameRenderTime;

    // Overall statistics
    private float framerate;
    private long updateTime;
    private long renderTime;

    private static final float WEIGHT = 0.9f;

    public FrameStats(int targetFramerate) {
        this(new Stopwatch(), targetFramerate);
    }

    @VisibleForTesting
    protected FrameStats(Stopwatch timer, int targetFramerate) {
        this.timer = timer;
        this.targetFramerate = targetFramerate;
    }

    public void markFrameStart() {
        long frameTime = timer.tick();
        float currentFramerate = 1f / (frameTime / 1000f);

        if (updateTime == 0L) {
            framerate = currentFramerate;
            updateTime = currentFrameUpdateTime;
            renderTime = currentFrameRenderTime;
        } else {
            framerate = WEIGHT * framerate + (1f - WEIGHT) * currentFramerate;
            updateTime = Math.round(WEIGHT * updateTime + (1f - WEIGHT) * currentFrameUpdateTime);
            renderTime = Math.round(WEIGHT * renderTime + (1f - WEIGHT) * currentFrameRenderTime);
        }
    }

    public void markFrameUpdate() {
        currentFrameUpdateTime = timer.tock();
    }

    public void markFrameRender() {
        currentFrameRenderTime = Math.max(timer.tock() - currentFrameUpdateTime, 0);
    }

    public int getTargetFramerate() {
        return targetFramerate;
    }

    public float getFramerate() {
        return framerate;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public long getRenderTime() {
        return renderTime;
    }
}
