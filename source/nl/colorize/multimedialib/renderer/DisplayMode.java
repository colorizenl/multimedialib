//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.base.Preconditions;

/**
 * Renderer configuration on how graphics should be displayed, covering both
 * screen/window size and resolution and framerate.
 */
public record DisplayMode(Canvas canvas, int framerate) {

    public DisplayMode {
        Preconditions.checkArgument(framerate >= 10,
            "Invalid framerate: " + framerate);
    }
}
