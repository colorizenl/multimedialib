//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.demo;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.teavm.Browser;
import nl.colorize.multimedialib.renderer.teavm.WebGraphics;
import nl.colorize.multimedialib.scene.ErrorHandler;
import nl.colorize.multimedialib.scene.MultimediaAppLauncher;

import static nl.colorize.multimedialib.renderer.teavm.WebGraphics.CANVAS;
import static nl.colorize.multimedialib.renderer.teavm.WebGraphics.PIXI;

/**
 * Launcher for the TeaVM version of the demo application. This class will be
 * transpiled to JavaScript and then called from the browser.
 */
public class TeaDemo2D {

    public static void main(String[] args) {
        Browser.log("MultimediaLib - TeaVM Demo");
        Browser.log("Screen size: " + Browser.getScreenWidth() + "x" + Browser.getScreenHeight());
        Browser.log("Page size: " + Math.round(Browser.getPageWidth()) + "x" +
            Math.round(Browser.getPageHeight()));

        Canvas canvas = Canvas.zoomBalanced(Demo2D.DEFAULT_CANVAS_WIDTH, Demo2D.DEFAULT_CANVAS_HEIGHT);
        DisplayMode displayMode = new DisplayMode(canvas, 60);
        WebGraphics graphicsMode = Browser.getPageQueryString().contains("pixi") ? PIXI : CANVAS;

        MultimediaAppLauncher.launchTea(displayMode, graphicsMode)
            .start(new Demo2D(), ErrorHandler.DEFAULT);
    }
}
