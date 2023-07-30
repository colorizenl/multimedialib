//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.demo;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.ErrorHandler;
import nl.colorize.multimedialib.renderer.teavm.TeaRenderer;

/**
 * Entry point for transpiling the 3D graphics demo to JavaScript using TeaVM.
 */
public class TeaDemo3D {

    public static void main(String[] args) {
        Canvas canvas = Canvas.scale(Demo3D.CANVAS_WIDTH, Demo3D.CANVAS_HEIGHT);
        DisplayMode displayMode = new DisplayMode(canvas, 60);

        TeaRenderer renderer = TeaRenderer.withThree(displayMode);
        renderer.start(new Demo3D(), ErrorHandler.DEFAULT);
    }
}
