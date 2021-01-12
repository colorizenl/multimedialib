//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.renderer.ApplicationData;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.NetworkAccess;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.NestedRenderCallback;
import nl.colorize.multimedialib.renderer.RenderCallback;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.Stage;
import nl.colorize.util.PlatformFamily;

/**
 * Renderer based on TeaVM (http://teavm.org) that is transpiled to JavaScript
 * and runs in the browser.
 */
public class TeaRenderer implements Renderer {

    private NestedRenderCallback callbacks;
    private Canvas canvas;
    private TeaGraphicsContext2D graphics;
    private TeaStage stage;
    private TeaInputDevice inputDevice;
    private TeaMediaLoader mediaLoader;

    public TeaRenderer(Canvas canvas) {
        this.callbacks = new NestedRenderCallback();
        this.canvas = canvas;
        this.graphics = new TeaGraphicsContext2D(canvas);
        this.inputDevice = new TeaInputDevice(canvas, getPlatform());
        this.mediaLoader = new TeaMediaLoader();

        if (getSupportedGraphicsMode() == GraphicsMode.ALL) {
            stage = new TeaStage();
        }
    }

    @Override
    public void attach(RenderCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void start() {
        Browser.startAnimationLoop(this::onFrame);
    }

    @Override
    public GraphicsMode getSupportedGraphicsMode() {
        if (Browser.getRendererType().equals("three")) {
            return GraphicsMode.ALL;
        } else {
            return GraphicsMode.G2D;
        }
    }

    @Override
    public Canvas getCanvas() {
        return canvas;
    }

    @Override
    public Stage getStage() {
        Preconditions.checkState(stage != null, "Support for 3D graphics is not enabled");
        return stage;
    }

    @Override
    public InputDevice getInputDevice() {
        return inputDevice;
    }

    @Override
    public MediaLoader getMediaLoader() {
        return mediaLoader;
    }

    @Override
    public ApplicationData getApplicationData(String appName) {
        return new TeaLocalStorage();
    }

    @Override
    public NetworkAccess getNetwork() {
        return new TeaNetworkAccess();
    }

    private void onFrame(float deltaTime, boolean render) {
        updateCanvas();
        inputDevice.update(deltaTime);
        if (stage != null) {
            stage.update(deltaTime);
        }

        if (isReady()) {
            callbacks.update(this, deltaTime);

            if (render) {
                callbacks.render(this, graphics);
            }
        }
    }

    private boolean isReady() {
        int canvasWidth = Math.round(Browser.getCanvasWidth());
        int canvasHeight = Math.round(Browser.getCanvasHeight());

        return canvasWidth > 0 && canvasHeight > 0 && mediaLoader.checkLoadingProgress();
    }

    private void updateCanvas() {
        int canvasWidth = Math.round(Browser.getCanvasWidth());
        int canvasHeight = Math.round(Browser.getCanvasHeight());

        if (canvasWidth > 0 && canvasHeight > 0) {
            canvas.resizeScreen(canvasWidth, canvasHeight);
        }
    }

    /**
     * Returns the display name of the current platform. This method is similar
     * to {@code Platform.getPlatformName()}, but detects the platform based on
     * the browser's {@code User-Agent} header rather than from the system
     * properties.
     */
    @Override
    public PlatformFamily getPlatform() {
        String userAgent = Browser.getUserAgent().toLowerCase();

        if (userAgent.contains("iphone") || userAgent.contains("ipad")) {
            return PlatformFamily.IOS;
        } else if (userAgent.contains("android")) {
            return PlatformFamily.ANDROID;
        } else if (userAgent.contains("mac")) {
            return PlatformFamily.MAC;
        } else {
            return PlatformFamily.WINDOWS;
        }
    }

    @Override
    public String takeScreenshot() {
        return Browser.takeScreenshot();
    }
}
