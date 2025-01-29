//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Size;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.swing.ApplicationMenuListener;

import java.util.Optional;

/**
 * Defines how the application window should be displayed on desktop platforms.
 * Mobile platforms and browsers do not allow applications to modify the window
 * appearance at runtime, so the renderer will ignore these options when
 * running on those platforms.
 * <p>
 * If the window size is not explicitly defined, it will be based on the size
 * of the application {@link Canvas}. If the application window is set to
 * fullscreen, this takes precedence over both the explicit window size and
 * the canvas size.
 * <p>
 * When the window is set to "embedded mode", it is assumed the MultimediaLib
 * application is embedded within another desktop application.
 */
@Getter
@Setter
public class WindowOptions {

    private String title;
    private ResourceFile iconFile;
    private boolean fullscreen;
    private Size windowSize;
    private ApplicationMenuListener appMenu;
    private boolean embedded;

    private static final String DEFAULT_WINDOW_TITLE = "MultimediaLib";
    private static final ResourceFile DEFAULT_ICON = new ResourceFile("colorize-icon-32.png");

    protected WindowOptions() {
        this.title = DEFAULT_WINDOW_TITLE;
        this.iconFile = DEFAULT_ICON;
        this.fullscreen = false;
        this.windowSize = null;
        this.appMenu = null;
        this.embedded = false;
    }

    /**
     * Returns the requested window size. If the optional is empty, the
     * renderer will determine the window size considering both the
     * application canvas and the screen size. If the optional is present,
     * the renderer will base the window size directly on the returned value.
     */
    public Optional<Size> getWindowSize() {
        return Optional.ofNullable(windowSize);
    }
}
