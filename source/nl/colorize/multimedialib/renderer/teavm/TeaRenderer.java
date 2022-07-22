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
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.StageObserver;
import nl.colorize.multimedialib.scene.StageVisitor;
import nl.colorize.util.LogHelper;
import org.teavm.jso.browser.AnimationFrameCallback;
import org.teavm.jso.browser.Window;

import java.util.logging.Logger;

/**
 * Renderer based on <a href="http://teavm.org">TeaVM</a> that is transpiled to
 * JavaScript and runs in the browser. Rendering graphics can switch between
 * different frameworks, the requested renderer can be indicated during the
 * build or at runtime using a URL parameter.
 */
public class TeaRenderer implements Renderer {

    private Canvas canvas;
    private TeaInputDevice inputDevice;
    private TeaMediaLoader mediaLoader;
    private SceneContext context;
    private StageVisitor stageVisitor;

    private int framerate;
    private float frameTime;
    private double lastFrameTimestamp;

    private static final Logger LOGGER = LogHelper.getLogger(TeaRenderer.class);

    public TeaRenderer(DisplayMode displayMode) {
        this.canvas = displayMode.getCanvas();
        this.mediaLoader = new TeaMediaLoader();
        this.inputDevice = new TeaInputDevice(canvas, mediaLoader.getPlatformFamily());
        TeaNetworkAccess network = new TeaNetworkAccess();
        context = new SceneContext(displayMode, inputDevice, mediaLoader, network);

        this.framerate = displayMode.getFramerate();
        this.frameTime = 1f / framerate;
        this.lastFrameTimestamp = 0.0;
    }

    @Override
    public void start(Scene initialScene) {
        stageVisitor = createStageRenderer();
        if (stageVisitor instanceof StageObserver) {
            context.getStage().addObserver((StageObserver) stageVisitor);
        }
        if (stageVisitor instanceof PixiRenderer) {
            inputDevice.setPixelRatio(1f);
        }

        context.changeScene(initialScene);

        AnimationFrameCallback callback = this::onAnimationFrame;
        Window.requestAnimationFrame(callback);
    }

    private StageVisitor createStageRenderer() {
        String requestedRenderer = Browser.getRequestedRenderer();

        if (requestedRenderer.contains("canvas")) {
            LOGGER.info("Using HTML canvas renderer");
            return new CanvasRenderer(canvas);
        } else if (requestedRenderer.contains("pixi")) {
            LOGGER.info("Using PixiJS renderer");
            return new PixiRenderer(canvas);
        } else if (requestedRenderer.contains("three")) {
            LOGGER.info("Using three.js renderer");
            return new ThreeRenderer(canvas);
        } else {
            LOGGER.warning("Requested unknown renderer '" + requestedRenderer + "'");
            return new CanvasRenderer(canvas);
        }
    }

    /**
     * Browser animation loop. Because {@code window.requestAnimationFrame}
     * always matches the display refresh rate, the browser and the application
     * might run at a different framerate.
     */
    private void onAnimationFrame(double timestamp) {
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
