//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import lombok.Data;
import nl.colorize.util.swing.ApplicationMenuListener;

/**
 * Configuration options for when the renderer is displayed in a window when
 * running on a desktop platform.
 * <p>
 * When the window is set to "embedded mode", it is assumes the multimedia
 * application is embedded within a regular desktop application. Note that not
 * all renderers might support this mode.
 */
@Data
public class WindowOptions {

    private String title;
    private FilePointer iconFile;
    private ApplicationMenuListener appMenuListener;
    private boolean fullscreen;
    private boolean embedded;

    private static final FilePointer DEFAULT_ICON = new FilePointer("colorize-icon-32.png");

    public WindowOptions(String title) {
        this.title = title;
        this.iconFile = DEFAULT_ICON;
        this.appMenuListener = null;
        this.fullscreen = false;
        this.embedded = false;
    }
}
