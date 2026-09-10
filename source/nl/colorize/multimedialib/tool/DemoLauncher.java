//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.RenderConfig;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.multimedialib.renderer.WindowOptions;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.util.cli.Arg;
import nl.colorize.util.cli.CommandLineArgumentParser;

import java.io.File;

import static nl.colorize.multimedialib.renderer.GraphicsMode.MODE_2D;
import static nl.colorize.multimedialib.renderer.GraphicsMode.MODE_3D;
import static nl.colorize.multimedialib.tool.Demo2D.DEFAULT_CANVAS_HEIGHT;
import static nl.colorize.multimedialib.tool.Demo2D.DEFAULT_CANVAS_WIDTH;

/**
 * Launches one of the demo applications from the command line. The behavior of
 * the demo can be controlled using the command line parameters.
 * <p>
 * Refer to the documentation for {@link Demo2D} for more information
 * on the demo application itself.
 */
public class DemoLauncher {

    @Arg(name = "--renderer", usage = "One of 'java2d', 'gdx', 'regression', 'regression3d'.")
    protected String rendererName;

    @Arg(name = "--demo", usage = "One of '2d', '3d', 'form'.")
    protected String demoMode;

    @Arg(defaultValue = "60", usage = "Framerate, default is 60 fps.")
    protected int framerate;

    @Arg(usage = "Starts the demo fullscreen instead of in a window.")
    protected boolean fullscreen;

    @Arg(name = "--zoom", usage = "Uses a fixed canvas size to display graphics.")
    protected boolean canvasZoom;

    @Arg(name = "--screenshot", required = false, usage = "Saves a screenshot then exits.")
    protected File screenshotFile;

    public static void main(String[] argv) {
        CommandLineArgumentParser argParser = new CommandLineArgumentParser(DemoLauncher.class);
        DemoLauncher launcher = argParser.parse(argv, DemoLauncher.class);
        launcher.start();
    }

    private void start() {
        GraphicsMode graphicsMode = demoMode.endsWith("3d") ? MODE_3D : MODE_2D;
        ScaleStrategy scaleStrategy = canvasZoom ? ScaleStrategy.scale() : ScaleStrategy.flexible();
        Canvas canvas = new Canvas(DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT, scaleStrategy);

        RenderConfig.forDesktop(rendererName, graphicsMode, canvas)
            .withFramerate(framerate)
            .withWindowOptions(new WindowOptions("MultimediaLib - Demo", fullscreen))
            .start(prepareDemo());
    }

    private Scene prepareDemo() {
        return switch (demoMode) {
            case "2d" -> new Demo2D();
            case "3d" -> new Demo3D();
            case "form" -> new FormDemo();
            case "regression" -> new RegressionDemo(MODE_2D, screenshotFile);
            case "regression3d" -> new RegressionDemo(MODE_3D, screenshotFile);
            default -> throw new UnsupportedOperationException();
        };
    }
}
