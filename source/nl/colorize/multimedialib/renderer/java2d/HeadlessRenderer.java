//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;

import nl.colorize.multimedialib.graphics.Align;
import nl.colorize.multimedialib.graphics.AlphaTransform;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.ApplicationData;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsContext2D;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.NetworkAccess;
import nl.colorize.multimedialib.renderer.KeyCode;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.RenderCallback;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.Stage;
import nl.colorize.util.Platform;
import nl.colorize.util.PlatformFamily;

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

    private Canvas canvas;
    private int framerate;
    private List<RenderCallback> callbacks;
    private boolean graphicsEnvironmentEnabled;
    
    public static final int DEFAULT_WIDTH = 800;
    public static final int DEFAULT_HEIGHT = 600;
    public static final int DEFAULT_FRAMERATE = 30;

    public HeadlessRenderer(Canvas canvas, int framerate) {
        this.canvas = canvas;
        this.framerate = framerate;
        this.callbacks = new ArrayList<>();
        this.graphicsEnvironmentEnabled = true;
    }
    
    public HeadlessRenderer() {
        this(Canvas.flexible(DEFAULT_WIDTH, DEFAULT_HEIGHT), DEFAULT_FRAMERATE);
    }

    @Override
    public GraphicsMode getSupportedGraphicsMode() {
        return GraphicsMode.HEADLESS;
    }
    
    @Override
    public void attach(RenderCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void start() {
        doFrame();
    }
    
    public void doFrame() {
        NullGraphicsContext graphics = new NullGraphicsContext(canvas);
        
        for (RenderCallback callback : callbacks) {
            callback.update(this, 1f / framerate);
            callback.render(this, graphics);
        }
    }

    @Override
    public Canvas getCanvas() {
        return canvas;
    }

    @Override
    public Stage getStage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputDevice getInputDevice() {
        return new NullInputDevice();
    }

    @Override
    public MediaLoader getMediaLoader() {
        return new HeadlessMediaLoader(graphicsEnvironmentEnabled);
    }

    @Override
    public ApplicationData getApplicationData(String appName) {
        return new StandardApplicationData(appName);
    }

    @Override
    public NetworkAccess getNetwork() {
        return new StandardNetworkAccess();
    }

    @Override
    public String takeScreenshot() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlatformFamily getPlatform() {
        return Platform.getPlatformFamily();
    }

    public void setGraphicsEnvironmentEnabled(boolean graphicsEnvironmentEnabled) {
        this.graphicsEnvironmentEnabled = graphicsEnvironmentEnabled;
    }

    public boolean isGraphicsEnvironmentEnabled() {
        return graphicsEnvironmentEnabled;
    }

    private static class NullGraphicsContext implements GraphicsContext2D {
        
        private Canvas canvas;
        
        public NullGraphicsContext(Canvas canvas) {
            this.canvas = canvas;
        }

        @Override
        public Canvas getCanvas() {
            return canvas;
        }

        @Override
        public void drawBackground(ColorRGB backgroundColor) {
        }

        @Override
        public void drawRect(Rect rect, ColorRGB color, AlphaTransform alpha) {
        }

        @Override
        public void drawCircle(Circle circle, ColorRGB color, AlphaTransform alpha) {
        }

        @Override
        public void drawPolygon(Polygon polygon, ColorRGB color, AlphaTransform alpha) {
        }

        @Override
        public void drawImage(Image image, float x, float y, Transform transform) {
        }

        @Override
        public void drawText(String text, TTFont font, float x, float y, Align align, AlphaTransform alpha) {
        }
    }
    
    private static class NullInputDevice implements InputDevice {

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
    }
}
