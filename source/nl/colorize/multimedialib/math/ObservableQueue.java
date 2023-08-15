//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.collect.ImmutableList;
import nl.colorize.util.Subject;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Data structure that combines "pull" notifications (by polling the queue)
 * with "push" notifications (by registering observers). Neither approach is
 * unequivocally "better" for MultimediaLib applications. Although observers
 * are generally preferable, immediate notification does not alwys combine
 * well with the frame-by-frame of MultimediaLib application. In such
 * situations it is often easier to explicitly poll the queue at the
 * appropriate moment during each frame update.
 *
 * @param <E> The type of element that is stored in the queue.
 */
public class ObservableQueue<E> extends Subject<E> {

    private Queue<E> elements;

    public ObservableQueue() {
        this.elements = new LinkedList<>();
    }

    public void push(E element) {
        elements.add(element);
        next(element);
    }

    /**
     * Returns all elements that were pushed to the queue since the last time
     * this method was called, then clears the queue. This method exists to
     * support frame-by-frame polling.
     */
    public Iterable<E> flush() {
        //TODO cannot use List.copyOf() until the next version
        //     of TeaVM is released.
        List<E> buffer = ImmutableList.copyOf(elements);
        elements.clear();
        return buffer;
    }
}
