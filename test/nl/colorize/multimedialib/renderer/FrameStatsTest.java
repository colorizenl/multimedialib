//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.primitives.Longs;
import nl.colorize.util.Stopwatch;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FrameStatsTest {

    private static final float EPSILON = 0.001f;

    @Test
    void calculateFramerateFromTimestamp() {
        FrameStats frameStats = new FrameStats(mockTimer(0L, 1000L, 1500L, 1800L, 3000L), 60);
        frameStats.markFrameStart();
        frameStats.markFrameUpdate();
        frameStats.markFrameRender();
        frameStats.markFrameStart();

        assertEquals(0.5f, frameStats.getFramerate(), EPSILON);
        assertEquals(500L, frameStats.getUpdateTime());
        assertEquals(300L, frameStats.getRenderTime());
    }

    @Test
    void trackMultipleFrames() {
        Stopwatch timer = mockTimer(0L, 1000L, 1500L, 1800L, 2000L, 2500L, 2800L, 3000L);
        FrameStats frameStats = new FrameStats(timer, 60);
        frameStats.markFrameStart();
        frameStats.markFrameUpdate();
        frameStats.markFrameRender();
        frameStats.markFrameStart();
        frameStats.markFrameUpdate();
        frameStats.markFrameRender();
        frameStats.markFrameStart();

        assertEquals(1f, frameStats.getFramerate(), EPSILON);
        assertEquals(500L, frameStats.getUpdateTime());
        assertEquals(300L, frameStats.getRenderTime());
    }

    @Test
    void useWeightedValuesWhenCalculatingAverage() {
        Stopwatch timer = mockTimer(0L, 1000L, 1500L, 1800L, 3000L, 3100L, 3200L, 4000L);
        FrameStats frameStats = new FrameStats(timer, 60);
        frameStats.markFrameStart();
        frameStats.markFrameUpdate();
        frameStats.markFrameRender();
        frameStats.markFrameStart();
        frameStats.markFrameUpdate();
        frameStats.markFrameRender();
        frameStats.markFrameStart();

        assertEquals(0.55f, frameStats.getFramerate(), EPSILON);
        assertEquals(460L, frameStats.getUpdateTime());
        assertEquals(280L, frameStats.getRenderTime());
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
