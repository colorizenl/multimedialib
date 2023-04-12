//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.scene.Updatable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FrameSyncTest {

    private static final Canvas CANVAS = Canvas.forNative(800, 600);

    @Test
    void nativeFramerate() {
        FrameSync frameSync = new FrameSync(new DisplayMode(CANVAS, 10));
        Counter counter = new Counter(new ArrayList<>());
        frameSync.requestFrame(1000L, counter);
        frameSync.requestFrame(1100L, counter);
        frameSync.requestFrame(1200L, counter);

        assertEquals("[0.10, 0.10, 0.10]", counter.frames.toString());
    }

    @Test
    void applicationFramerateSlowerThanRefreshRate() {
        FrameSync frameSync = new FrameSync(new DisplayMode(CANVAS, 10));
        Counter counter = new Counter(new ArrayList<>());
        frameSync.requestFrame(1000L, counter);
        frameSync.requestFrame(1100L, counter);
        frameSync.requestFrame(1150L, counter);
        frameSync.requestFrame(1200L, counter);
        frameSync.requestFrame(1300L, counter);

        assertEquals("[0.10, 0.10, 0.10, 0.10]", counter.frames.toString());
    }

    @Test
    void slowerFramerateWithNonExactMatch() {
        FrameSync frameSync = new FrameSync(new DisplayMode(CANVAS, 10));
        Counter counter = new Counter(new ArrayList<>());
        frameSync.requestFrame(1000L, counter);
        frameSync.requestFrame(1100L, counter);
        frameSync.requestFrame(1130L, counter);
        frameSync.requestFrame(1160L, counter);
        frameSync.requestFrame(1190L, counter);
        frameSync.requestFrame(1220L, counter);
        frameSync.requestFrame(1250L, counter);
        frameSync.requestFrame(1300L, counter);

        assertEquals("[0.10, 0.10, 0.12]", counter.frames.toString());
    }

    @Test
    void applicationFramerateFasterThanRefreshRate() {
        FrameSync frameSync = new FrameSync(new DisplayMode(CANVAS, 10));
        Counter counter = new Counter(new ArrayList<>());
        frameSync.requestFrame(1000L, counter);
        frameSync.requestFrame(1200L, counter);
        frameSync.requestFrame(1400L, counter);

        assertEquals("[0.10, 0.20, 0.20]", counter.frames.toString());
    }

    @Test
    void limitExtremelyLargeDeltaTime() {
        FrameSync frameSync = new FrameSync(new DisplayMode(CANVAS, 10));
        Counter counter = new Counter(new ArrayList<>());
        frameSync.requestFrame(1000L, counter);
        frameSync.requestFrame(1200L, counter);
        frameSync.requestFrame(9999L, counter);

        assertEquals("[0.10, 0.20, 0.20]", counter.frames.toString());
    }

    @Test
    void allowFramesThatAreLittleBitTooShort() {
        FrameSync frameSync = new FrameSync(new DisplayMode(CANVAS, 10));
        Counter counter = new Counter(new ArrayList<>());
        frameSync.requestFrame(1000L, counter);
        frameSync.requestFrame(1100L, counter);
        frameSync.requestFrame(1195L, counter);
        frameSync.requestFrame(1300L, counter);

        assertEquals("[0.10, 0.10, 0.09, 0.10]", counter.frames.toString());
    }

    private static record Counter(List<String> frames) implements Updatable {

        @Override
        public void update(float deltaTime) {
            frames.add(String.format("%.2f", deltaTime));
        }
    }
}
