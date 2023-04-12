//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;

import java.util.LinkedList;

/**
 * Data structure that stores a fixed number of numerical values. Once this
 * capacity is exceeded, the oldest values are removed to make place for the
 * newer values.
 */
public class RingBuffer {

    private LinkedList<Float> values;
    private int capacity;

    public RingBuffer(int capacity) {
        Preconditions.checkArgument(capacity >= 1, "Invalid capacity: " + capacity);

        this.values = new LinkedList<>();
        this.capacity = capacity;
    }

    public void add(float value) {
        if (values.size() >= capacity) {
            values.removeFirst();
        }
        values.add(value);
    }

    public float getLatestValue() {
        return values.isEmpty() ? 0f : values.getLast();
    }

    public float getAverageValue() {
        return MathUtils.average(values);
    }
}
