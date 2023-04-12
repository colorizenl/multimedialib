//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.ErrorHandler;
import nl.colorize.multimedialib.renderer.FrameSync;
import nl.colorize.multimedialib.renderer.RenderCapabilities;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.scene.RenderContext;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.stage.StageObserver;
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
    private TeaMediaLoader mediaLoader;
    private SceneContext context;
    private FrameSync frameSync;

    public TeaRenderer(DisplayMode displayMode, TeaGraphics graphics) {
        this.displayMode = displayMode;
        this.graphics = graphics;
        this.mediaLoader = new TeaMediaLoader(graphics);
        this.inputDevice = new TeaInputDevice(displayMode.canvas(), graphics);
        this.frameSync = new FrameSync(displayMode);
    }

    @Override
    public void start(Scene initialScene, ErrorHandler errorHandler) {
        context = new RenderContext(this);
        context.changeScene(initialScene);

        graphics.init();
        if (graphics instanceof StageObserver observer) {
            context.getStage().getObservers().add(observer);
        }

        inputDevice.bindEventHandlers();

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
            frameSync.requestFrame((long) timestamp, deltaTime -> {
                context.getFrameStats().markFrameStart();
                context.update(deltaTime);
                context.getFrameStats().markFrameUpdate();
                context.getStage().visit(graphics);
                context.getFrameStats().markFrameRender();

                inputDevice.reset();
            });
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

    @Override
    public RenderCapabilities getCapabilities() {
        TeaNetwork network = new TeaNetwork();
        return new RenderCapabilities(graphics.getGraphicsMode(), displayMode,
            graphics, inputDevice, mediaLoader, network);
    }

    private void handleError(ErrorHandler errorHandler, String error) {
        RuntimeException cause = new RuntimeException("JavaScript error\n\n" + error);
        errorHandler.onError(context, cause);
    }
}
