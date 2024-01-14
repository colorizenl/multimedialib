//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.util.swing.ApplicationMenuListener;

/**
 * Window options that are used by the renderer when starting applications on
 * desktop platforms. Note that some options will only be used on certain
 * platforms.
 * <p>
 * When the window is set to "embedded mode", it is assumed the MultimediaLib
 * application is embedded within a regular desktop application. Some renderers
 * will use special behavior when started in this mode.
 */
@Getter
@Setter
public class WindowOptions {

    private String title;
    private FilePointer iconFile;
    private boolean fullscreen;
    private ApplicationMenuListener appMenu;
    private boolean embedded;

    public static final FilePointer DEFAULT_ICON = new FilePointer("colorize-icon-32.png");

    public WindowOptions(String title, FilePointer iconFile, boolean fullscreen) {
        this.title = title;
        this.iconFile = iconFile;
        this.fullscreen = fullscreen;
        this.appMenu = null;
        this.embedded = false;
    }

    public WindowOptions(String title) {
        this(title, DEFAULT_ICON, false);
    }
}
