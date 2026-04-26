//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import lombok.Getter;
import nl.colorize.multimedialib.math.Size;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.RenderConfig;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.SceneManager;
import org.teavm.jso.browser.Window;

import java.util.List;

/**
 * Renderer based on <a href="http://teavm.org">TeaVM</a> that is transpiled to
 * JavaScript and runs in the browser. Rendering graphics can switch between
 * different frameworks, the requested renderer can be indicated during the
 * build or at runtime using a URL parameter.
 */
@Getter
public class HtmlCanvasRenderer implements Renderer, SceneContext {

    private RenderConfig config;
    private TeaInput input;
    private TeaMediaLoader mediaLoader;
    private HtmlCanvasGraphics graphics;
    private TeaNetwork network;
    private SceneManager sceneManager;

    public static final String CONTAINER_ID = "multimediaLibContainer";

    /**
     * Starts the renderer by initializing all media files. The animation loop
     * will be started afterwards, but only once all media files have been
     * loaded.
     */
    @Override
    public void start(RenderConfig config, Scene initialScene) {
        this.config = config;
        network = new TeaNetwork();
        sceneManager = new SceneManager(this, initialScene);

        Browser.getBrowserBridge().registerErrorHandler(this::handleError);

        mediaLoader = new TeaMediaLoader();
        mediaLoader.preload().subscribe(_ -> startAnimationLoop());
    }

    /**
     * Initializes the canvas and input, then starts the animation loop that
     * processes input and graphics every frame. The animation will only
     * start when all media files have been preloaded.
     */
    private void startAnimationLoop() {
        graphics = new HtmlCanvasGraphics(config.getCanvas(), mediaLoader);
        graphics.prepareCanvas();

        input = new TeaInput(config.getCanvas());
        input.bindEventHandlers();

        Browser.getBrowserBridge().prepareAnimationLoop();
        Window.requestAnimationFrame(this::onAnimationFrame);
    }

    /**
     * Callback function for the browser animation loop that is called using
     * {@code requestAnimationFrame}. If the application canvas is not ready,
     * this will skip frame logic and instead render an "empty" frame.
     */
    private void onAnimationFrame(double timestamp) {
        if (prepareCanvas()) {
            if (sceneManager.requestFrameUpdate() > 0) {
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
        Size display = graphics.getDisplaySize();

        if (display.width() > 0 || display.height() > 0) {
            getCanvas().resizeScreen(display.width(), display.height());
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
    public String getDisplayName() {
        return "HTML Canvas renderer";
    }

    @Override
    public List<GraphicsMode> getSupportedGraphicsModes() {
        return List.of(GraphicsMode.MODE_2D);
    }
}
