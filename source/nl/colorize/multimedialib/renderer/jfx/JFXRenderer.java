//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.jfx;

import com.google.common.base.Preconditions;
import javafx.application.Application;
import lombok.Getter;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.ErrorHandler;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.WindowOptions;
import nl.colorize.multimedialib.renderer.java2d.StandardNetwork;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Stopwatch;

import java.util.logging.Logger;

/**
 * Renderer based on <a href="https://openjfx.io">OpenJFX</a>, which was
 * previously known as JavaFX. Note that JavaFX is only used for the animation
 * loop, graphics, and input. Other renderer capabilities are identical to the
 * Java2D renderer, since both are based on the Java standard library.
 */
@Getter
public class JFXRenderer implements Renderer {

    private GraphicsMode graphicsMode;
    private DisplayMode displayMode;
    private WindowOptions windowOptions;

    private JFXGraphics graphics;
    private JFXInput input;
    private JFXMediaLoader mediaLoader;
    private Network network;

    private SceneContext context;
    private ErrorHandler errorHandler;

    private static JFXRenderer instance;

    private static final Logger LOGGER = LogHelper.getLogger(JFXRenderer.class);

    private JFXRenderer(DisplayMode displayMode, WindowOptions windowOptions) {
        this.graphicsMode = GraphicsMode.MODE_2D;
        this.displayMode = displayMode;
        this.windowOptions = windowOptions;
    }

    @Override
    public void start(Scene initialScene, ErrorHandler errorHandler) {
        mediaLoader = new JFXMediaLoader();
        network = new StandardNetwork();
        input = new JFXInput(displayMode.canvas());
        graphics = new JFXGraphics(displayMode, mediaLoader);

        Stopwatch timer = new Stopwatch();
        context = new SceneContext(this, mediaLoader, input, network);
        context.changeScene(initialScene);

        this.errorHandler = errorHandler;

        Application.launch(JFXAnimationLoop.class);
    }

    @Override
    public void terminate() {
        System.exit(0);
    }

    @Override
    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    @Override
    public GraphicsMode getGraphicsMode() {
        return GraphicsMode.MODE_2D;
    }

    @Override
    public String toString() {
        return "JavaFX renderer";
    }

    /**
     * Launches the JavaFX renderer. Note that JavaFX does not support
     * multiple instances of the renderer to be active simultaneously.
     *
     * @throws IllegalStateException if this method is called at a moment
     *         when another {@link JFXRenderer} instance is already active.
     */
    public static synchronized JFXRenderer launch(DisplayMode displayMode, WindowOptions windowOptions) {
        Preconditions.checkState(instance == null, "Another JFXRenderer instance is already active");
        instance = new JFXRenderer(displayMode, windowOptions);
        return instance;
    }

    /**
     * Returns the single currently active {@link JFXRenderer} instance. This
     * method is used to access the renderer from the JavaFX application
     * thread.
     *
     * @throws IllegalStateException when trying to call this method before
     *         the renderer has been initialized.
     */
    protected static synchronized JFXRenderer accessInstance() {
        Preconditions.checkState(instance != null, "JFXRenderer has not yet been initialized");
        Preconditions.checkState(instance.context != null, "Scene context not yet initialized");
        return instance;
    }
}
