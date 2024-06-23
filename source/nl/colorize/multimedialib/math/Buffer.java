//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import nl.colorize.util.Subscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Data structure that can be flushed to obtain values that have accumulated
 * over time. This is suitable for events that should be handled during each
 * frame update, but that can occur at any point.
 * <p>
 * The buffer can also be used as a {@link Subscriber}, where events
 * accumulate in the buffer until it is flushed. This is useful in situations
 * where asynchronous events are performed on a different thread, but the
 * results need to be processed during the animation loop.
 *
 * @param <E> The type of element that is stored in the queue.
 */
public class Buffer<E> implements Subscriber<E> {

    private List<E> contents;

    public Buffer() {
        this.contents = new ArrayList<>();
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

    public void remove(E element) {
        contents.remove(element);
    }

    /**
     * Returns a stream containing all elements within this buffer, clearing
     * the buffer in the process. This allows for polling the buffer during
     * frame updates.
     */
    public Iterable<E> flush() {
        List<E> buffer = List.copyOf(contents);
        contents.clear();
        return buffer;
    }

    /**
     * Returns a stream containing all elements within this buffer, clearing
     * the buffer in the process. This allows for polling the buffer during
     * frame updates.
     */
    public Stream<E> flushStream() {
        return ((List<E>) flush()).stream();
    }

    /**
     * Removes all contents from this buffer. This method is the same as
     * calling {@link #flush()} and ignoring the result.
     */
    public void clear() {
        contents.clear();
    }

    @Override
    public void onEvent(E event) {
        push(event);
    }

    @Override
    public String toString() {
        return contents.toString();
    }
}
