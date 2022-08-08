//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.demo;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.WindowOptions;
import nl.colorize.multimedialib.scene.ErrorHandler;
import nl.colorize.multimedialib.scene.MultimediaAppLauncher;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.util.cli.CommandLineArgumentParser;
import nl.colorize.util.swing.ApplicationMenuListener;
import nl.colorize.util.swing.Popups;

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
    private boolean canvas;

    public static void main(String[] args) {
        CommandLineArgumentParser argParser = new CommandLineArgumentParser("DemoLauncher")
            .add("-renderer", "Renderer to use for the demo (java2d, gdx)")
            .add("-graphics", "Either '2d' or '3d'")
            .addOptional("-framerate", "60", "Demo framerate, default is 60 fps")
            .addFlag("-canvas", "Uses a fixed canvas size to display graphics");

        argParser.parseArgs(args);

        DemoLauncher demo = new DemoLauncher();
        demo.rendererName = argParser.get("renderer");
        demo.graphics = argParser.get("graphics");
        demo.framerate = argParser.getInt("framerate");
        demo.canvas = argParser.getBool("canvas");
        demo.start();
    }

    private void start() {
        DisplayMode displayMode = new DisplayMode(getCanvas(), framerate);
        WindowOptions window = new WindowOptions("MultimediaLib - Demo", WindowOptions.DEFAULT_ICON, this);

        Renderer renderer = switch (rendererName) {
            case "java2d" -> MultimediaAppLauncher.launchJava2D(displayMode, window);
            case "libgdx", "gdx" -> MultimediaAppLauncher.launchGDX(displayMode, window);
            default -> throw new UnsupportedOperationException("Unsupported renderer: " + rendererName);
        };

        Scene demo = createDemoScene();
        renderer.start(demo, ErrorHandler.DEFAULT);
    }

    private Scene createDemoScene() {
        return switch (graphics) {
            case "2d" -> new Demo2D();
            case "3d" -> new Demo3D();
            default -> throw new UnsupportedOperationException("Unknown graphics mode: " + graphics);
        };
    }

    private Canvas getCanvas() {
        if (canvas) {
            return Canvas.zoomOut(Demo2D.DEFAULT_CANVAS_WIDTH, Demo2D.DEFAULT_CANVAS_HEIGHT);
        } else {
            return Canvas.flexible(Demo2D.DEFAULT_CANVAS_WIDTH, Demo2D.DEFAULT_CANVAS_HEIGHT);
        }
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
