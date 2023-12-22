//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.demo;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.multimedialib.renderer.teavm.Browser;
import nl.colorize.multimedialib.renderer.teavm.BrowserDOM;
import nl.colorize.multimedialib.renderer.teavm.TeaRenderer;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.util.http.PostData;

import static nl.colorize.multimedialib.demo.Demo2D.DEFAULT_CANVAS_HEIGHT;
import static nl.colorize.multimedialib.demo.Demo2D.DEFAULT_CANVAS_WIDTH;

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

        Canvas canvas = new Canvas(DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT, initScaleStrategy());
        DisplayMode displayMode = new DisplayMode(canvas, BROWSER_FRAMERATE);
        Renderer renderer = initRenderer(displayMode);
        renderer.start(new Demo2D(), TeaDemo2D::logError);
    }

    private static TeaRenderer initRenderer(DisplayMode displayMode) {
        PostData queryString = BrowserDOM.getQueryString();

        if (queryString.contains("webgl")) {
            return TeaRenderer.withWebGL(displayMode);
        } else if (queryString.contains("pixi")) {
            return TeaRenderer.withPixi(displayMode);
        } else if (queryString.contains("three")) {
            return TeaRenderer.withThree(displayMode);
        } else {
            return TeaRenderer.withCanvas(displayMode);
        }
    }

    private static ScaleStrategy initScaleStrategy() {
        PostData queryString = BrowserDOM.getQueryString();

        if (queryString.contains("canvas")) {
            return ScaleStrategy.balanced();
        } else {
            return ScaleStrategy.flexible();
        }
    }

    private static void logError(SceneContext context, Exception cause) {
        Browser.log("----");
        Browser.log(cause.getMessage());
        Browser.log("----");
    }
}
