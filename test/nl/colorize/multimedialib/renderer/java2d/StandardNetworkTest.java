//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import nl.colorize.multimedialib.renderer.Response;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import nl.colorize.util.EventQueue;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StandardNetworkTest {

    @Test
    public void testSendGetRequest() {
        List<String> responses = new CopyOnWriteArrayList<>();
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        StandardNetwork internetAccess = new StandardNetwork();
        EventQueue<Response> eventQueue = internetAccess.get("https://clrz.nl");

        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            HeadlessRenderer renderer = new HeadlessRenderer();
            renderer.attach(eventQueue, response -> responses.add(response.getBody()), errors::add);

            while (responses.isEmpty()) {
                renderer.doFrame();
                Thread.sleep(1000);
            }

            assertEquals(1, responses.size());
            assertTrue(responses.getFirst().contains("<title>Colorize"));
            assertEquals(0, errors.size());
        });
    }
}
