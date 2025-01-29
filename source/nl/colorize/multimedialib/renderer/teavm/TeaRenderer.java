//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import lombok.Getter;
import nl.colorize.multimedialib.math.Box;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.math.Shape3D;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.RenderConfig;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.SceneManager;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.multimedialib.stage.Stage;
import org.teavm.jso.browser.Window;

import java.io.File;

/**
 * Renderer based on <a href="http://teavm.org">TeaVM</a> that is transpiled to
 * JavaScript and runs in the browser. Rendering graphics can switch between
 * different frameworks, the requested renderer can be indicated during the
 * build or at runtime using a URL parameter.
 */
@Getter
public class TeaRenderer implements Renderer, SceneContext {

    private RenderConfig config;
    private TeaGraphics graphics;
    private TeaInputDevice input;
    private TeaMediaLoader mediaLoader;
    private TeaNetwork network;
    private SceneManager sceneManager;
    private Stage stage;

    /**
     * Initializes the TeaVM renderer using the specified graphics library.
     * Applications should use one of the factory methods rather than using
     * this constructor directly.
     */
    public TeaRenderer(TeaGraphics graphics) {
        this.graphics = graphics;
    }

    @Override
    public void start(RenderConfig config, Scene initialScene) {
        this.config = config;
        network = new TeaNetwork();
        sceneManager = new SceneManager();
        stage = new Stage(config.getGraphicsMode(), config.getCanvas());
        mediaLoader = new TeaMediaLoader(config.getGraphicsMode());

        input = new TeaInputDevice(config.getCanvas(), graphics);
        input.bindEventHandlers();

        graphics.init(this);
        sceneManager.changeScene(initialScene);

        Browser.getBrowserBridge().prepareAnimationLoop();
        Browser.getBrowserBridge().registerErrorHandler(this::handleError);
        Window.requestAnimationFrame(this::onAnimationFrame);
    }

    /**
     * Callback function for the browser animation loop that is called using
     * {@code requestAnimationFrame}. If the application canvas is not ready,
     * this will skip frame logic and instead render an "empty" frame.
     */
    private void onAnimationFrame(double timestamp) {
        if (prepareCanvas()) {
            if (sceneManager.requestFrameUpdate(this) > 0) {
                sceneManager.getFrameStats().markStart(FrameStats.PHASE_FRAME_RENDER);
                getStage().visit(graphics);
                sceneManager.getFrameStats().markEnd(FrameStats.PHASE_FRAME_RENDER);
                input.reset();
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
            getCanvas().resizeScreen(width, height);
            return true;
        } else {
            return false;
        }
    }

    private void handleError(String error) {
        RuntimeException cause = new RuntimeException("JavaScript error\n\n" + error);
        config.getErrorHandler().onError(this, cause);
    }

    @Override
    public void terminate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mesh createMesh(Shape3D shape, ColorRGB color) {
        return graphics.createMesh(shape, color);
    }

    @Override
    public Point2D project(Point3D position) {
        return graphics.project(position);
    }

    @Override
    public boolean castPickRay(Point2D canvasPosition, Box area) {
        return graphics.castPickRay(canvasPosition, area);
    }

    @Override
    public void takeScreenshot(File screenshotFile) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRendererName() {
        return switch (graphics) {
            case HtmlCanvasGraphics canvas -> "HTML5 Canvas renderer";
            case PixiGraphics pixi -> "Pixi.js renderer";
            case ThreeGraphics three -> "Three.js renderer";
            default -> "TeaVM renderer";
        };
    }

    @Override
    public boolean isSupported(GraphicsMode graphicsMode) {
        return graphics.getGraphicsMode() == graphicsMode;
    }
}
