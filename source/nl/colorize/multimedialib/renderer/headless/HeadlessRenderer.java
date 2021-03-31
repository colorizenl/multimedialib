//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.headless;

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
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsContext2D;
import nl.colorize.multimedialib.renderer.GraphicsMode;
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

    private Canvas canvas;
    private int framerate;
    private boolean graphicsEnvironmentEnabled;
    private SceneContext context;
    
    public static final int DEFAULT_WIDTH = 800;
    public static final int DEFAULT_HEIGHT = 600;
    public static final int DEFAULT_FRAMERATE = 30;

    public HeadlessRenderer(Canvas canvas, int framerate) {
        this.canvas = canvas;
        this.framerate = framerate;
        this.graphicsEnvironmentEnabled = true;
    }
    
    public HeadlessRenderer() {
        this(Canvas.flexible(DEFAULT_WIDTH, DEFAULT_HEIGHT), DEFAULT_FRAMERATE);
    }

    @Override
    public GraphicsMode getGraphicsMode() {
        return GraphicsMode.HEADLESS;
    }

    @Override
    public void start(Scene initialScene) {
        HeadlessMediaLoader mediaLoader = new HeadlessMediaLoader(graphicsEnvironmentEnabled);
        StandardNetworkAccess network = new StandardNetworkAccess();
        context = new SceneContext(canvas, new NullInputDevice(), mediaLoader, network);
        context.changeScene(initialScene);

        doFrame();
    }
    
    public void doFrame() {
        context.update(1f / framerate);
        context.getStage().render2D(new NullGraphicsContext(canvas));
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
        public void drawLine(Point2D from, Point2D to, ColorRGB color, float thickness) {
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
