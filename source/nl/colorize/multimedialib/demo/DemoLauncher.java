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
import nl.colorize.multimedialib.renderer.WindowOptions;
import nl.colorize.multimedialib.renderer.java2d.Java2DRenderer;
import nl.colorize.multimedialib.renderer.libgdx.GDXRenderer;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.util.cli.CommandLineArgumentParser;
import nl.colorize.util.swing.ApplicationMenuListener;
import nl.colorize.util.swing.Popups;

import static nl.colorize.multimedialib.demo.Demo2D.DEFAULT_CANVAS_HEIGHT;
import static nl.colorize.multimedialib.demo.Demo2D.DEFAULT_CANVAS_WIDTH;
import static nl.colorize.multimedialib.renderer.WindowOptions.DEFAULT_ICON;

/**
 * Launches one of the demo applications from the command line. The behavior of
 * the demo can be controlled using the command line parameters.
 * <p>
 * Refer to the documentation for {@link Demo2D} for more information
 * on the demo application itself.
 */
public class DemoLauncher implements ApplicationMenuListener {

    private String rendererName;
    private String graphics;
    private int framerate;
    private boolean canvasZoom;

    public static void main(String[] argv) {
        CommandLineArgumentParser args = new CommandLineArgumentParser(DemoLauncher.class)
            .addOptional("--renderer", "Either 'java2d' or 'gdx'")
            .addOptional("--graphics", "Either '2d' or '3d'")
            .addOptional("--framerate", "Demo framerate, default is 60 fps")
            .addFlag("--canvas", "Uses a fixed canvas size to display graphics")
            .parseArgs(argv);

        DemoLauncher demo = new DemoLauncher();
        demo.rendererName = args.get("renderer").getStringOr("java2d");
        demo.graphics = args.get("graphics").getStringOr("2d");
        demo.framerate = args.get("framerate").getIntOr(60);
        demo.canvasZoom = args.get("canvas").getBool();
        demo.start();
    }

    private void start() {
        Canvas canvas = initCanvas();
        DisplayMode displayMode = new DisplayMode(canvas, framerate);
        WindowOptions window = new WindowOptions("MultimediaLib - Demo", DEFAULT_ICON, this);

        Renderer renderer = switch (rendererName) {
            case "java2d" -> new Java2DRenderer(displayMode, window);
            case "libgdx", "gdx" -> new GDXRenderer(getGraphicsMode(), displayMode, window);
            default -> throw new UnsupportedOperationException("Unsupported renderer: " + rendererName);
        };

        Scene demo = createDemoScene();
        renderer.start(demo, (ErrorHandler) demo);
    }

    private Canvas initCanvas() {
        if (canvasZoom) {
            return Canvas.forSize(DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT);
        } else {
            return Canvas.forNative(DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT);
        }
    }

    private GraphicsMode getGraphicsMode() {
        return switch (graphics) {
            case "2d" -> GraphicsMode.MODE_2D;
            case "3d" -> GraphicsMode.MODE_3D;
            default -> throw new UnsupportedOperationException("Unknown graphics mode: " + graphics);
        };
    }

    private Scene createDemoScene() {
        return switch (graphics) {
            case "2d" -> new Demo2D();
            case "3d" -> new Demo3D();
            default -> throw new UnsupportedOperationException("Unknown graphics mode: " + graphics);
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

    @Override
    public boolean hasPreferencesMenu() {
        return false;
    }

    @Override
    public void onPreferences() {
    }
}
