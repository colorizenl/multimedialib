//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.demo;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.pixi.PixiGraphics;
import nl.colorize.multimedialib.renderer.teavm.Browser;
import nl.colorize.multimedialib.renderer.teavm.HtmlCanvasGraphics;
import nl.colorize.multimedialib.renderer.teavm.TeaGraphics;
import nl.colorize.multimedialib.renderer.teavm.TeaRenderer;
import nl.colorize.multimedialib.renderer.three.ThreeGraphics;
import nl.colorize.multimedialib.scene.SceneContext;

/**
 * Launcher for the TeaVM version of the demo application. This class will be
 * transpiled to JavaScript and then called from the browser.
 */
public class TeaDemo2D {

    private static final int BROWSER_FRAMERATE = 60;

    public static void main(String[] args) {
        Browser.log("MultimediaLib - TeaVM Demo");
        Browser.log("Screen size: " + Browser.getScreenWidth() + "x" + Browser.getScreenHeight());
        Browser.log("Page size: " + Math.round(Browser.getPageWidth()) + "x" +
            Math.round(Browser.getPageHeight()));

        Canvas canvas = Canvas.forSize(Demo2D.DEFAULT_CANVAS_WIDTH, Demo2D.DEFAULT_CANVAS_HEIGHT);
        DisplayMode displayMode = new DisplayMode(canvas, BROWSER_FRAMERATE);

        Renderer renderer = new TeaRenderer(displayMode, initGraphics(canvas));
        renderer.start(new Demo2D(), TeaDemo2D::logError);
    }

    private static TeaGraphics initGraphics(Canvas canvas) {
        if (Browser.getPageQueryString().contains("pixi")) {
            return new PixiGraphics(canvas);
        } else if (Browser.getPageQueryString().contains("three")) {
            return new ThreeGraphics(canvas);
        } else {
            return new HtmlCanvasGraphics(canvas);
        }
    }

    private static void logError(SceneContext context, Exception cause) {
        Browser.log("----");
        Browser.log(cause.getMessage());
        Browser.log("----");
    }
}
