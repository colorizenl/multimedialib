//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.base.Charsets;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.java2d.Java2DRenderer;
import nl.colorize.multimedialib.renderer.java2d.WindowOptions;
import nl.colorize.multimedialib.renderer.libgdx.GDXRenderer;
import nl.colorize.multimedialib.scene.Application;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.util.LogHelper;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.swing.ApplicationMenuListener;
import nl.colorize.util.swing.Popups;
import org.kohsuke.args4j.Option;

import java.util.logging.Logger;

/**
 * Launches one of the demo applications from the command line. The behavior of
 * the demo can be controlled using the command line parameters.
 * <p>
 * Refer to the documentation for {@link Demo2D} for more information
 * on the demo application itself.
 */
public class DemoLauncher extends CommandLineTool implements ApplicationMenuListener {

    @Option(name = "-renderer", required = true, usage = "Renderer to use for the demo (java2d, gdx)")
    public String rendererName;

    @Option(name = "-graphics", required = true, usage = "Either '2d' or '3d'")
    public String graphics;

    @Option(name = "-framerate", required = false, usage = "Demo framerate, default is 60 fps")
    public int framerate = Demo2D.DEFAULT_FRAMERATE;

    @Option(name = "-canvas", required = false, usage = "Uses a fixed canvas size to display graphics")
    public boolean canvas = false;

    @Option(name = "-orientationlock", required = false, usage = "Restricts the demo to landscape orientation")
    public boolean orientationLock = false;

    @Option(name = "-verification", required = false, usage = "Prints instructions for verification")
    public boolean verification = false;

    private static final ResourceFile VERIFICATION_FILE = new ResourceFile("verification-instructions.txt");
    private static final Logger LOGGER = LogHelper.getLogger(DemoLauncher.class);

    public static void main(String[] args) {
        DemoLauncher demo = new DemoLauncher();
        demo.start(args);
    }

    @Override
    public void run() {
        start();

        if (verification) {
            printVerificationInstructions();
        }
    }

    private void start() {
        Renderer renderer = createRenderer();
        Application.start(renderer, createDemoScene());
    }

    private Renderer createRenderer() {
        switch (rendererName) {
            case "java2d" : return createJava2DRenderer();
            case "gdx" : return createGDXRenderer();
            default : throw new IllegalArgumentException("Renderer not supported: " + rendererName);
        }
    }

    private Java2DRenderer createJava2DRenderer() {
        return new Java2DRenderer(getCanvas(), framerate, getWindowOptions());
    }

    private GDXRenderer createGDXRenderer() {
        GDXRenderer renderer = new GDXRenderer(getCanvas(), framerate, getWindowOptions());
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

    private void printVerificationInstructions() {
        String instructions = VERIFICATION_FILE.read(Charsets.UTF_8);
        LOGGER.info("\n\n" + instructions);
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
