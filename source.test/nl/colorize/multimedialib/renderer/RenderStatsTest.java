//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import org.junit.Test;

import static org.junit.Assert.*;

public class RenderStatsTest {

    @Test
    public void testAverageUPS() throws InterruptedException {
        RenderStats stats = new RenderStats();
        stats.onFrame(1f, null);
        stats.onFrame(1f, null);
        Thread.sleep(500);
        stats.onFrame(1f, null);
        Thread.sleep(500);
        stats.onFrame(1f, null);

        assertEquals(2f, stats.getAverageUPS(), 1f);
    }

    @Test
    public void testSamplesAreStoredInRotatingBuffer() {
        RenderStats stats = new RenderStats();

        for (int i = 0; i < 1000; i++) {
            stats.onFrame(1f, null);
        }

        assertEquals(1f, stats.getAverageFPS(), 1f);
    }
}