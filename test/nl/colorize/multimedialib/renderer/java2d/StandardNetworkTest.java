//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import nl.colorize.util.http.Headers;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StandardNetworkTest {

    @Test
    public void testSendGetRequest() throws InterruptedException {
        List<String> responses = new ArrayList<>();
        List<Throwable> errors = new ArrayList<>();

        StandardNetwork internetAccess = new StandardNetwork();
        internetAccess.get("https://clrz.nl", Headers.none())
            .subscribe(response -> responses.add(response.readBody()), errors::add);

        Thread.sleep(3000);

        assertEquals(1, responses.size());
        assertTrue(responses.get(0).contains("<title>Colorize"));
        assertEquals(0, errors.size());
    }
}
