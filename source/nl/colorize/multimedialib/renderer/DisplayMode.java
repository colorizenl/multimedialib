//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Renderer configuration on how graphics should be displayed, covering both
 * screen/window size and resolution and framerate.
 */
public class DisplayMode {

    private Canvas canvas;
    private int framerate;

    private static final List<Integer> SUPPORTED_FRAME_RATES = ImmutableList.of(20, 25, 30, 60, 120);

    public DisplayMode(Canvas canvas, int framerate) {
        Preconditions.checkArgument(SUPPORTED_FRAME_RATES.contains(framerate),
            "Invalid framerate: " + framerate);

        this.canvas = canvas;
        this.framerate = framerate;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public int getFramerate() {
        return framerate;
    }
}
