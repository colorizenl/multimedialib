//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.collect.ImmutableList;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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

    /**
     * Returns all elements that were pushed to the queue since the last time
     * this method was called, then clears the queue. This method exists to
     * support frame-by-frame polling.
     */
    public Iterable<E> flush() {
        //TODO cannot use List.copyOf() until the next version
        //     of TeaVM is released.
        List<E> buffer = ImmutableList.copyOf(contents);
        contents.clear();
        return buffer;
    }
}
