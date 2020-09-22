//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.teavm.TeaRenderer;
import nl.colorize.multimedialib.scene.Application;
import nl.colorize.util.Platform;

/**
 * Entry point for transpiling the 3D graphics demo to JavaScript using TeaVM.
 */
public class TeaDemo3D {

    public static void main(String[] args) {
        Platform.enableTeaVM();

        Canvas canvas = Canvas.flexible(Demo3D.CANVAS_WIDTH, Demo3D.CANVAS_HEIGHT);
        TeaRenderer renderer = new TeaRenderer(canvas);
        Application.start(renderer, new Demo3D());
    }
}
