//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.demo;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.ErrorHandler;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.multimedialib.renderer.WindowOptions;
import nl.colorize.multimedialib.renderer.java2d.Java2DRenderer;
import nl.colorize.multimedialib.renderer.libgdx.GDXRenderer;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.util.cli.Arg;
import nl.colorize.util.cli.CommandLineArgumentParser;
import nl.colorize.util.swing.ApplicationMenuListener;
import nl.colorize.util.swing.Popups;

import static nl.colorize.multimedialib.demo.Demo2D.DEFAULT_CANVAS_HEIGHT;
import static nl.colorize.multimedialib.demo.Demo2D.DEFAULT_CANVAS_WIDTH;

/**
 * Launches one of the demo applications from the command line. The behavior of
 * the demo can be controlled using the command line parameters.
 * <p>
 * Refer to the documentation for {@link Demo2D} for more information
 * on the demo application itself.
 */
public class DemoLauncher implements ApplicationMenuListener {

    @Arg(name = "-renderer", usage = "Either 'java2d' or 'gdx'")
    protected String rendererName;

    @Arg(usage = "Either '2d' or '3d'")
    protected String graphics;

    @Arg(defaultValue = "60", usage = "Framerate, default is 60 fps")
    protected int framerate;

    @Arg(name = "zoom", usage = "Uses a fixed canvas size to display graphics")
    protected boolean canvasZoom;

    public static void main(String[] argv) {
        CommandLineArgumentParser argParser = new CommandLineArgumentParser(DemoLauncher.class);
        DemoLauncher launcher = argParser.parse(argv, DemoLauncher.class);
        launcher.start();
    }

    private void start() {
        Canvas canvas = initCanvas();
        DisplayMode displayMode = new DisplayMode(canvas, framerate);
        WindowOptions window = new WindowOptions("MultimediaLib - Demo", this);

        Renderer renderer = switch (rendererName) {
            case "java2d" -> new Java2DRenderer(displayMode, window);
            case "libgdx", "gdx" -> new GDXRenderer(getGraphicsMode(), displayMode, window);
            default -> throw new UnsupportedOperationException("Unknown renderer: " + rendererName);
        };

        Scene demo = createDemoScene();
        renderer.start(demo, (ErrorHandler) demo);
    }

    private Canvas initCanvas() {
        ScaleStrategy scaleStrategy = canvasZoom ? ScaleStrategy.scale() : ScaleStrategy.flexible();
        return new Canvas(DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT, scaleStrategy);
    }

    private GraphicsMode getGraphicsMode() {
        return switch (graphics) {
            case "2d" -> GraphicsMode.MODE_2D;
            case "3d" -> GraphicsMode.MODE_3D;
            default -> throw new UnsupportedOperationException("Unknown graphics mode");
        };
    }

    private Scene createDemoScene() {
        return switch (graphics) {
            case "2d" -> new Demo2D();
            case "3d" -> new Demo3D();
            default -> throw new UnsupportedOperationException("Unknown graphics mode");
        };
    }

    @Override
    public void onQuit() {
        System.exit(0);
    }

    @Override
    public void onAbout() {
        Popups.message(null, "MultimediaLib - demo application");
    }
}
