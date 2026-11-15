//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import nl.colorize.util.Subject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class EventSourceTest {

    @Test
    void handleEventsDuringFrameUpdates() {
        AtomicInteger count = new AtomicInteger(0);
        List<String> events = new ArrayList<>();
        List<Throwable> errors = new ArrayList<>();

        Subject<String> subject = Subject.runAsync(() -> {
            count.incrementAndGet();
            return String.valueOf(count.get());
        });

        EventSource<String> eventSource = EventSource.from(subject);
        HeadlessRenderer renderer = new HeadlessRenderer();
        renderer.attach(eventSource).subscribe(events::add, errors::add);

        assertEquals(0, events.size());

        renderer.doFrame();

        assertEquals(1, events.size());
        assertEquals(0, errors.size());
    }

    @Test
    void handleErrors() {
        AtomicInteger count = new AtomicInteger(0);
        List<String> events = new ArrayList<>();
        List<Throwable> errors = new ArrayList<>();

        Subject<String> subject = Subject.runAsync(() -> {
            if (count.incrementAndGet() < 2) {
                throw new RuntimeException();
            }
            return String.valueOf(count.get());
        });

        EventSource<String> eventSource = EventSource.from(subject);
        HeadlessRenderer renderer = new HeadlessRenderer();
        renderer.attach(eventSource).subscribe(events::add, errors::add);

        assertEquals(0, events.size());
        assertEquals(0, errors.size());

        renderer.doFrame();

        assertEquals(0, events.size());
        assertEquals(1, errors.size());
    }

    @Test
    void poll() {
        Subject<String> subject = Subject.of("a", "b");
        EventSource<String> eventSource = EventSource.from(subject);
        List<String> events = new ArrayList<>();

        eventSource.poll(events::add, _ -> {});
        assertEquals(List.of("a", "b"), events);

        eventSource.poll(events::add, _ -> {});
        assertEquals(List.of("a", "b"), events);

        subject.next("c");
        eventSource.poll(events::add, _ -> {});
        assertEquals(List.of("a", "b", "c"), events);
    }

    @Test
    void blockUntilEvent() {
        Subject<Integer> subject = new Subject<>();
        EventSource<Integer> eventSource = EventSource.from(subject);

        Thread backgroundThread = new Thread(() -> {
            try {
                Thread.sleep(100);
                subject.next(1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        backgroundThread.start();

        assertEquals(1, eventSource.block(0.5).orElse(-1));
    }

    @Test
    void emptyResultIfBlockTimeout() {
        Subject<Integer> subject = new Subject<>();
        EventSource<Integer> eventSource = EventSource.from(subject);

        Thread backgroundThread = new Thread(() -> {
            try {
                Thread.sleep(500);
                subject.next(1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        backgroundThread.start();

        assertFalse(eventSource.block(0.1).isPresent());
    }

    @Test
    void mapEvents() {
        AtomicInteger count = new AtomicInteger(0);
        List<String> events = new ArrayList<>();
        List<Throwable> errors = new ArrayList<>();

        Subject<String> subject = Subject.run(() -> {
            count.incrementAndGet();
            return String.valueOf(count.get());
        });

        EventSource<String> eventSource = EventSource.from(subject)
            .map(x -> x + "b");
        HeadlessRenderer renderer = new HeadlessRenderer();
        renderer.attach(eventSource).subscribe(events::add, errors::add);

        assertEquals(List.of(), events);

        renderer.doFrame();

        assertEquals(List.of("1b"), events);
        assertEquals(0, errors.size());
    }

    @Test
    void flatmapEvents() {
        AtomicInteger count = new AtomicInteger(0);
        List<String> events = new ArrayList<>();
        List<Throwable> errors = new ArrayList<>();

        Subject<String> subject = Subject.run(() -> {
            count.incrementAndGet();
            return String.valueOf(count.get());
        });

        EventSource<String> eventSource = EventSource.from(subject)
            .flatMap(x -> Stream.of(x + "b", x + "c"));
        HeadlessRenderer renderer = new HeadlessRenderer();
        renderer.attach(eventSource).subscribe(events::add, errors::add);

        assertEquals(List.of(), events);

        renderer.doFrame();

        assertEquals(List.of("1b", "1c"), events);
        assertEquals(0, errors.size());
    }

    @Test
    void filterEvents() {
        List<String> events = new ArrayList<>();
        List<Throwable> errors = new ArrayList<>();

        Subject<String> subject = Subject.of("1", "2", "3");
        EventSource<String> eventSource = EventSource.from(subject).filter(x -> x.startsWith("2"));
        HeadlessRenderer renderer = new HeadlessRenderer();
        renderer.attach(eventSource).subscribe(events::add, errors::add);

        assertEquals(List.of(), events);

        renderer.doFrame();
        renderer.doFrame();

        assertEquals(List.of("2"), events);
        assertEquals(0, errors.size());
    }
}
