//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.RenderConfig;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.teavm.Browser;
import nl.colorize.multimedialib.renderer.teavm.TeaInput;
import nl.colorize.multimedialib.renderer.teavm.TeaMediaLoader;
import nl.colorize.multimedialib.renderer.teavm.TeaNetwork;
import nl.colorize.multimedialib.renderer.teavm.HtmlCanvasRenderer;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneManager;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.util.LogHelper;
import nl.colorize.util.ResourceFile;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;

import java.util.List;
import java.util.Properties;
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
    private static final Logger LOGGER = LogHelper.getLogger(GDXBrowserRenderer.class);

    @Override
    public void start(RenderConfig config, Scene initialScene) {
        this.config = config;
        this.sceneManager = new SceneManager(this, initialScene);

        try {
            createCanvas();
            Browser.getBrowserBridge().prepareAnimationLoop();

            WebApplicationConfiguration webConfig = new WebApplicationConfiguration();
            webConfig.width = 0;
            webConfig.height = 0;
            webConfig.canvasID = CANVAS_ID;
            webConfig.usePhysicalPixels = true;
            webConfig.preloadListener = assetLoader -> assetLoader.loadScript("freetype.js");
            new WebApplication(this, webConfig);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during animation loop", e);
            config.getErrorHandler().onError(this, e);
            throw e;
        }
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

        mediaLoader = new GDXBrowserMediaLoader(new TeaMediaLoader());
        graphics = new GDXGraphics(graphicsMode, canvas, mediaLoader);
        input = new TeaInput(canvas);
        network = new TeaNetwork();

        ((TeaInput) input).bindEventHandlers();
    }

    @Override
    protected void prepareFrame() {
        ((TeaInput) input).reset();
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
     * The {@code gdx-teavm} back-end doesn't support audio yet, so loading
     * and playing audio does not rely on libGDX and instead uses the browser
     * API directly via TeaVM.
     */
    private static class GDXBrowserMediaLoader extends GDXMediaLoader {

        private TeaMediaLoader browserMedia;

        public GDXBrowserMediaLoader(TeaMediaLoader browserMedia) {
            super(browserMedia);
            this.browserMedia = browserMedia;
        }

        @Override
        public Audio loadAudio(ResourceFile file) {
            return browserMedia.loadAudio(file);
        }

        @Override
        public String loadText(ResourceFile file) {
            return browserMedia.loadText(file);
        }

        @Override
        public boolean containsResourceFile(ResourceFile file) {
            return browserMedia.containsResourceFile(file);
        }

        @Override
        public Properties loadApplicationData(String appName) {
            return browserMedia.loadApplicationData(appName);
        }

        @Override
        public void saveApplicationData(String appName, Properties data) {
            browserMedia.saveApplicationData(appName, data);
        }
    }
}
