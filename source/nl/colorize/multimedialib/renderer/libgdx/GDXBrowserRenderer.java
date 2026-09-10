//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration;
import com.github.xpenatan.gdx.teavm.backends.web.WebAssetPreloadListener;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetLoader;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetLoaderListener;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.RenderConfig;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.teavm.Browser;
import nl.colorize.multimedialib.renderer.teavm.HtmlCanvasRenderer;
import nl.colorize.multimedialib.renderer.teavm.TeaInput;
import nl.colorize.multimedialib.renderer.teavm.TeaMediaLoader;
import nl.colorize.multimedialib.renderer.teavm.TeaNetwork;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneManager;
import nl.colorize.util.LogHelper;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Uses the {@code gdx-teavm} back-end for libGDX so that it can run in the
 * browser via TeaVM. There are some differences between this renderer and
 * the "normal" libGDX desktop renderer. Browser APIs are used in place of
 * facilities that are not supported when running in TeaVM.
 */
public class GDXBrowserRenderer extends GDXContext implements Renderer {

    private static final String CANVAS_ID = "multimediaLibCanvas";
    private static final boolean SHOW_DOWNLOADED_ASSETS = false;
    private static final Logger LOGGER = LogHelper.getLogger(GDXBrowserRenderer.class);

    @Override
    public void start(RenderConfig config, Scene initialScene) {
        this.config = config;
        this.sceneManager = new SceneManager(config, initialScene);

        try {
            createCanvas();
            Browser.getBrowserBridge().prepareAnimationLoop();

            WebApplicationConfiguration webConfig = createWebAppConfig();
            ApplicationListener preloadListener = new ApplicationAdapter() {};
            new WebApplication(this, preloadListener, webConfig);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during animation loop", e);
            config.getErrorHandler().onError(this, e);
            throw e;
        }
    }

    private WebApplicationConfiguration createWebAppConfig() {
        WebApplicationConfiguration webConfig = new WebApplicationConfiguration();
        webConfig.width = 0;
        webConfig.height = 0;
        webConfig.canvasID = CANVAS_ID;
        webConfig.usePhysicalPixels = true;
        webConfig.preloadListener = new AssetPreloader();
        webConfig.showDownloadLogs = SHOW_DOWNLOADED_ASSETS;
        return webConfig;
    }

    /**
     * Creates a new HTML canvas that will be used by libGDX. The canvas is
     * given a default width and height, since it will be resized by libGDX
     * anyway.
     */
    private void createCanvas() {
        HTMLDocument document = Window.current().getDocument();
        HTMLCanvasElement canvas = (HTMLCanvasElement) document.createElement("canvas");
        canvas.setId(CANVAS_ID);
        canvas.setWidth(getConfig().getCanvas().getWidth());
        canvas.setHeight(getConfig().getCanvas().getHeight());
        document.getElementById(HtmlCanvasRenderer.CONTAINER_ID).appendChild(canvas);
    }

    @Override
    protected void initContext() {
        GraphicsMode graphicsMode = config.getGraphicsMode();
        Canvas canvas = config.getCanvas();

        mediaLoader = new GDXBrowserMediaLoader(new GDXMediaLoader(), new TeaMediaLoader());
        graphics = new GDXGraphics(graphicsMode, canvas);
        input = new TeaInput(canvas);
        network = new TeaNetwork();

        ((TeaInput) input).bindEventHandlers();
    }

    @Override
    protected void prepareFrame() {
        ((TeaInput) input).reset();
    }

    @Override
    public void captureScreenshot(File pngFile) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDisplayName() {
        return "libGDX/TeaVM renderer";
    }

    @Override
    public List<GraphicsMode> getSupportedGraphicsModes() {
        return List.of(GraphicsMode.MODE_2D, GraphicsMode.MODE_3D);
    }

    /**
     * Custom version of the {@code gdx-teavm} asset loading process.
     * MultimediaLib supports multiple renderers, so it cannot fully
     * rely on the "standard" {@code gdx-teavm} build and asset loading
     * process.
     */
    private static class AssetPreloader implements WebAssetPreloadListener, AssetLoaderListener<Void> {

        @Override
        public void onPreload(AssetLoader assetLoader) {
            assetLoader.loadScript("freetype.js");
            assetLoader.preload("preload-assets.txt", this);
        }

        @Override
        public void onSuccess(String url, Void result) {
            WebApplication.get().setPreloadReady();
        }

        @Override
        public void onFailure(String url) {
            LOGGER.severe("Failed to preload resource file: " + url);
        }
    }
}
