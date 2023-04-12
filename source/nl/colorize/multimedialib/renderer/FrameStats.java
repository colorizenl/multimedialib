//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.math.RingBuffer;
import nl.colorize.multimedialib.stage.Graphic2D;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.util.Stopwatch;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tracks performance statistics for each frame. The renderer will mark both
 * the frame update and rendering the frame. Statistics are reported use a
 * sliding window.
 * <p>
 * In addition to the standard statistics like frame update time and frame
 * render time, it is also possible to register custom, application-specific
 * statistics that are tracked every frame.
 */
public class FrameStats {

    private Stopwatch timer;

    private RingBuffer frameTime;
    private RingBuffer frameUpdateTime;
    private RingBuffer frameRenderTime;
    private int spriteCount;
    private int primitiveCount;
    private int textCount;
    private Map<String, RingBuffer> customStats;

    private static final int FRAME_STATISTICS_WINDOW = 60;

    public FrameStats() {
        this(new Stopwatch());
    }

    protected FrameStats(Stopwatch timer) {
        this.timer = timer;

        this.frameTime = new RingBuffer(FRAME_STATISTICS_WINDOW);
        this.frameUpdateTime = new RingBuffer(FRAME_STATISTICS_WINDOW);
        this.frameRenderTime = new RingBuffer(FRAME_STATISTICS_WINDOW);
        this.customStats = new LinkedHashMap<>();
    }

    public void markFrameStart() {
        frameTime.add(timer.tick());
    }

    public void markFrameUpdate() {
        frameUpdateTime.add(timer.tock());
    }

    public void markFrameRender() {
        frameRenderTime.add(Math.max(timer.tock() - frameUpdateTime.getLatestValue(), 0));
    }

    public void markDrawOperation(Graphic2D graphic) {
        if (graphic instanceof Sprite) {
            spriteCount++;
        } else if (graphic instanceof Primitive) {
            primitiveCount++;
        } else if (graphic instanceof Text) {
            textCount++;
        }
    }

    public void markCustom(String name, float time) {
        RingBuffer buffer = customStats.get(name);
        if (buffer == null) {
            buffer = new RingBuffer(FRAME_STATISTICS_WINDOW);
            customStats.put(name, buffer);
        }
        buffer.add(time);
    }

    public void markCustom(Object phase, float time) {
        markCustom(phase.getClass().getSimpleName(), time);
    }

    public void resetDrawOperations() {
        spriteCount = 0;
        primitiveCount = 0;
        textCount = 0;
    }

    public float getFrameTime() {
        return frameTime.getAverageValue();
    }

    public int getFramerate() {
        return Math.round(1f / (getFrameTime() / 1000f));
    }

    public int getFrameUpdateTime() {
        return Math.round(frameUpdateTime.getAverageValue());
    }

    public int getFrameRenderTime() {
        return Math.round(frameRenderTime.getAverageValue());
    }

    public int getSpriteCount() {
        return spriteCount;
    }

    public int getPrimitiveCount() {
        return primitiveCount;
    }

    public int getTextCount() {
        return textCount;
    }

    public Map<String, Integer> getCustomStats() {
        Map<String, Integer> averages = new LinkedHashMap<>();
        for (Map.Entry<String, RingBuffer> entry : customStats.entrySet()) {
            averages.put(entry.getKey(), Math.round(entry.getValue().getAverageValue()));
        }
        return averages;
    }
}
