//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.ErrorHandler;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.pixi.PixiGraphics;
import nl.colorize.multimedialib.renderer.three.ThreeGraphics;
import nl.colorize.multimedialib.renderer.webgl.WebGL;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import org.teavm.jso.browser.Window;

/**
 * Renderer based on <a href="http://teavm.org">TeaVM</a> that is transpiled to
 * JavaScript and runs in the browser. Rendering graphics can switch between
 * different frameworks, the requested renderer can be indicated during the
 * build or at runtime using a URL parameter.
 */
public class TeaRenderer implements Renderer {

    private DisplayMode displayMode;
    private TeaGraphics graphics;
    private TeaInputDevice inputDevice;
    private SceneContext context;

    /**
     * Initializes the TeaVM renderer using the specified graphics library.
     * Applications should use one of the factory methods rather than using
     * this constructor directly.
     */
    private TeaRenderer(DisplayMode displayMode, TeaGraphics graphics) {
        this.displayMode = displayMode;
        this.graphics = graphics;
        this.inputDevice = new TeaInputDevice(displayMode.canvas(), graphics);
    }

    @Override
    public void start(Scene initialScene, ErrorHandler errorHandler) {
        TeaMediaLoader mediaLoader = new TeaMediaLoader(graphics);
        TeaNetwork network = new TeaNetwork();

        inputDevice.bindEventHandlers();
        graphics.init(mediaLoader);

        context = new SceneContext(this, mediaLoader, inputDevice, network);
        context.changeScene(initialScene);

        Browser.prepareAnimationLoop();
        Browser.registerErrorHandler(error -> handleError(errorHandler, error));
        Window.requestAnimationFrame(this::onAnimationFrame);
    }

    /**
     * Callback function for the browser animation loop that is called using
     * {@code requestAnimationFrame}. If the application canvas is not ready,
     * this will skip frame logic and instead render an "empty" frame.
     */
    private void onAnimationFrame(double timestamp) {
        if (prepareCanvas()) {
            if (context.syncFrame() > 0) {
                renderFrame();
                inputDevice.reset();
            }
        }

        // Request the next frame. This is intentionally done *after*
        // the frame update, to avoid an infinite error loop if an
        // error occurs somewhere during the frame update.
        Window.requestAnimationFrame(this::onAnimationFrame);
    }

    /**
     * Resizes the canvas based on the requested dimensions and the current
     * browser window. Returns true if the canvas is ready to be used.
     */
    private boolean prepareCanvas() {
        int width = graphics.getDisplayWidth();
        int height = graphics.getDisplayHeight();

        if (width > 0 || height > 0) {
            displayMode.canvas().resizeScreen(width, height);
            return true;
        } else {
            return false;
        }
    }

    private void renderFrame() {
        context.getFrameStats().markStart(FrameStats.PHASE_FRAME_RENDER);
        context.getStage().visit(graphics, context.getSceneTime());
        context.getFrameStats().markEnd(FrameStats.PHASE_FRAME_RENDER);
    }

    private void handleError(ErrorHandler errorHandler, String error) {
        RuntimeException cause = new RuntimeException("JavaScript error\n\n" + error);
        errorHandler.onError(context, cause);
    }

    @Override
    public GraphicsMode getGraphicsMode() {
        return graphics.getGraphicsMode();
    }

    @Override
    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    @Override
    public void terminate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        if (graphics instanceof HtmlCanvasGraphics) {
            return "HTML5 Canvas renderer";
        } else if (graphics instanceof WebGL) {
            return "WebGL renderer";
        } else if (graphics instanceof PixiGraphics) {
            return "Pixi.js renderer";
        } else if (graphics instanceof ThreeGraphics) {
            return "Three.js renderer";
        } else {
            return "TeaVM renderer";
        }
    }

    public static TeaRenderer withCanvas(DisplayMode displayMode) {
        TeaGraphics graphics = new HtmlCanvasGraphics(displayMode.canvas());
        return new TeaRenderer(displayMode, graphics);
    }

    public static TeaRenderer withWebGL(GraphicsMode graphicsMode, DisplayMode displayMode) {
        TeaGraphics graphics = new WebGL(graphicsMode, displayMode.canvas());
        return new TeaRenderer(displayMode, graphics);
    }

    public static TeaRenderer withPixi(DisplayMode displayMode) {
        TeaGraphics graphics = new PixiGraphics(displayMode.canvas());
        return new TeaRenderer(displayMode, graphics);
    }

    public static TeaRenderer withThree(DisplayMode displayMode) {
        TeaGraphics graphics = new ThreeGraphics(displayMode.canvas());
        return new TeaRenderer(displayMode, graphics);
    }
}
