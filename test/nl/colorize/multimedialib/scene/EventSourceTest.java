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

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventSourceTest {

    @Test
    void handleEventsDuringFrameUpdates() throws InterruptedException {
        List<String> events = new ArrayList<>();
        List<Throwable> errors = new ArrayList<>();

        Subject<String> subject = Subject.runAsync(() -> {
            Thread.sleep(100);
            return "1";
        });

        EventSource<String> eventSource = EventSource.from(subject);
        HeadlessRenderer renderer = new HeadlessRenderer();
        renderer.attach(eventSource).subscribe(events::add, errors::add);

        Thread.sleep(3000);

        assertEquals(0, events.size());
        renderer.doFrame();
        assertEquals(1, events.size());
        assertEquals(0, errors.size());
    }

    @Test
    void handleErrors() throws InterruptedException {
        List<String> events = new ArrayList<>();
        List<Throwable> errors = new ArrayList<>();

        Subject<String> subject = Subject.runAsync(() -> {
            Thread.sleep(100);
            throw new RuntimeException();
        });

        EventSource<String> eventSource = EventSource.from(subject);
        HeadlessRenderer renderer = new HeadlessRenderer();
        renderer.attach(eventSource).subscribe(events::add, errors::add);

        Thread.sleep(3000);

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
}
