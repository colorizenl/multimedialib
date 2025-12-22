//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.util.TupleList;
import nl.colorize.util.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResponseTest {

    @Test
    void headersAreCaseInsensitive() {
        TupleList<String, String> headers = new TupleList<>();
        headers.add("a", "2");
        headers.add("a", "2extra");
        headers.add("b", "3");

        Response response = new Response(HttpStatus.OK, headers, "");

        assertEquals("2", response.getHeader("a").orElse(""));
        assertEquals("2", response.getHeader("A").orElse(""));
        assertEquals("3", response.getHeader("b").orElse(""));
        assertEquals("", response.getHeader("c").orElse(""));
    }

    @Test
    void multipleHeaders() {
        TupleList<String, String> headers = new TupleList<>();
        headers.add("a", "2");
        headers.add("a", "2extra");
        headers.add("b", "3");

        Response response = new Response(HttpStatus.OK, headers, "");

        assertEquals(List.of("2", "2extra"), response.getHeaderValues("a"));
        assertEquals(List.of("2", "2extra"), response.getHeaderValues("A"));
        assertEquals(List.of("3"), response.getHeaderValues("b"));
        assertEquals(List.of(), response.getHeaderValues("c"));
    }
}
