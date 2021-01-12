//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;

/**
 * Stores time series values in a rotating buffer. If the buffer's capacity is
 * exceeded the oldest values are replaced.
 */
public class RotatingBuffer {

    private float[] values;
    private int index;
    private int filled;
    private float outlierThreshold;

    public RotatingBuffer(int capacity, float outlierThreshold) {
        Preconditions.checkArgument(capacity >= 1, "Invalid capacity: " + capacity);

        this.values = new float[capacity];
        this.index = 0;
        this.filled = 0;
        this.outlierThreshold = outlierThreshold;
    }

    public RotatingBuffer(int capacity) {
        this(capacity, Float.MAX_VALUE);
    }

    public void add(float value) {
        if (value >= outlierThreshold) {
            return;
        }

        values[index] = value;

        index++;
        if (index >= values.length) {
            index = 0;
        }

        if (filled < values.length) {
            filled++;
        }
    }

    public float getAverageValue() {
        float total = 0f;
        int count = 0;

        for (int i = 0; i < filled; i++) {
            total += values[i];
            count++;
        }

        if (count > 0) {
            return total / count;
        } else {
            return 0f;
        }
    }
}
