//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import nl.colorize.multimedialib.demo.Demo3D;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.scene.MultimediaAppLauncher;

/**
 * Entry point for transpiling the 3D graphics demo to JavaScript using TeaVM.
 */
public class TeaDemo3D {

    public static void main(String[] args) {
        Canvas canvas = Canvas.flexible(Demo3D.CANVAS_WIDTH, Demo3D.CANVAS_HEIGHT);

        MultimediaAppLauncher.create(canvas).startTea(new Demo3D());
    }
}
