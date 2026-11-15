//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import nl.colorize.util.Development;
import nl.colorize.util.Subject;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

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

    private Subject<T> publisher;
    private List<Object> buffer;
    private Subject<T> output;
    private boolean attached;

    private EventSource(Subject<T> publisher) {
        this.publisher = publisher;
        this.buffer = new CopyOnWriteArrayList<>();
        this.output = new Subject<>();
        this.attached = false;

        publisher.subscribe(buffer::add, buffer::add);
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

    /**
     * Blocks the current thread until the next event has been received from
     * the underlying asynchronous operation. Throws an exception if the
     * duration has been reached before the event is received.
     * <p>
     * <strong>Important note:</strong> This method is only provided for
     * testing purposes; so that tests can cover application logic that
     * requires asynchronous operations. MultimediaLib applications should
     * <em>never</em> block the current thread as this would cause the
     * renderer to freeze.
     *
     * @throws IllegalArgumentException if {@code timeout} is negative or zero.
     *
     * @deprecated Only use this method in test code, for the reasons explained
     *             above.
     */
    @Development
    @Deprecated
    public Optional<T> block(double timeout) {
        Preconditions.checkArgument(timeout > 0.0, "Invalid timeout: " + timeout);
        Preconditions.checkArgument(timeout < 500L, "Unlikely timeout, did you mean milliseconds?");

        // This can build on top of Subject.block() once that is available.
        BlockingQueue<T> queue = new ArrayBlockingQueue<>(1);
        publisher.subscribe(queue::add);
        try {
            T result = queue.poll(Math.round(timeout * 1000), TimeUnit.MILLISECONDS);
            return Optional.ofNullable(result);
        } catch (InterruptedException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns a new {@link EventSource} that performs the specified operation
     * on received events before forwarding them to subscribers.
     *
     * @throws IllegalStateException if this {@link EventSource} has already
     *         been attached to the currently active scene.
     */
    public <S> EventSource<S> map(Function<T, S> mapper) {
        Preconditions.checkState(!attached, "EventSource already attached to scene");
        return new EventSource<>(publisher.map(mapper));
    }

    /**
     * Returns a new {@link EventSource} that performs the specified operation
     * on received events before forwarding them to subscribers.
     *
     * @throws IllegalStateException if this {@link EventSource} has already
     *         been attached to the currently active scene.
     */
    public <S> EventSource<S> flatMap(Function<T, Stream<S>> mapper) {
        Preconditions.checkState(!attached, "EventSource already attached to scene");
        return new EventSource<>(publisher.flatMap(mapper));
    }

    /**
     * Returns a new {@link EventSource} that only forwards events matching
     * the specified predicate to subscribers.
     *
     * @throws IllegalStateException if this {@link EventSource} has already
     *         been attached to the currently active scene.
     */
    public EventSource<T> filter(Predicate<T> condition) {
        Preconditions.checkState(!attached, "EventSource already attached to scene");
        return new EventSource<>(publisher.filter(condition));
    }

    public static <T> EventSource<T> from(Subject<T> publisher) {
        return new EventSource<>(publisher);
    }
}
