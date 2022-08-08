//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.teavm.pixi.PixiRenderer;
import nl.colorize.multimedialib.renderer.teavm.three.ThreeRenderer;
import nl.colorize.multimedialib.scene.ErrorHandler;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.StageObserver;
import nl.colorize.multimedialib.scene.StageVisitor;
import nl.colorize.util.LogHelper;
import org.teavm.jso.browser.AnimationFrameCallback;
import org.teavm.jso.browser.Window;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Renderer based on <a href="http://teavm.org">TeaVM</a> that is transpiled to
 * JavaScript and runs in the browser. Rendering graphics can switch between
 * different frameworks, the requested renderer can be indicated during the
 * build or at runtime using a URL parameter.
 */
public class TeaRenderer implements Renderer {

    private WebGraphics graphicsMode;
    private Canvas canvas;
    private TeaInputDevice inputDevice;
    private TeaMediaLoader mediaLoader;
    private SceneContext context;
    private StageVisitor stageVisitor;
    private ErrorHandler errorHandler;

    private int framerate;
    private float frameTime;
    private double lastFrameTimestamp;

    private static final Logger LOGGER = LogHelper.getLogger(TeaRenderer.class);

    public TeaRenderer(DisplayMode displayMode, WebGraphics graphicsMode) {
        this.graphicsMode = graphicsMode;
        this.canvas = displayMode.canvas();
        this.mediaLoader = new TeaMediaLoader();
        this.inputDevice = new TeaInputDevice(canvas, mediaLoader.getPlatformFamily());
        TeaNetworkAccess network = new TeaNetworkAccess();
        context = new SceneContext(displayMode, inputDevice, mediaLoader, network);
        errorHandler = ErrorHandler.DEFAULT;

        this.framerate = displayMode.framerate();
        this.frameTime = 1f / framerate;
        this.lastFrameTimestamp = 0.0;
    }

    @Override
    public void start(Scene initialScene, ErrorHandler errorHandler) {
        LOGGER.info("Using graphics mode " + graphicsMode);

        stageVisitor = switch (graphicsMode) {
            case CANVAS -> new CanvasRenderer(canvas);
            case PIXI -> new PixiRenderer(canvas);
            case THREE -> new ThreeRenderer(canvas);
        };

        if (stageVisitor instanceof StageObserver) {
            context.getStage().addObserver((StageObserver) stageVisitor);
        }

        if (stageVisitor instanceof PixiRenderer) {
            inputDevice.setPixelRatio(1f);
        }

        this.errorHandler = errorHandler;

        context.changeScene(initialScene);

        AnimationFrameCallback callback = this::onAnimationFrame;
        Window.requestAnimationFrame(callback);
    }

    /**
     * Browser animation loop. Because {@code window.requestAnimationFrame}
     * always matches the display refresh rate, the browser and the application
     * might run at a different framerate.
     */
    private void onAnimationFrame(double timestamp) {
        try {
            int canvasWidth = Math.round(Browser.getCanvasWidth());
            int canvasHeight = Math.round(Browser.getCanvasHeight());
            float deltaTime = (float) (timestamp - lastFrameTimestamp) / 1000f;

            if (updateCanvas(canvasWidth, canvasHeight) && (framerate >= 60 || deltaTime >= frameTime)) {
                context.getFrameStats().markFrameStart();
                inputDevice.update(frameTime);
                context.update(frameTime);
                context.getFrameStats().markFrameUpdate();
                context.getStage().visit(stageVisitor);
                context.getFrameStats().markFrameRender();
                lastFrameTimestamp = timestamp;
            }

            AnimationFrameCallback callback = this::onAnimationFrame;
            Window.requestAnimationFrame(callback);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during animation loop", e);
            errorHandler.onError(context, e);
        }
    }

    private boolean updateCanvas(int width, int height) {
        if (width > 0 && height > 0) {
            canvas.resizeScreen(width, height);
            return mediaLoader.checkLoadingProgress();
        } else {
            return false;
        }
    }

    @Override
    public String takeScreenshot() {
        return Browser.takeScreenshot();
    }

    @Override
    public DisplayMode getDisplayMode() {
        return new DisplayMode(canvas, framerate);
    }
}
