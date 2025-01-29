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
import nl.colorize.util.cli.Arg;
import nl.colorize.util.cli.CommandLineArgumentParser;
import nl.colorize.util.swing.ApplicationMenuListener;
import nl.colorize.util.swing.Popups;

import static nl.colorize.multimedialib.renderer.GraphicsMode.MODE_2D;
import static nl.colorize.multimedialib.tool.Demo2D.DEFAULT_CANVAS_HEIGHT;
import static nl.colorize.multimedialib.tool.Demo2D.DEFAULT_CANVAS_WIDTH;

/**
 * Launches one of the demo applications from the command line. The behavior of
 * the demo can be controlled using the command line parameters.
 * <p>
 * Refer to the documentation for {@link Demo2D} for more information
 * on the demo application itself.
 */
public class DemoLauncher implements ApplicationMenuListener {

    @Arg(name = "-renderer", usage = "One of 'java2d', 'javafx', 'gdx'.")
    protected String rendererName;

    @Arg(usage = "Either '2d' or '3d'.")
    protected String graphics;

    @Arg(defaultValue = "60", usage = "Framerate, default is 60 fps.")
    protected int framerate;

    @Arg(usage = "Starts the demo fullscreen instead of in a window.")
    protected boolean fullscreen;

    @Arg(name = "zoom", usage = "Uses a fixed canvas size to display graphics.")
    protected boolean canvasZoom;

    public static void main(String[] argv) {
        CommandLineArgumentParser argParser = new CommandLineArgumentParser(DemoLauncher.class);
        DemoLauncher launcher = argParser.parse(argv, DemoLauncher.class);
        launcher.start();
    }

    private void start() {
        RenderConfig config = RenderConfig.forDesktop(rendererName, getGraphicsMode(), initCanvas());
        config.setFramerate(framerate);
        config.getWindowOptions().setTitle("MultimediaLib - Demo");
        config.getWindowOptions().setAppMenu(this);
        config.getWindowOptions().setFullscreen(fullscreen);

        switch (config.getGraphicsMode()) {
            case MODE_2D -> config.start(new Demo2D());
            case MODE_3D -> config.start(new Demo3D());
            default -> throw new UnsupportedOperationException();
        }
    }

    private Canvas initCanvas() {
        ScaleStrategy scaleStrategy = canvasZoom ? ScaleStrategy.scale() : ScaleStrategy.flexible();
        return new Canvas(DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT, scaleStrategy);
    }

    private GraphicsMode getGraphicsMode() {
        return switch (graphics) {
            case "2d" -> MODE_2D;
            case "3d" -> GraphicsMode.MODE_3D;
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
