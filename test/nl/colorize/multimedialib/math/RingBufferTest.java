//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import static nl.colorize.multimedialib.math.MathUtils.EPSILON;
import static org.junit.jupiter.api.Assertions.*;

class RingBufferTest {

    @Test
    void getLatestValue() {
        RingBuffer buffer = new RingBuffer(3);
        assertEquals(0f, buffer.getLatestValue(), EPSILON);
        buffer.add(2f);
        assertEquals(2f, buffer.getLatestValue(), EPSILON);
        buffer.add(3f);
        buffer.add(4f);
        buffer.add(5f);
        assertEquals(5f, buffer.getLatestValue(), EPSILON);
    }

    @Test
    void getAverageValue() {
        RingBuffer buffer = new RingBuffer(3);
        assertEquals(0f, buffer.getAverageValue(), EPSILON);
        buffer.add(2f);
        assertEquals(2f, buffer.getAverageValue(), EPSILON);
        buffer.add(3f);
        assertEquals(2.5f, buffer.getAverageValue(), EPSILON);
        buffer.add(4f);
        assertEquals(3f, buffer.getAverageValue(), EPSILON);
        buffer.add(5f);
        assertEquals(4f, buffer.getAverageValue(), EPSILON);
    }
}
