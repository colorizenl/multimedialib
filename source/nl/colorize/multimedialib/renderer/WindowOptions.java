//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.util.swing.ApplicationMenuListener;

/**
 * Configuration options for when the renderer is displayed in a window when
 * running on a desktop platform.
 * <p>
 * When the window is set to "embedded mode", it is assumes the multimedia
 * application is embedded within a regular desktop application. Note that not
 * all renderers might support this mode.
 */
public record WindowOptions(
    String title,
    FilePointer iconFile,
    boolean fullscreen,
    ApplicationMenuListener appMenuListener,
    boolean embedded
) {

    private static final FilePointer DEFAULT_ICON = new FilePointer("colorize-icon-32.png");

    public WindowOptions(String title) {
        this(title, DEFAULT_ICON, false, null, false);
    }

    public WindowOptions(String title, FilePointer iconFile, ApplicationMenuListener appMenu) {
        this(title, iconFile, false, appMenu, false);
    }

    public WindowOptions(String title, ApplicationMenuListener appMenu) {
        this(title, DEFAULT_ICON, false, appMenu, false);
    }
}
