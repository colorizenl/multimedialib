//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.primitives.Longs;
import nl.colorize.util.Stopwatch;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FrameStatsTest {

    private static final float EPSILON = 0.001f;

    @Test
    void calculateFramerateFromTimestamp() {
        FrameStats frameStats = new FrameStats(mockTimer(0L, 1000L, 1500L, 1800L, 3000L));
        frameStats.markFrameStart();
        frameStats.markFrameUpdate();
        frameStats.markFrameRender();
        frameStats.markFrameStart();

        assertEquals(1f, frameStats.getFramerate(), EPSILON);
        assertEquals(500L, frameStats.getFrameUpdateTime());
        assertEquals(300L, frameStats.getFrameRenderTime());
    }

    @Test
    void trackMultipleFrames() {
        Stopwatch timer = mockTimer(0L, 1000L, 1500L, 1800L, 2000L, 2500L, 2800L, 3000L);
        FrameStats frameStats = new FrameStats(timer);
        frameStats.markFrameStart();
        frameStats.markFrameUpdate();
        frameStats.markFrameRender();
        frameStats.markFrameStart();
        frameStats.markFrameUpdate();
        frameStats.markFrameRender();
        frameStats.markFrameStart();

        assertEquals(1f, frameStats.getFramerate(), EPSILON);
        assertEquals(500L, frameStats.getFrameUpdateTime());
        assertEquals(300L, frameStats.getFrameRenderTime());
    }

    @Test
    void customStats() {
        FrameStats frameStats = new FrameStats();
        frameStats.markCustom("A", 10f);
        frameStats.markCustom("A", 20f);
        frameStats.markCustom("B", 30f);

        assertEquals(Map.of("A", 15, "B", 30), frameStats.getCustomStats());
    }

    private Stopwatch mockTimer(long... values) {
        List<Long> remainingValues = new ArrayList<>();
        remainingValues.addAll(Longs.asList(values));

        return new Stopwatch() {
            @Override
            public long value() {
                Long value = remainingValues.get(0);
                remainingValues.remove(0);
                return value;
            }
        };
    }
}
