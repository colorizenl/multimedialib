//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.BitmapFont;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Stopwatch;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Base implementation of a renderer that tries to perform frame updates as close
 * to the targeted framerate as possible, but changes the frequency of frame
 * renders depending on performance. This means that several frame updates may
 * occur before a frame is rendered, to ensure performance does not have too much
 * influence on application logic.
 */
public abstract class AnimationLoopRenderer implements Renderer, RenderContext {

    protected ScaleStrategy scaling;
    protected int framerate;
    private List<RenderCallback> callbacks;
    private RenderStats stats;

    private Stopwatch timer;
    private float accumulatedTime;

    private static final Logger LOGGER = LogHelper.getLogger(AnimationLoopRenderer.class);

    public AnimationLoopRenderer(ScaleStrategy scaling, int framerate) {
        Preconditions.checkArgument(framerate >= 1, "Invalid framerate: " + framerate);

        this.scaling = scaling;
        this.framerate = framerate;
        this.callbacks = new ArrayList<>();
        this.stats = new RenderStats();

        registerCallback(stats);
    }

    /**
     * Performs a frame update. This will result in one or multiple frame updates
     * followed by rendering a frame, as described in the class documentation.
     */
    protected void performFrameUpdate() {
        if (timer == null) {
            timer = new Stopwatch();
            accumulatedTime = 0f;
        }

        float frameTime = getFrameTime();
        float deltaTime = timer.tick() / 1000f;

        for (RenderCallback callback : callbacks) {
            callback.onFrame(frameTime, getInputDevice());
        }

        for (RenderCallback callback : callbacks) {
            callback.onRender(this);
        }

        syncFrames(frameTime);
    }

    /**
     * Synchronizes this frame to make the animation loop run as close to the
     * targeted framerate as possible.
     */
    private void syncFrames(float frameTime) {
        if (shouldSyncFrames()) {
            float sleepTime = frameTime - timer.tock() / 1000f;
            long sleepTimeInMilliseconds = Math.round(sleepTime * 1000f);
            sleepTimeInMilliseconds = Math.max(sleepTimeInMilliseconds, 1L);

            try {
                Thread.sleep(sleepTimeInMilliseconds);
            } catch (InterruptedException e) {
                LOGGER.warning("Frame sync interrupted");
            }
        }
    }

    /**
     * If this method returns true, the renderer is responsible for keeping
     * frame updates synchronized with the targeted framerate. If so, the
     * renderer might intentionally slow down frame updates by sleeping.
     * <p>
     * If this method returns false, the underlying platform is responsible
     * for synchronizing frames, and in these cases the renderer will still
     * adapt the number of frame updates versus the number of frame renders,
     * but it will not sleep to slow down the animation loop.
     */
    protected abstract boolean shouldSyncFrames();

    @Override
    public int getCanvasWidth() {
        return scaling.getCanvasWidth(getScreenBounds());
    }

    @Override
    public int getCanvasHeight() {
        return scaling.getCanvasHeight(getScreenBounds());
    }

    protected abstract Rect getScreenBounds();

    /**
     * Implements drawing text by drawing each of the characters as an image
     * using the specified bitmap font.
     */
    @Override
    public void drawText(String text, BitmapFont font, int x, int y) {
        int currentX = x;
        int currentY = y - font.getBaseline();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            Image glyph = font.getGlyph(c);
            Rect glyphBounds = font.getGlyphBounds(c);
            drawImage(glyph, currentX + glyphBounds.getWidth() / 2,
                    currentY + glyphBounds.getHeight() / 2, null);

            currentX += glyphBounds.getWidth() + font.getLetterSpacing();
        }
    }

    @Override
    public ScaleStrategy getScaleStrategy() {
        return scaling;
    }

    @Override
    public int getTargetFramerate() {
        return framerate;
    }

    public float getFrameTime() {
        return 1f / framerate;
    }

    protected abstract InputDevice getInputDevice();

    @Override
    public RenderStats getStats() {
        return stats;
    }

    @Override
    public void registerCallback(RenderCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void unregisterCallback(RenderCallback callback) {
        callbacks.remove(callback);
    }
}
