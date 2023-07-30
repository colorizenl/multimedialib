//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Data structure that accumulates elements until it is flushed. Afterward,
 * the buffer is cleared and starts to accumulate new elements until the next
 * time it is flushed. Elements are flushed in the same order they were added.
 *
 * @param <E> The type of element that is stored in the buffer.
 */
public class Buffer<E> {

    private List<E> elements;

    public Buffer() {
        this.elements = new ArrayList<>();
    }

    public void push(E element) {
        elements.add(element);
    }

    public Iterable<E> flush() {
        List<E> flushed = List.copyOf(elements);
        elements.clear();
        return flushed;
    }

    public void flush(Consumer<E> callback) {
        elements.forEach(callback);
        elements.clear();
    }

    public void clear() {
        elements.clear();
    }

    @Override
    public String toString() {
        return elements.toString();
    }
}
