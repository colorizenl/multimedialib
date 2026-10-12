//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import nl.colorize.util.Subject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Accumulates asynchronous events, then forwards them to subscribers during
 * frame updates. Asynchronous events in MultimediaLib applications should
 * always be processed through this class. This ensures that events are
 * only processed during frame updates. It also prevents scenes that are no
 * longer active from still responding to events.
 *
 * @param <T> The type of event that can be subscribed to.
 *
 * @see SceneContext#attach(EventSource)
 */
public class EventSource<T> implements Actor {

    private List<Object> buffer;
    private Subject<T> output;
    private boolean attached;

    private EventSource() {
        this.buffer = new CopyOnWriteArrayList<>();
        this.output = new Subject<>();
        this.attached = false;
    }

    /**
     * Returns a {@link Subject} that, unlike the original {@link Subject},
     * will only publish events during frame updates. Applications should not
     * interact with this method directly. Use
     * {@link SceneContext#attach(EventSource)} to attach this
     * {@link EventSource} to the currently active scene, which then allows
     * subscribers to receive events.
     */
    protected Subject<T> getOutput() {
        return output;
    }

    @Override
    public void update(double deltaTime) {
        attached = true;
        flush(null, null);
    }

    @SuppressWarnings("unchecked")
    private void flush(Consumer<T> onEvent, Consumer<Throwable> onError) {
        List<Object> snapshot = List.copyOf(buffer);
        buffer.clear();

        for (Object event : snapshot) {
            if (event instanceof Throwable error) {
                output.nextError(error);
                if (onError != null) {
                    onError.accept(error);
                }
            } else {
                output.next((T) event);
                if (onEvent != null) {
                    onEvent.accept((T) event);
                }
            }
        }
    }

    /**
     * Gathers all events that have been received since the previous frame
     * update and forwards them to both registered subscribers and the
     * specified callback methods.
     *
     * @throws IllegalStateException if this {@link EventSource} has already
     *         been attached to the currently active scene.
     *
     * @deprecated Prefer {@link SceneContext#attach(EventSource)}.
     */
    @Deprecated
    public void poll(Consumer<T> onEvent, Consumer<Throwable> onError) {
        Preconditions.checkState(!attached, "EventSource already attached to scene");
        flush(onEvent, onError);
    }

    public static <T> EventSource<T> from(Subject<T> publisher) {
        EventSource<T> eventSource = new EventSource<>();
        publisher.subscribe(eventSource.buffer::add, eventSource.buffer::add);
        return eventSource;
    }
}
