//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.util.Stopwatch;
import nl.colorize.util.stats.Aggregate;

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

    private Map<String, PhaseStats> stats;

    public static final String PHASE_FRAME_TIME = "$$frameTime";
    public static final String PHASE_FRAME_UPDATE = "$$frameUpdate";
    public static final String PHASE_FRAME_RENDER = "$$frameRender";
    public static final int BUFFER_CAPACITY = 60;

    public FrameStats() {
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

        while (phaseStats.values.size() > BUFFER_CAPACITY) {
            phaseStats.values.removeFirst();
        }
    }

    public float getAverageFramerate() {
        int frameTimeMS = getAverageTimeMS(PHASE_FRAME_TIME);
        return 1000f / Math.max(frameTimeMS, 1);
    }

    public int getFrameUpdateTime() {
        return getAverageTimeMS(PHASE_FRAME_UPDATE);
    }

    public int getFrameRenderTime() {
        return getAverageTimeMS(PHASE_FRAME_RENDER);
    }

    /**
     * Returns the average duration for the specified phase, in milliseconds.
     * The average is based on all previously measured frames that are
     * currently in the buffer.
     */
    public int getAverageTimeMS(String phase) {
        PhaseStats phaseStats = prepare(phase);
        return (int) Aggregate.average(phaseStats.values);
    }

    /**
     * Returns the average duration for the specified phase, in seconds.
     * The average is based on all previously measured frames that are
     * currently in the buffer.
     */
    public float getAverageTime(String phase) {
        return getAverageTimeMS(phase) / 1000f;
    }

    /**
     * Returns the names of all custom statistics that have been measured and
     * are currently in the buffer. Note this does <em>not</em> return the
     * standard performance statistics that are <em>always</em> measured.
     */
    public List<String> getCustomStats() {
        return stats.keySet().stream()
            .filter(phase -> !phase.startsWith("$$"))
            .toList();
    }

    /**
     * Returns all measured frame times for the specified phase that are
     * currently in the buffer, in millisecond precision. Frames are sorted
     * so that the oldest frame is first, and the most recent frame is last.
     */
    public Iterable<Long> getFrameTimes(String phase) {
        return prepare(phase).values();
    }

    /**
     * Returns the number of frames that have been measured and that are
     * currently in the buffer.
     */
    public int getBufferSize() {
        return prepare(PHASE_FRAME_TIME).values().size();
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
