//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.WindowOptions;
import nl.colorize.multimedialib.renderer.java2d.Java2DRenderer;
import nl.colorize.multimedialib.renderer.libgdx.GDXRenderer;
import nl.colorize.multimedialib.scene.Application;
import nl.colorize.util.LogHelper;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.swing.ApplicationMenuListener;
import nl.colorize.util.swing.Popups;
import org.kohsuke.args4j.Option;

import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Launches the demo application from the command line. The behavior of the demo
 * can be controlled using the command line parameters.
 * <p>
 * Refer to the documentation for {@link DemoApplication} for more information
 * on the demo application itself.
 */
public class DemoLauncher extends CommandLineTool implements ApplicationMenuListener {

    @Option(name = "-renderer", required = true, usage = "Renderer to use for the demo (java2d, gdx)")
    public String rendererName;

    @Option(name = "-framerate", required = false, usage = "Demo framerate, default is 30 fps")
    private int framerate = DemoApplication.DEFAULT_FRAMERATE;

    @Option(name = "-canvas", required = false, usage = "Uses a fixed canvas size to display graphics")
    private boolean canvas = false;

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
        Renderer renderer = createRenderer();
        LOGGER.info("Launching demo application using " + renderer.getClass().getName());

        Application app = new Application(renderer);
        app.changeScene(new DemoApplication(app));

        if (verification) {
            printVerificationInstructions();
        }
    }

    private Renderer createRenderer() {
        Map<String, Supplier<Renderer>> possibilities = ImmutableMap.of(
            "java2d", this::createJava2DRenderer,
            "gdx", this::createGDXRenderer
        );

        Preconditions.checkArgument(possibilities.containsKey(rendererName),
            "Renderer not supported: " + rendererName);

        return possibilities.get(rendererName).get();
    }

    private Java2DRenderer createJava2DRenderer() {
        return new Java2DRenderer(getCanvas(), framerate, getWindowOptions());
    }

    private GDXRenderer createGDXRenderer() {
        return new GDXRenderer(getCanvas(), framerate, getWindowOptions());
    }

    private Canvas getCanvas() {
        if (canvas) {
            return Canvas.create(DemoApplication.DEFAULT_CANVAS_WIDTH, DemoApplication.DEFAULT_CANVAS_HEIGHT);
        } else {
            return Canvas.flexible(DemoApplication.DEFAULT_CANVAS_WIDTH, DemoApplication.DEFAULT_CANVAS_HEIGHT);
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
