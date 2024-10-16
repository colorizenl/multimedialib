//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.headless;

import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.ErrorHandler;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.Pointer;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.multimedialib.renderer.WindowOptions;
import nl.colorize.multimedialib.renderer.java2d.StandardNetwork;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.StageVisitor;
import nl.colorize.util.Subscribable;

import java.util.List;

import static nl.colorize.multimedialib.stage.ColorRGB.BLACK;

/**
 * Headless renderer implementation intended for testing or simulating on
 * platforms without a graphics environment. The renderer does not actually
 * run an animation loop, frame updates need to be performed manually by
 * calling {@link #doFrame()}. Graphics and input can be simulated using the
 * methods provided by this class.
 * <p>
 * Although the headless renderer does not display graphics, it is capable
 * of loading images. See {@link HeadlessMediaLoader} for more information.
 */
@VisibleForTesting
@Getter
@Setter
public class HeadlessRenderer implements Renderer, InputDevice {

    private GraphicsMode graphicsMode;
    private WindowOptions windowOptions;
    private DisplayMode displayMode;
    private StageVisitor graphics;
    private HeadlessMediaLoader mediaLoader;
    private Network network;
    private SceneContext context;

    private boolean touchAvailable;
    private boolean keyboardAvailable;
    private Point2D pointer;
    private boolean pointerPressed;
    private boolean pointerReleased;

    public static final int DEFAULT_WIDTH = 800;
    public static final int DEFAULT_HEIGHT = 600;
    public static final int DEFAULT_FRAMERATE = 30;

    public static final FontFace DEFAULT_FONT = new FontFace(null, "sans-serif", 10, BLACK);

    public HeadlessRenderer(DisplayMode displayMode, boolean graphicsEnvironmentEnabled) {
        this.graphicsMode = GraphicsMode.HEADLESS;
        this.windowOptions = new WindowOptions("<headless>");
        this.displayMode = displayMode;
        this.graphics = null;
        this.mediaLoader = new HeadlessMediaLoader(graphicsEnvironmentEnabled);
        this.network = new StandardNetwork();
        this.context = new SceneContext(this, mediaLoader, this, network);

        this.touchAvailable = false;
        this.keyboardAvailable = false;
        this.pointer = new Point2D(0f, 0f);
        this.pointerPressed = false;
        this.pointerReleased = false;
    }

    public HeadlessRenderer(Canvas canvas, int framerate) {
        this(new DisplayMode(canvas, framerate), true);
    }
    
    public HeadlessRenderer() {
        this(new Canvas(DEFAULT_WIDTH, DEFAULT_HEIGHT, ScaleStrategy.flexible()), DEFAULT_FRAMERATE);
    }

    @Override
    public void start(Scene initialScene, ErrorHandler errorHandler) {
        context.changeScene(initialScene);
        doFrame();
    }

    @Override
    public void terminate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void takeScreenshot(FilePointer dest) {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    // Simulate animation loop
    //-------------------------------------------------------------------------

    public void doFrame() {
        context.update(1f / displayMode.framerate());
    }

    @Override
    public void update(float deltaTime) {
    }

    //-------------------------------------------------------------------------
    // Simulate input
    //-------------------------------------------------------------------------

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
    public Subscribable<String> requestTextInput(String label, String initialValue) {
        return new Subscribable<>();
    }

    @Override
    public void fillClipboard(String text) {
    }

    @Override
    public String toString() {
        return "<headless>";
    }
}
