//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.colorize.multimedialib.math.Size;
import nl.colorize.util.ResourceFile;

import java.util.Optional;

/**
 * Defines how the application window should be displayed on desktop platforms.
 * Mobile platforms and browsers do not allow applications to modify the window
 * appearance at runtime, so the renderer will ignore these options when
 * running on those platforms.
 */
@AllArgsConstructor
@Getter
public class WindowOptions {

    private String title;
    private ResourceFile iconFile;
    private boolean fullscreen;
    private Size windowSize;

    public static final ResourceFile DEFAULT_ICON = new ResourceFile("colorize-icon-32.png");

    public WindowOptions(String title, ResourceFile iconFile, boolean fullscreen) {
        this(title, iconFile, fullscreen, null);
    }

    public WindowOptions(String title, boolean fullscreen) {
        this(title, DEFAULT_ICON, fullscreen, null);
    }

    /**
     * Returns the requested window size. If the window size is not explicitly
     * defined, it will be based on the size of the application {@link Canvas}.
     * If the application window is set to fullscreen, this takes precedence
     * over both the requested window size and the canvas size.
     */
    public Optional<Size> getWindowSize() {
        return Optional.ofNullable(windowSize);
    }
}
