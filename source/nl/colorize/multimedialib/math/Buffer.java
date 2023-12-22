//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

/**
 * Data structure that can be flushed to obtain values that have accumulated
 * over time. This is suitable for events that should be handled during each
 * frame update, but that can occur at any point.
 *
 * @param <E> The type of element that is stored in the queue.
 */
public class Buffer<E> {

    private Queue<E> contents;

    public Buffer() {
        this.contents = new LinkedList<>();
    }

    public void push(E element) {
        contents.add(element);
    }

    public void push(List<E> elements) {
        contents.addAll(elements);
    }

    public void push(E[] elements) {
        for (E element : elements) {
            push(element);
        }
    }

    /**
     * Returns all elements that were pushed to the queue since the last time
     * this method was called, then clears the queue. This method exists to
     * support frame-by-frame polling.
     */
    public Iterable<E> flush() {
        List<E> buffer = List.copyOf(contents);
        contents.clear();
        return buffer;
    }

    /**
     * Returns all elements that were pushed to the queue since the last time
     * this method was called, then clears the queue. Similar to
     * {@link #flush()}, but returns a stream instead of an {@link Iterable}.
     */
    public Stream<E> flushStream() {
        List<E> buffer = List.copyOf(contents);
        contents.clear();
        return buffer.stream();
    }

    /**
     * Peeks at the contents of this buffer without flushing it.
     */
    public Iterable<E> peek() {
        return contents;
    }

    @Override
    public String toString() {
        return contents.toString();
    }
}
