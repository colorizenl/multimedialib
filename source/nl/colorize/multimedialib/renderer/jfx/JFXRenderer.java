//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.jfx;

import com.google.common.base.Preconditions;
import javafx.application.Application;
import lombok.Getter;
import nl.colorize.multimedialib.math.Box;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.math.Shape3D;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.RenderConfig;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.java2d.StandardNetwork;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.SceneManager;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.util.swing.SwingUtils;

import javax.swing.SwingUtilities;
import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Renderer based on <a href="https://openjfx.io">OpenJFX</a>, which was
 * previously known as JavaFX. Note that JavaFX is only used for the animation
 * loop, graphics, and input. Other renderer capabilities are identical to the
 * Java2D renderer, since both are based on the Java standard library.
 */
@Getter
public class JFXRenderer implements Renderer, SceneContext {

    private RenderConfig config;
    private JFXGraphics graphics;
    private JFXInput input;
    private JFXMediaLoader mediaLoader;
    private Network network;
    private SceneManager sceneManager;
    private Stage stage;
    private List<File> screenshotQueue;

    private static JFXRenderer instance;

    private JFXRenderer() {
        this.screenshotQueue = new CopyOnWriteArrayList<>();
    }

    @Override
    public void start(RenderConfig config, Scene initialScene) {
        // The JavaFX relies on Swing for certain operations. There is
        // no point in avoiding the usage of Swing entirely, since it
        // is still part of the Java runtime.
        SwingUtilities.invokeLater(SwingUtils::initializeSwing);

        this.config = config;
        mediaLoader = new JFXMediaLoader();
        network = new StandardNetwork();
        input = new JFXInput(config.getCanvas());
        graphics = new JFXGraphics(config, mediaLoader);
        sceneManager = new SceneManager();
        stage = new Stage(config.getGraphicsMode(), config.getCanvas());

        changeScene(initialScene);
        Application.launch(JFXAnimationLoop.class);
    }

    @Override
    public void terminate() {
        System.exit(0);
    }

    @Override
    public Mesh createMesh(Shape3D shape, ColorRGB color) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Point2D project(Point3D position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean castPickRay(Point2D canvasPosition, Box area) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void takeScreenshot(File screenshotFile) {
        screenshotQueue.add(screenshotFile);
    }

    @Override
    public String getRendererName() {
        return "JavaFX renderer";
    }

    @Override
    public boolean isSupported(GraphicsMode graphicsMode) {
        return graphicsMode == GraphicsMode.MODE_2D;
    }

    /**
     * Launches the JavaFX renderer. Note that JavaFX does not support
     * multiple instances of the renderer to be active simultaneously.
     *
     * @throws IllegalStateException if this method is called at a moment
     *         when another {@link JFXRenderer} instance is already active.
     */
    public static synchronized JFXRenderer launch() {
        Preconditions.checkState(instance == null, "Another JFXRenderer instance is already active");
        instance = new JFXRenderer();
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
        Preconditions.checkState(instance.config != null, "JFXRenderer not yet initialized");
        return instance;
    }
}
