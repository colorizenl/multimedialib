//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.demo;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.teavm.TeaRenderer;
import nl.colorize.multimedialib.renderer.three.ThreeGraphics;
import nl.colorize.multimedialib.renderer.ErrorHandler;

/**
 * Entry point for transpiling the 3D graphics demo to JavaScript using TeaVM.
 */
public class TeaDemo3D {

    public static void main(String[] args) {
        Canvas canvas = Canvas.forSize(Demo3D.CANVAS_WIDTH, Demo3D.CANVAS_HEIGHT);
        DisplayMode displayMode = new DisplayMode(canvas, 60);

        TeaRenderer renderer = new TeaRenderer(displayMode, new ThreeGraphics(canvas));
        renderer.start(new Demo3D(), ErrorHandler.DEFAULT);
    }
}