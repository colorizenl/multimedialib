//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.util.swing.ApplicationMenuListener;

/**
 * Configuration options for when the renderer is displayed in a window when
 * running on a desktop platform.
 */
public class WindowOptions {

    private String title;
    private FilePointer iconFile;
    private boolean fullscreen;
    private ApplicationMenuListener appMenuListener;

    public WindowOptions(String title, FilePointer iconFile) {
        this.title = title;
        this.iconFile = iconFile;
        this.fullscreen = false;
        this.appMenuListener = null;
    }

    public WindowOptions(String title) {
        this(title, null);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public FilePointer getIconFile() {
        return iconFile;
    }

    public void setIconFile(FilePointer iconFile) {
        this.iconFile = iconFile;
    }

    public boolean hasIcon() {
        return iconFile != null;
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public ApplicationMenuListener getAppMenuListener() {
        return appMenuListener;
    }

    public void setAppMenuListener(ApplicationMenuListener appMenuListener) {
        this.appMenuListener = appMenuListener;
    }
}
