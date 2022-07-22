//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.headless;

import com.google.common.annotations.VisibleForTesting;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.java2d.Java2DRenderer;
import nl.colorize.multimedialib.renderer.java2d.StandardNetworkAccess;
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
 * using {@link HeadlessMediaLoader}. Image loading can be disabled entirely by
 * using {@link #setGraphicsEnvironmentEnabled(boolean)}.
 */
@VisibleForTesting
public class HeadlessRenderer implements Renderer {

    private DisplayMode displayMode;
    private boolean graphicsEnvironmentEnabled;
    private SceneContext context;
    
    public static final int DEFAULT_WIDTH = 800;
    public static final int DEFAULT_HEIGHT = 600;
    public static final int DEFAULT_FRAMERATE = 30;

    public HeadlessRenderer(DisplayMode displayMode) {
        this.displayMode = displayMode;
        this.graphicsEnvironmentEnabled = true;

        HeadlessMediaLoader mediaLoader = new HeadlessMediaLoader(graphicsEnvironmentEnabled);
        StandardNetworkAccess network = new StandardNetworkAccess();
        NullInputDevice input = new NullInputDevice(displayMode.getCanvas());
        context = new SceneContext(displayMode, input, mediaLoader, network);
    }

    public HeadlessRenderer(Canvas canvas, int framerate) {
        this(new DisplayMode(canvas, framerate));
    }
    
    public HeadlessRenderer() {
        this(Canvas.flexible(DEFAULT_WIDTH, DEFAULT_HEIGHT), DEFAULT_FRAMERATE);
    }

    @Override
    public void start(Scene initialScene) {
        context.changeScene(initialScene);
        doFrame();
    }
    
    public void doFrame() {
        context.update(1f / displayMode.getFramerate());
    }

    @Override
    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    @Override
    public String takeScreenshot() {
        throw new UnsupportedOperationException();
    }

    public void setGraphicsEnvironmentEnabled(boolean graphicsEnvironmentEnabled) {
        this.graphicsEnvironmentEnabled = graphicsEnvironmentEnabled;
    }

    public boolean isGraphicsEnvironmentEnabled() {
        return graphicsEnvironmentEnabled;
    }

    public SceneContext getContext() {
        return context;
    }

    private static class NullInputDevice implements InputDevice {

        private Canvas canvas;

        public NullInputDevice(Canvas canvas) {
            this.canvas = canvas;
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

        @Override
        public Canvas getCanvas() {
            return canvas;
        }
    }
}
