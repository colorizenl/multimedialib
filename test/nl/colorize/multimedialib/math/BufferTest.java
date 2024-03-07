//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BufferTest {

    @Test
    void flush() {
        Buffer<String> buffer = new Buffer<>();
        buffer.push("a");
        buffer.push("b");
        buffer.push("c");

        assertEquals("[a, b, c]", Iterables.toString(buffer.flush()));
    }

    @Test
    void resetAfterFlushing() {
        Buffer<String> buffer = new Buffer<>();
        buffer.flush();
        buffer.push("c");
        buffer.push("d");

        assertEquals("[c, d]", Iterables.toString(buffer.flush()));
    }
}
