//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.util.ResourceFile;
import nl.colorize.util.swing.ApplicationMenuListener;

/**
 * Configuration options for when the renderer is displayed in a window when
 * running on a desktop platform.
 */
public class WindowOptions {

    private String title;
    private ResourceFile iconFile;
    private ApplicationMenuListener appMenuListener;

    public WindowOptions(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public ResourceFile getIconFile() {
        return iconFile;
    }

    public void setIconFile(ResourceFile iconFile) {
        this.iconFile = iconFile;
    }

    public boolean hasIcon() {
        return iconFile != null;
    }

    public ApplicationMenuListener getAppMenuListener() {
        return appMenuListener;
    }

    public void setAppMenuListener(ApplicationMenuListener appMenuListener) {
        this.appMenuListener = appMenuListener;
    }
}
