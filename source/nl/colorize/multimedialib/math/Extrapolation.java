//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;
import com.google.common.math.Stats;

import java.util.ArrayList;
import java.util.List;

/**
 * Linear extrapolation based on a series of numerical data points. Extrapolation
 * is based on a rolling window, meaning all values are kept in a buffer, and
 * when the buffer is full the oldest values are removed.
 */
public class Extrapolation {

    private List<Float> buffer;
    private int bufferSize;

    public Extrapolation(int bufferSize) {
        Preconditions.checkArgument(bufferSize >= 1,
            "Invalid buffer size: " + bufferSize);

        this.buffer = new ArrayList<>();
        this.bufferSize = bufferSize;
    }

    public void add(float value) {
        buffer.add(value);

        if (buffer.size() > bufferSize) {
            buffer = buffer.subList(buffer.size() - bufferSize, buffer.size());
        }
    }

    public void reset() {
        buffer.clear();
    }

    public float extrapolate() {
        if (buffer.isEmpty()) {
            return 0f;
        }

        if (buffer.size() == 1) {
            return buffer.get(0);
        }

        List<Float> deltas = new ArrayList<>();
        for (int i = 1; i < buffer.size(); i++) {
            deltas.add(buffer.get(i) - buffer.get(i - 1));
        }

        float averageDelta = (float) Stats.meanOf(deltas);
        return buffer.get(buffer.size() - 1)  +averageDelta;
    }
}
