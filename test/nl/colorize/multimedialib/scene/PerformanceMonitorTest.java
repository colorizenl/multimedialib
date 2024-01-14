//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PerformanceMonitorTest {

    @Test
    void depictPerformanceStats() {
        PerformanceMonitor performanceMonitor = new PerformanceMonitor(true);
        HeadlessRenderer renderer = new HeadlessRenderer();
        renderer.start(performanceMonitor, null);

        for (int i = 0; i < 20; i++) {
            FrameStats frameStats = renderer.getContext().getFrameStats();
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
                Container
                    Container
                        Primitive [(0, 0, 300, 100)]
                        Container
                            Primitive [(0, 100) -> (3, 100) -> (6, 100) -> (9, 100) -> (12, 100) -> (15, 100) -> (18, 100) -> (21, 100) -> (24, 100) -> (27, 100) -> (30, 100) -> (33, 100) -> (36, 100) -> (39, 100) -> (42, 100) -> (45, 100) -> (48, 100) -> (51, 100) -> (54, 100) -> (57, 100)]
                            Primitive [(0, 100) -> (3, 100) -> (6, 100) -> (9, 100) -> (12, 100) -> (15, 100) -> (18, 100) -> (21, 100) -> (24, 100) -> (27, 100) -> (30, 100) -> (33, 100) -> (36, 100) -> (39, 100) -> (42, 100) -> (45, 100) -> (48, 100) -> (51, 100) -> (54, 100) -> (57, 100)]
                            Primitive [(0, 100) -> (3, 100) -> (6, 100) -> (9, 100) -> (12, 100) -> (15, 100) -> (18, 100) -> (21, 100) -> (24, 100) -> (27, 100) -> (30, 100) -> (33, 100) -> (36, 100) -> (39, 100) -> (42, 100) -> (45, 100) -> (48, 100) -> (51, 100) -> (54, 100) -> (57, 100)]
                        Text [1,000.0]
                        Primitive [(0, 0) -> (300, 0)]
                        Primitive [(0, 20) -> (300, 20)]
                        Text [10ms]
                        Primitive [(0, 40) -> (300, 40)]
                        Text [20ms]
                        Primitive [(0, 60) -> (300, 60)]
                        Text [30ms]
                        Primitive [(0, 80) -> (300, 80)]
                        Text [40ms]
                        Primitive [(0, 100) -> (300, 100)]
                        Text [50ms]
                        Primitive [(0, 0) -> (300, 0)]
                        Primitive [(0, 100) -> (300, 100)]
                        Primitive [(0, 0) -> (0, 100)]
                        Primitive [(300, 0) -> (300, 100)]
            """;

        assertEquals(expected, renderer.getContext().getStage().toString());
    }
}
