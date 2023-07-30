//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.headless;

import com.google.common.annotations.VisibleForTesting;
import lombok.Data;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.ErrorHandler;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.java2d.StandardNetwork;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.stage.StageVisitor;
import nl.colorize.util.Stopwatch;

import java.io.File;
import java.util.List;
import java.util.Optional;

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
@Data
public class HeadlessRenderer implements Renderer, InputDevice {

    private final GraphicsMode graphicsMode;
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

    public HeadlessRenderer(DisplayMode displayMode, boolean graphicsEnvironmentEnabled) {
        this.graphicsMode = GraphicsMode.HEADLESS;
        this.displayMode = displayMode;
        this.graphics = null;
        this.mediaLoader = new HeadlessMediaLoader(graphicsEnvironmentEnabled);
        this.network = new StandardNetwork();
        this.context = new SceneContext(this, new Stopwatch());

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
        this(Canvas.flexible(DEFAULT_WIDTH, DEFAULT_HEIGHT), DEFAULT_FRAMERATE);
    }

    @Override
    public void start(Scene initialScene, ErrorHandler errorHandler) {
        context.changeScene(initialScene);
        doFrame();
    }
    
    @Override
    public void takeScreenshot(File outputFile) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void terminate() {
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
    public InputDevice getInput() {
        return this;
    }

    @Override
    public Optional<Point2D> getPointer() {
        return Optional.ofNullable(pointer);
    }

    @Override
    public boolean isPointerPressed(Rect area) {
        return pointerPressed;
    }

    @Override
    public boolean isPointerReleased(Rect area) {
        return pointerReleased;
    }

    @Override
    public void clearPointerReleased() {
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
    public String requestTextInput(String label, String initialValue) {
        return null;
    }
}
