//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.headless;

import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.ErrorHandler;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.RenderCapabilities;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.java2d.Java2DRenderer;
import nl.colorize.multimedialib.renderer.java2d.StandardNetwork;
import nl.colorize.multimedialib.scene.RenderContext;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;

import java.util.Collections;
import java.util.List;

/**
 * Headless renderer implementation that uses {@link Java2DRenderer} for platform
 * access, but does not draw any graphics. The renderer does not run an animation
 * loop, frame updates need to be performed manually by calling {@link #doFrame()}.
 * This is primarily intended for testing, or for simulating in situations where
 * a graphics environment is not available.
 * <p>
 * By default, the renderer will not *draw* images, but it will still *load* them
 * using {@link HeadlessMediaLoader}.
 */
public class HeadlessRenderer implements Renderer, InputDevice {

    private DisplayMode displayMode;
    private HeadlessMediaLoader mediaLoader;
    private SceneContext context;
    
    public static final int DEFAULT_WIDTH = 800;
    public static final int DEFAULT_HEIGHT = 600;
    public static final int DEFAULT_FRAMERATE = 30;

    public HeadlessRenderer(DisplayMode displayMode, boolean graphicsEnvironmentEnabled) {
        this.displayMode = displayMode;
        this.mediaLoader = new HeadlessMediaLoader(graphicsEnvironmentEnabled);
        this.context = new RenderContext(this);
    }

    public HeadlessRenderer(Canvas canvas, int framerate) {
        this(new DisplayMode(canvas, framerate), true);
    }
    
    public HeadlessRenderer() {
        this(Canvas.forNative(DEFAULT_WIDTH, DEFAULT_HEIGHT), DEFAULT_FRAMERATE);
    }

    @Override
    public void start(Scene initialScene, ErrorHandler errorHandler) {
        context.changeScene(initialScene);
        doFrame();
    }
    
    public void doFrame() {
        context.update(1f / displayMode.framerate());
    }

    @Override
    public RenderCapabilities getCapabilities() {
        Network network = new StandardNetwork();
        return new RenderCapabilities(GraphicsMode.HEADLESS, displayMode, null,
            this, mediaLoader, network);
    }

    @Override
    public List<Point2D> getPointers() {
        return Collections.emptyList();
    }

    @Override
    public boolean isPointerPressed(Rect area) {
        return false;
    }

    @Override
    public boolean isPointerReleased(Rect area) {
        return false;
    }

    @Override
    public boolean isPointerReleased() {
        return false;
    }

    @Override
    public void clearPointerReleased() {
    }

    @Override
    public boolean isTouchAvailable() {
        return false;
    }

    @Override
    public boolean isKeyboardAvailable() {
        return true;
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

    public SceneContext getContext() {
        return context;
    }
}
