//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
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
import nl.colorize.multimedialib.scene.SceneManager;
import nl.colorize.util.LogHelper;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.swing.ApplicationMenuListener;
import nl.colorize.util.swing.Popups;
import org.kohsuke.args4j.Option;

import java.io.IOException;
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
    private int framerate = DEFAULT_FRAMERATE;

    @Option(name = "-verification", required = false, usage = "Prints instructions for verification")
    public boolean verification = false;

    private static final int DEFAULT_CANVAS_WIDTH = 800;
    private static final int DEFAULT_CANVAS_HEIGHT = 600;
    private static final int DEFAULT_FRAMERATE = 60;
    private static final ResourceFile VERIFICATION_FILE = new ResourceFile("verification-instructions.txt");
    private static final Logger LOGGER = LogHelper.getLogger(DemoLauncher.class);

    public static void main(String[] args) {
        DemoLauncher demo = new DemoLauncher();
        demo.start(args);
    }

    @Override
    public void run() {
        Renderer renderer = createRenderer();
        SceneManager sceneManager = SceneManager.attach(renderer);

        LOGGER.info("Launching demo application using " + renderer.getClass().getName());

        DemoApplication demo = new DemoApplication(renderer);
        sceneManager.changeScene(demo);

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
        return new Canvas(DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT, 1f);
    }

    private WindowOptions getWindowOptions() {
        WindowOptions windowOptions = new WindowOptions("MultimediaLib - Demo");
        windowOptions.setAppMenuListener(this);
        return windowOptions;
    }

    private void printVerificationInstructions() {
        try {
            String instructions = VERIFICATION_FILE.read(Charsets.UTF_8);
            LOGGER.info("\n\n" + instructions);
        } catch (IOException e) {
            throw new AssertionError("Cannot load verification instructions", e);
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
}
