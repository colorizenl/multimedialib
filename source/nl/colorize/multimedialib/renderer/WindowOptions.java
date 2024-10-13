//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Size;
import nl.colorize.util.swing.ApplicationMenuListener;

import java.util.Optional;

/**
 * Defines the window appearance on desktop platforms. The interpretation of
 * these options depends on both the platform and the renderer.
 * <p>
 * The window size is defined in terms of <em>logical</em> size. This might
 * be different from its <em>physical</em> size, depending on the device
 * pixel ratio. Do not that {@link #isFullscreen()} takes precedence over
 * window size: If the application is fullscreen, it will always fill the
 * screen regardless of the requested window size. If the window size is
 * not explicitly defined, it will be defined based on the application's
 * display mode.
 * <p>
 * When the window is set to "embedded mode", it is assumed the
 * MultimediaLib application is embedded within a regular desktop
 * application. Some renderers will use special behavior when started in
 * this mode.
 */
@Getter
@Setter
public class WindowOptions {

    private String title;
    private FilePointer iconFile;
    private boolean fullscreen;
    private Size windowSize;
    private ApplicationMenuListener appMenu;
    private boolean embedded;

    private static final FilePointer DEFAULT_ICON = new FilePointer("colorize-icon-32.png");

    public WindowOptions(String title, FilePointer iconFile, boolean fullscreen) {
        this.title = title;
        this.iconFile = iconFile;
        this.fullscreen = fullscreen;
        this.windowSize = null;
        this.appMenu = null;
        this.embedded = false;
    }

    public WindowOptions(String title) {
        this(title, DEFAULT_ICON, false);
    }

    public Optional<Size> getWindowSize() {
        return Optional.ofNullable(windowSize);
    }
}
