//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.RenderConfig;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.multimedialib.renderer.teavm.Browser;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.util.LogHelper;

import java.util.logging.Logger;

import static nl.colorize.multimedialib.renderer.GraphicsMode.MODE_2D;
import static nl.colorize.multimedialib.renderer.GraphicsMode.MODE_3D;
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

        String demoMode = getDemoMode();
        GraphicsMode graphicsMode = demoMode.equals("3d") ? MODE_3D : MODE_2D;
        Canvas canvas = new Canvas(DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT, ScaleStrategy.balanced());

        String defaultRenderer = graphicsMode == MODE_3D ? "three" : "canvas";
        String rendererName = Browser.getBrowserBridge().getQueryParameter("renderer", defaultRenderer);
        LOGGER.info("Renderer: " + rendererName);

        RenderConfig config = RenderConfig.forBrowser(rendererName, graphicsMode, canvas);
        config.setFramerate(BROWSER_FRAMERATE);
        config.setErrorHandler(TeaDemoLauncher::logError);

        switch (demoMode) {
            case "2d" -> config.start(new Demo2D());
            case "3d" -> config.start(new Demo3D());
            case "isometric" -> config.start(new DemoIsometric());
            default -> throw new UnsupportedOperationException();
        }
    }

    private static String getDemoMode() {
        String defaultDemoMode = Browser.getBrowserBridge().getMeta("demo", "2d");
        return Browser.getBrowserBridge().getQueryParameter("demo", defaultDemoMode);
    }

    private static void logError(SceneContext context, Exception cause) {
        LOGGER.info("----");
        LOGGER.info(cause.getMessage());
        LOGGER.info("----");
    }
}
