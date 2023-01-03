//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.ErrorHandler;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.pixi.PixiGraphics;
import nl.colorize.multimedialib.renderer.three.ThreeGraphics;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.stage.StageObserver;
import nl.colorize.multimedialib.stage.StageVisitor;
import org.teavm.jso.browser.AnimationFrameCallback;
import org.teavm.jso.browser.Window;

/**
 * Renderer based on <a href="http://teavm.org">TeaVM</a> that is transpiled to
 * JavaScript and runs in the browser. Rendering graphics can switch between
 * different frameworks, the requested renderer can be indicated during the
 * build or at runtime using a URL parameter.
 */
public class TeaRenderer implements Renderer {

    private Canvas canvas;
    private StageVisitor graphics;
    private TeaInputDevice inputDevice;
    private TeaMediaLoader mediaLoader;
    private SceneContext context;
    private ErrorHandler errorHandler;

    private int framerate;
    private float frameTime;
    private double lastFrameTimestamp;

    public TeaRenderer(DisplayMode displayMode, StageVisitor graphics) {
        this.canvas = displayMode.canvas();
        this.graphics = graphics;
        this.mediaLoader = new TeaMediaLoader(graphics);
        this.inputDevice = new TeaInputDevice(canvas, mediaLoader.getPlatformFamily());
        errorHandler = ErrorHandler.DEFAULT;

        this.framerate = displayMode.framerate();
        this.frameTime = 1f / framerate;
        this.lastFrameTimestamp = 0.0;
    }

    @Override
    public void start(Scene initialScene, ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        this.context = new SceneContext(this, initialScene);

        if (graphics instanceof StageObserver observer) {
            context.getStage().getObservers().add(observer);
        }

        if (graphics instanceof PixiGraphics) {
            inputDevice.setPixelRatio(1f);
        }

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
                context.getStage().visit(graphics);
                context.getFrameStats().markFrameRender();
                lastFrameTimestamp = timestamp;
            }

            AnimationFrameCallback callback = this::onAnimationFrame;
            Window.requestAnimationFrame(callback);
        } catch (Exception e) {
            errorHandler.onError(context, e);
            // Need to rethrow the exception to get TeaVM's normal stack trace.
            throw e;
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
    public GraphicsMode getGraphicsMode() {
        if (graphics instanceof ThreeGraphics) {
            return GraphicsMode.MODE_3D;
        } else {
            return GraphicsMode.MODE_2D;
        }
    }

    @Override
    public DisplayMode getDisplayMode() {
        return new DisplayMode(canvas, framerate);
    }

    @Override
    public StageVisitor accessGraphics() {
        return graphics;
    }

    @Override
    public InputDevice accessInputDevice() {
        return inputDevice;
    }

    @Override
    public MediaLoader accessMediaLoader() {
        return mediaLoader;
    }

    @Override
    public Network accessNetwork() {
        return new TeaNetwork();
    }
}
