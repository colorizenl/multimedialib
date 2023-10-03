//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.util.Subscribable;
import nl.colorize.util.http.Headers;
import nl.colorize.util.http.PostData;
import nl.colorize.util.http.URLResponse;

/**
 * Interface for the platform-specific mechanism for network access. HTTP
 * requests are sent asynchronously to avoid blocking the application.
 */
public interface Network {

    public Subscribable<URLResponse> get(String url, Headers headers);

    public Subscribable<URLResponse> post(String url, Headers headers, PostData body);

    /**
     * Returns true if the application is running in the local development
     * environment. The definition of what such an environment entails depends
     * on the renderer and platform.
     */
    public boolean isDevelopmentEnvironment();
}
