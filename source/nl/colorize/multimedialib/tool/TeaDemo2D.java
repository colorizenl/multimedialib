//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import nl.colorize.multimedialib.demo.Demo2D;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.teavm.Browser;
import nl.colorize.multimedialib.scene.MultimediaAppLauncher;
import nl.colorize.util.Platform;

/**
 * Launcher for the TeaVM version of the demo application. This class will be
 * transpiled to JavaScript and then called from the browser.
 */
public class TeaDemo2D {

    public static void main(String[] args) {
        Platform.enableTeaVM();

        Browser.log("MultimediaLib - TeaVM Demo");
        Browser.log("Screen size: " + Browser.getScreenWidth() + "x" + Browser.getScreenHeight());
        Browser.log("Page size: " + Math.round(Browser.getPageWidth()) + "x" +
            Math.round(Browser.getPageHeight()));

        Canvas canvas = Canvas.zoomBalanced(Demo2D.DEFAULT_CANVAS_WIDTH, Demo2D.DEFAULT_CANVAS_HEIGHT);

        MultimediaAppLauncher.create(canvas)
            .withFramerate(60)
            .startTea(new Demo2D());
    }
}
