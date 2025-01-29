//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.headless;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Box;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.math.Shape3D;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.Pointer;
import nl.colorize.multimedialib.renderer.RenderConfig;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.multimedialib.renderer.java2d.StandardMediaLoader;
import nl.colorize.multimedialib.renderer.java2d.StandardNetwork;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.SceneManager;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.StageVisitor;
import nl.colorize.util.Development;
import nl.colorize.util.Subject;

import java.io.File;
import java.util.List;

import static nl.colorize.multimedialib.stage.ColorRGB.BLACK;

/**
 * Headless renderer implementation which can be used for testing and
 * simulation purposes, and (unlike all other renderers) can be used on
 * platforms without a graphics environment.
 * <p>
 * The headless renderer can be started without an active animation loop.
 * In this scenatio, all frame updates need to be performed manually.
 */
@Development
@Getter
@Setter
public class HeadlessRenderer implements Renderer, SceneContext, InputDevice {

    private boolean graphicsEnvironmentEnabled;
    private RenderConfig config;
    private StageVisitor graphics;
    private MediaLoader mediaLoader;
    private Network network;
    private SceneManager sceneManager;
    private Stage stage;

    private boolean touchAvailable;
    private boolean keyboardAvailable;
    private Point2D pointer;
    private boolean pointerPressed;
    private boolean pointerReleased;

    public static final FontFace DEFAULT_FONT = new FontFace(null, "sans-serif", 10, BLACK);

    public HeadlessRenderer(boolean graphicsEnvironmentEnabled) {
        this.graphicsEnvironmentEnabled = graphicsEnvironmentEnabled;

        this.touchAvailable = false;
        this.keyboardAvailable = false;
        this.pointer = new Point2D(0f, 0f);
        this.pointerPressed = false;
        this.pointerReleased = false;

        // The headless renderer doesn't need to be started explicitly,
        // and can be used immediately after creation.
        Canvas defaultCanvas = new Canvas(800, 600, ScaleStrategy.flexible());
        RenderConfig defaultConfig = RenderConfig.headless(GraphicsMode.HEADLESS, defaultCanvas);
        defaultConfig.setFramerate(10);
        Scene nullScene = (context, deltaTime) -> {};
        start(defaultConfig, nullScene);
    }

    @Override
    public void start(RenderConfig config, Scene initialScene) {
        this.config = config;
        this.graphics = null;
        this.mediaLoader = new StandardMediaLoader();
        this.network = new StandardNetwork();
        this.sceneManager = new SimulatedSceneManager();
        this.stage = new Stage(config.getGraphicsMode(), config.getCanvas());

        sceneManager.changeScene(initialScene);
        doFrame(0f);
    }

    public void start(Scene initialScene) {
        start(config, initialScene);
    }

    @Override
    public void terminate() {
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
    public Mesh createMesh(Shape3D shape, ColorRGB color) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void takeScreenshot(File screenshotFile) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRendererName() {
        return "Headless renderer";
    }

    @Override
    public boolean isSupported(GraphicsMode graphicsMode) {
        return true;
    }

    @Deprecated
    public SceneContext getContext() {
        return this;
    }

    //-------------------------------------------------------------------------
    // Simulate animation loop
    //-------------------------------------------------------------------------

    public void doFrame() {
        update(1f);
    }

    public void doFrame(float deltaTime) {
        update(deltaTime);
    }

    @Override
    public void update(float deltaTime) {
        if (sceneManager instanceof SimulatedSceneManager simulated) {
            simulated.simulateFrameUpdate(this, deltaTime);
        }
    }

    //-------------------------------------------------------------------------
    // Simulate input
    //-------------------------------------------------------------------------

    @Override
    public InputDevice getInput() {
        return this;
    }

    @Override
    public Iterable<Pointer> getPointers() {
        Pointer pointerObject = new Pointer("headless");
        pointerObject.setPosition(pointer);

        if (pointerReleased) {
            pointerObject.setState(Pointer.STATE_RELEASED);
        } else if (pointerPressed) {
            pointerObject.setState(Pointer.STATE_PRESSED);
        } else {
            pointerObject.setState(Pointer.STATE_IDLE);
        }

        return List.of(pointerObject);
    }

    @Override
    public void clearPointerState() {
        pointerPressed = false;
        pointerReleased = false;
    }

    @Override
    public boolean isKeyPressed(KeyCode keyCode) {
        return false;
    }

    @Override
    public boolean isKeyReleased(KeyCode keyCode) {
        return false;
    }

    @Override
    public Subject<String> requestTextInput(String label, String initialValue) {
        return new Subject<>();
    }

    @Override
    public void fillClipboard(String text) {
    }

    /**
     * Extended scene manager that can simulate frame updates independent of
     * how much time has actually elapsed.
     */
    private static class SimulatedSceneManager extends SceneManager {

        public void simulateFrameUpdate(SceneContext context, float deltaTime) {
            performFrameUpdate(context, deltaTime);
        }
    }
}
