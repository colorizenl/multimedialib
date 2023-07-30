//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.util.Stopwatch;
import nl.colorize.util.stats.Aggregates;

import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Tracks performance statistics for each frame, then reports on average
 * performance behavior over time using a sliding window.
 * <p>
 * The most essential performance information is the application's
 * <em>actual</em> framerate, which is measured by the renderer itself.
 * In addition, this class can be used to measure performance <em>within</em>
 * each frame. The renderer will automatically measure frame update logic and
 * rendering the frame, but this can be extended by registering additioanl
 * application-specific statistics which are then also tracked every frame.
 */
public class FrameStats {

    private DisplayMode displayMode;
    private Map<String, PhaseStats> stats;

    public static final String PHASE_NATIVE_ANIMATION_LOOP_FRAME_TIME = "$$nativeFrameTime";
    public static final String PHASE_FRAME_TIME = "$$frameTime";
    public static final String PHASE_FRAME_UPDATE = "$$frameUpdate";
    public static final String PHASE_FRAME_RENDER = "$$frameRender";
    private static final int SLIDING_WINDOW_FRAMES = 60;

    public FrameStats(DisplayMode displayMode) {
        this.displayMode = displayMode;
        this.stats = new LinkedHashMap<>();
    }

    private PhaseStats prepare(String phase) {
        PhaseStats phaseStats = stats.get(phase);
        if (phaseStats == null) {
            phaseStats = new PhaseStats(phase, new Stopwatch(), new LinkedList<>());
            stats.put(phase, phaseStats);
        }
        return phaseStats;
    }

    public void markStart(String phase) {
        PhaseStats phaseStats = prepare(phase);
        phaseStats.timer.tick();
    }

    public void markEnd(String phase) {
        PhaseStats phaseStats = prepare(phase);
        long value = phaseStats.timer.tick();
        phaseStats.values.add(value);

        while (phaseStats.values.size() > SLIDING_WINDOW_FRAMES) {
            phaseStats.values.removeFirst();
        }
    }

    public int getTargetFramerate() {
        return displayMode.framerate();
    }

    public int getActualFramerate() {
        int frameTimeMS = getAverageTimeMS(PHASE_FRAME_TIME);
        return 1000 / Math.max(frameTimeMS, 1);
    }

    public int getFrameUpdateTime() {
        return getAverageTimeMS(PHASE_FRAME_UPDATE);
    }

    public int getFrameRenderTime() {
        return getAverageTimeMS(PHASE_FRAME_RENDER);
    }

    /**
     * Returns the average duration for the specified phase based on previously
     * measured frames, in milliseconds.
     */
    public int getAverageTimeMS(String phase) {
        PhaseStats phaseStats = prepare(phase);
        return (int) Aggregates.average(phaseStats.values);
    }

    public List<String> getCustomStats() {
        return stats.keySet().stream()
            .filter(phase -> !phase.startsWith("$$"))
            .toList();
    }

    /**
     * Returns a list containing all "native" frame times currently in the
     * buffer, in millisecond precision.
     */
    public Deque<Long> getFrameTimes() {
        return prepare(PHASE_NATIVE_ANIMATION_LOOP_FRAME_TIME).values();
    }

    /**
     * Performance statistics captured during a single frame. Each statistic
     * uses its timer to allow overlapping measurements. Since performance
     * statistics are measured using a timer, measurement values are expressed
     * in milliseconds.
     */
    private record PhaseStats(String phase, Stopwatch timer, Deque<Long> values) {
    }
}
