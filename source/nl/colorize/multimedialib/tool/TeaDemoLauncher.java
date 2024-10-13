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
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.util.LogHelper;

import java.util.logging.Logger;

import static nl.colorize.multimedialib.tool.Demo2D.DEFAULT_CANVAS_HEIGHT;
import static nl.colorize.multimedialib.tool.Demo2D.DEFAULT_CANVAS_WIDTH;

/**
 * Launcher for the TeaVM version of the demo application. This class is
 * transpiled to JavaScript and then used as the entry point by TeaVM when
 * running the demo from a browser.
 */
public class TeaDemoLauncher {

    private static final int BROWSER_FRAMERATE = 60;
    private static final Logger LOGGER = LogHelper.getLogger(TeaDemoLauncher.class);

    public static void main(String[] args) {
        LOGGER.info("MultimediaLib - TeaVM Demo");

        String rendererName = Browser.getBrowserBridge().getQueryParameter("renderer", "canvas");
        LOGGER.info("Renderer: " + rendererName);

        Canvas canvas = new Canvas(DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT, ScaleStrategy.balanced());
        DisplayMode displayMode = new DisplayMode(canvas, BROWSER_FRAMERATE);
        Scene demo = initDemo();

        if (demo instanceof Demo3D) {
            RendererLauncher.configure(displayMode)
                .forBrowser3D(rendererName)
                .start(demo, TeaDemoLauncher::logError);
        } else {
            RendererLauncher.configure(displayMode)
                .forBrowser2D(rendererName)
                .start(demo, TeaDemoLauncher::logError);
        }
    }

    private static Scene initDemo() {
        String demo = Browser.getBrowserBridge().getMeta("demo", "2d");

        return switch (demo) {
            case "2d" -> new Demo2D();
            case "3d" -> new Demo3D();
            default -> throw new UnsupportedOperationException("Unknown demo");
        };
    }

    private static void logError(SceneContext context, Exception cause) {
        LOGGER.info("----");
        LOGGER.info(cause.getMessage());
        LOGGER.info("----");
    }
}
