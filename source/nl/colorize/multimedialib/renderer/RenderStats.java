//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.util.Stopwatch;

/**
 * Callback that tracks renderer statistics over time. These statistics can then
 * be used for performance monitoring.
 */
public class RenderStats implements RenderCallback {

    private Stopwatch fpsTimer;
    private float[] fpsSamples;
    private int fpsSampleIndex;

    private Stopwatch upsTimer;
    private float[] upsSamples;
    private int upsSampleIndex;

    private static final int NUM_SAMPLES = 100;
    private static final float EPSILON = 0.001f;

    public RenderStats() {
        fpsTimer = new Stopwatch();
        fpsSamples = new float[NUM_SAMPLES];
        fpsSampleIndex = 0;

        upsTimer = new Stopwatch();
        upsSamples = new float[NUM_SAMPLES];
        upsSampleIndex = 0;
    }

    @Override
    public void onFrame(float deltaTime, InputDevice input) {
        upsSamples[upsSampleIndex] = upsTimer.tick() / 1000f;
        upsSampleIndex = (upsSampleIndex + 1) % NUM_SAMPLES;
    }

    @Override
    public void onRender(RenderContext context) {
        fpsSamples[fpsSampleIndex] = fpsTimer.tick() / 1000f;
        fpsSampleIndex = (fpsSampleIndex + 1) % NUM_SAMPLES;
    }

    public float getAverageFPS() {
        return 1f / calculateAverage(fpsSamples);
    }

    public float getAverageUPS() {
        return 1f / calculateAverage(upsSamples);
    }

    private float calculateAverage(float[] values) {
        float total = 0f;
        int count = 0;

        for (float value : values) {
            if (value >= EPSILON) {
                total += value;
                count++;
            }
        }

        if (count > 0) {
            return total / count;
        } else {
            return 0f;
        }
    }
}
