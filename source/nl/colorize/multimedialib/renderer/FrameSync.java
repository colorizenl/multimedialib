//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.scene.Updatable;

/**
 * Helper class that can be used by renderers to synchronize between the
 * application's targeted framerate and the display refresh rate. This can be
 * used by renderers that do not support "native" custom framerates, and are
 * instead locked to the platform's native refresh rate. While this seems
 * desirable, it can lead to slowdown if the complexity of the graphics makes
 * it impossible to actually achieve the native framerate.
 * <p>
 * Although this class provides the renderer with the delta time since the last
 * frame update, this may not always reflect the actual elapsed time. It is not
 * realistic to expect applications to be able to function correctly for every
 * possible {@code deltaTime} value, so this class will attempt to produce
 * frame updates that try to find a balance between the targeted framerate and
 * the actual elapsed time.
 */
public class FrameSync {

    private DisplayMode displayMode;
    private long lastFrameTimestamp;
    private long elapsedTime;

    private static final int MAX_FRAMES = 2;
    private static final long LEEWAY_MS = 5;

    public FrameSync(DisplayMode displayMode) {
        this.displayMode = displayMode;
        this.lastFrameTimestamp = 0L;
    }

    /**
     * Should be called by the renderer at the start of every "native" frame
     * update, and will then invoke the specified callback for every
     * application frame update. Depending on the elapsed time and targeted
     * frame rate, each "native" frame could lead to one, zero, or multiple
     * application frame updates.
     * <p>
     * The provided {@code timestamp} value should represent a timestamp with
     * millisecond precision. The absolute value of this timestamp does not
     * matter, this value is purely used for comparison against the previous
     * frame.
     */
    public void requestFrame(long timestamp, Updatable callback) {
        float targetFrameTime = displayMode.getFrameTime();
        long targetFrameTimeMS = displayMode.getFrameTimeMS();

        if (lastFrameTimestamp == 0L) {
            callback.update(targetFrameTime);
            lastFrameTimestamp = timestamp;
            return;
        }

        elapsedTime += timestamp - lastFrameTimestamp;
        lastFrameTimestamp = timestamp;

        if (elapsedTime >= targetFrameTimeMS - LEEWAY_MS) {
            float deltaTime = Math.min(elapsedTime / 1000f, targetFrameTime * MAX_FRAMES);
            callback.update(deltaTime);
            elapsedTime = 0L;
        }
    }
}
