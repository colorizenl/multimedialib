//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.scene.EventSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StandardNetworkTest {

    @Test
    void headerNamesAreCaseInsensitive() {
        StandardNetwork internetAccess = new StandardNetwork();
        EventSource<Network.Response> eventQueue = internetAccess.get("https://clrz.nl");
        Network.Response response = eventQueue.block(10).orElseThrow();

        assertTrue(response.body().contains("<title>Colorize"));
        assertEquals("text/html", response.getHeader("Content-Type").orElse(""));
        assertEquals("text/html", response.getHeader("content-type").orElse(""));
    }
}
