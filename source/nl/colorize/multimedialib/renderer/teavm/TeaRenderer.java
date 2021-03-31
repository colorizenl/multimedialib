//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.graphics.PolygonModel;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.Stage;
import nl.colorize.util.PlatformFamily;

/**
 * Renderer based on TeaVM (http://teavm.org) that is transpiled to JavaScript
 * and runs in the browser.
 */
public class TeaRenderer implements Renderer {

    private Canvas canvas;
    private TeaGraphicsContext2D graphics;
    private TeaInputDevice inputDevice;
    private TeaMediaLoader mediaLoader;
    private SceneContext context;

    public TeaRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.graphics = new TeaGraphicsContext2D(canvas);
        this.inputDevice = new TeaInputDevice(canvas, getPlatform());
        this.mediaLoader = new TeaMediaLoader();
        TeaNetworkAccess network = new TeaNetworkAccess();
        context = new SceneContext(canvas, inputDevice, mediaLoader, network);
    }

    @Override
    public void start(Scene initialScene) {
        context.changeScene(initialScene);
        Browser.startAnimationLoop(this::onFrame);
    }

    @Override
    public GraphicsMode getGraphicsMode() {
        if (Browser.getRendererType().equals("three")) {
            return GraphicsMode.MODE_3D;
        } else {
            return GraphicsMode.MODE_2D;
        }
    }

    private void onFrame(float deltaTime, boolean render) {
        updateCanvas();
        inputDevice.update(deltaTime);

        if (isReady()) {
            context.update(deltaTime);

            if (render) {
                renderStage(context.getStage(), deltaTime);
            }
        }
    }

    private void renderStage(Stage stage, float deltaTime) {
        if (getGraphicsMode() == GraphicsMode.MODE_3D) {
            Point3D camera = stage.getCameraPosition();
            Point3D cameraDirection = stage.getCameraTarget();
            Browser.moveCamera(camera.getX(), camera.getY(), camera.getZ(),
                cameraDirection.getX(), cameraDirection.getY(), cameraDirection.getZ());

            Browser.changeAmbientLight(stage.getAmbientLight().toHex());
            Browser.changeLight(stage.getAmbientLight().toHex());

            for (PolygonModel model : stage.getModels()) {
                model.update(deltaTime);
            }
        }

        stage.render2D(graphics);
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

    @Override
    public String takeScreenshot() {
        return Browser.takeScreenshot();
    }

    /**
     * Returns the underlying platform. This will not return a generic
     * "browser" or "web" value, but instead return the platform that is
     * running the browser. The detection is based on the browser's User-Agent
     * header.
     */
    public static PlatformFamily getPlatform() {
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
}
