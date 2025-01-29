//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PerformanceMonitorTest {

    @Test
    void depictPerformanceStats() {
        PerformanceMonitor performanceMonitor = new PerformanceMonitor(true);
        HeadlessRenderer renderer = new HeadlessRenderer(false);
        renderer.start(performanceMonitor);

        for (int i = 0; i < 20; i++) {
            FrameStats frameStats = renderer.getSceneManager().getFrameStats();
            frameStats.markStart(FrameStats.PHASE_FRAME_TIME);
            frameStats.markEnd(FrameStats.PHASE_FRAME_TIME);
            frameStats.markStart(FrameStats.PHASE_FRAME_UPDATE);
            frameStats.markEnd(FrameStats.PHASE_FRAME_UPDATE);
            frameStats.markStart(FrameStats.PHASE_FRAME_RENDER);
            frameStats.markEnd(FrameStats.PHASE_FRAME_RENDER);

            renderer.doFrame();
        }

        String expected = """
            Stage
                $$root [1]
                    Container [18]
                        Rect [(0, 0, 300, 100)]
                        Container [3]
                            SegmentedLine
                            SegmentedLine
                            SegmentedLine
                        Text [1,000.0]
                        Line
                        Line
                        Text [10ms]
                        Line
                        Text [20ms]
                        Line
                        Text [30ms]
                        Line
                        Text [40ms]
                        Line
                        Text [50ms]
                        Line
                        Line
                        Line
                        Line
            """;

        assertEquals(expected, renderer.getStage().toString());
    }
}
