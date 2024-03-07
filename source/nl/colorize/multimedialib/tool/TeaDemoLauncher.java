//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.RendererLauncher;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.multimedialib.renderer.teavm.Browser;
import nl.colorize.multimedialib.renderer.teavm.BrowserDOM;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;

import static nl.colorize.multimedialib.tool.Demo2D.DEFAULT_CANVAS_HEIGHT;
import static nl.colorize.multimedialib.tool.Demo2D.DEFAULT_CANVAS_WIDTH;

/**
 * Launcher for the TeaVM version of the demo application. This class is
 * transpiled to JavaScript and then used as the entry point by TeaVM when
 * running the demo from a browser.
 */
public class TeaDemoLauncher {

    private static final int BROWSER_FRAMERATE = 60;

    public static void main(String[] args) {
        Browser.log("MultimediaLib - TeaVM Demo");
        Browser.log("Screen size: " + Browser.getScreenWidth() + "x" + Browser.getScreenHeight());
        Browser.log("Page size: " + Math.round(Browser.getPageWidth()) + "x" +
            Math.round(Browser.getPageHeight()));

        Canvas canvas = new Canvas(DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT, ScaleStrategy.balanced());
        DisplayMode displayMode = new DisplayMode(canvas, BROWSER_FRAMERATE);

        String rendererName = BrowserDOM.getQueryString().getOptionalParameter("renderer", "canvas");

        RendererLauncher.configure(displayMode)
            .forBrowser2D(rendererName)
            .start(initDemo(), TeaDemoLauncher::logError);
    }

    private static Scene initDemo() {
        return switch (Browser.getMeta("demo", "2d")) {
            case "2d" -> new Demo2D();
            case "3d" -> new Demo3D();
            default -> throw new UnsupportedOperationException("Unknown demo");
        };
    }

    private static void logError(SceneContext context, Exception cause) {
        Browser.log("----");
        Browser.log(cause.getMessage());
        Browser.log("----");
    }
}
