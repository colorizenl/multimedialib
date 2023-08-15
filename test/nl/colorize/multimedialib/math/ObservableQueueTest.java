//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ObservableQueueTest {

    @Test
    void flush() {
        ObservableQueue<String> buffer = new ObservableQueue<>();
        buffer.push("a");
        buffer.push("b");
        buffer.push("c");

        assertEquals("[a, b, c]", Iterables.toString(buffer.flush()));
    }

    @Test
    void resetAfterFlushing() {
        ObservableQueue<String> buffer = new ObservableQueue<>();
        buffer.flush();
        buffer.push("c");
        buffer.push("d");

        assertEquals("[c, d]", Iterables.toString(buffer.flush()));
    }

    @Test
    void observeChanges() {
        List<String> observed = new ArrayList<>();

        ObservableQueue<String> buffer = new ObservableQueue<>();
        buffer.subscribe(observed::add);
        buffer.push("a");
        buffer.flush();
        buffer.push("b");

        assertEquals(List.of("a", "b"), observed);
    }
}
