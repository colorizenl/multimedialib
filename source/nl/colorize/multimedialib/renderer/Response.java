//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.net.HttpHeaders;
import lombok.Getter;
import nl.colorize.util.stats.Tuple;
import nl.colorize.util.stats.TupleList;

import java.util.List;
import java.util.Optional;

/**
 * Representation of an HTTP response received after sending requests using
 * {@link Network}.
 * <p>
 * MultimediaLib cannot use the standard HTTP client in {@code java.net.http},
 * as this is not yet available on all platforms supported by MultimediaLib.
 *
 * @see Network
 */
@Getter
public class Response {

    private int status;
    private TupleList<String, String> headers;
    private String body;

    public Response(int status, TupleList<String, String> headers, String body) {
        this.status = status;
        this.headers = headers.immutable();
        this.body = body;
    }

    /**
     * Returns the value of the header with the specified name. Since HTTP
     * headers are case-insensitive, so is this method. Returns the first
     * value if the header is present multiple times.
     */
    public Optional<String> getHeader(String name) {
        return headers.stream()
            .filter(header -> header.left().equalsIgnoreCase(name))
            .map(Tuple::right)
            .findFirst();
    }

    /**
     * Returns the values of the header with the specified name. Since HTTP
     * headers are case-insensitive, so is this method.
     */
    public List<String> getHeaderValues(String name) {
        return headers.stream()
            .filter(header -> header.left().equalsIgnoreCase(name))
            .map(Tuple::right)
            .toList();
    }

    public Optional<String> getContentType() {
        return getHeader(HttpHeaders.CONTENT_TYPE);
    }
}
