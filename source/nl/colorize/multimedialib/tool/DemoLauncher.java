//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import nl.colorize.multimedialib.demo.Demo2D;
import nl.colorize.multimedialib.demo.Demo3D;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.WindowOptions;
import nl.colorize.multimedialib.renderer.java2d.Java2DRenderer;
import nl.colorize.multimedialib.renderer.libgdx.GDXRenderer;
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
        Renderer renderer = createRenderer();
        renderer.start(createDemoScene());
    }

    private Renderer createRenderer() {
        switch (rendererName) {
            case "java2d" : return createJava2DRenderer();
            case "gdx" : return createGDXRenderer();
            default : throw new IllegalArgumentException("Renderer not supported: " + rendererName);
        }
    }

    private Java2DRenderer createJava2DRenderer() {
        return new Java2DRenderer(getDisplayMode(), getWindowOptions());
    }

    private GDXRenderer createGDXRenderer() {
        GDXRenderer renderer = new GDXRenderer(getDisplayMode(), getWindowOptions());
        if (graphics.equals("3d")) {
            renderer.enableFreeCamera();
        }
        return renderer;
    }

    private Scene createDemoScene() {
        switch (graphics) {
            case "2d" : return new Demo2D();
            case "3d" : return new Demo3D();
            default : throw new IllegalArgumentException("Unknown graphics mode: " + graphics);
        }
    }

    private DisplayMode getDisplayMode() {
        return new DisplayMode(getCanvas(), framerate);
    }

    private Canvas getCanvas() {
        if (canvas) {
            return Canvas.zoomOut(Demo2D.DEFAULT_CANVAS_WIDTH, Demo2D.DEFAULT_CANVAS_HEIGHT);
        } else {
            return Canvas.flexible(Demo2D.DEFAULT_CANVAS_WIDTH, Demo2D.DEFAULT_CANVAS_HEIGHT);
        }
    }

    private WindowOptions getWindowOptions() {
        WindowOptions windowOptions = new WindowOptions("MultimediaLib - Demo");
        windowOptions.setAppMenuListener(this);
        return windowOptions;
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
