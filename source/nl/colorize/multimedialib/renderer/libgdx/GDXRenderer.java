//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.ErrorHandler;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.RenderCapabilities;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.WindowOptions;
import nl.colorize.multimedialib.renderer.java2d.StandardNetwork;
import nl.colorize.multimedialib.scene.RenderContext;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.util.LogHelper;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Renderer built on top of the libGDX framework. In turn, libGDX supports multiple
 * back-end implementations that determine which platforms are supported and which
 * libraries are used.
 */
public class GDXRenderer implements Renderer, ApplicationListener {

    private GraphicsMode graphicsMode;
    private Canvas canvas;
    private int framerate;
    private WindowOptions window;

    private GDXInput input;
    private GDXMediaLoader mediaLoader;
    private SceneContext context;
    private Scene initialScene;

    private GDXGraphics graphicsContext;

    private static final Logger LOGGER = LogHelper.getLogger(GDXRenderer.class);

    public GDXRenderer(GraphicsMode graphicsMode, DisplayMode displayMode, WindowOptions window) {
        this.graphicsMode = graphicsMode;
        this.canvas = displayMode.canvas();
        this.framerate = displayMode.framerate();
        this.window = window;
    }

    @Override
    public void start(Scene initialScene, ErrorHandler errorHandler) {
        this.initialScene = initialScene;

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(canvas.getWidth(), canvas.getHeight());
        config.setDecorated(true);
        config.setIdleFPS(framerate);
        config.setForegroundFPS(framerate);
        config.setHdpiMode(HdpiMode.Pixels);
        config.setTitle(window.title());
        if (window.iconFile() != null) {
            config.setWindowIcon(Files.FileType.Internal, window.iconFile().path());
        }

        try {
            new Lwjgl3Application(this, config);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during animation loop", e);
            errorHandler.onError(context, e);
        }
    }

    @Override
    public void create() {
        mediaLoader = new GDXMediaLoader();
        input = new GDXInput(canvas);

        resize(canvas.getWidth(), canvas.getHeight());
        graphicsContext = new GDXGraphics(canvas);

        context = new RenderContext(this);
        context.changeScene(initialScene);
    }

    @Override
    public void dispose() {
        graphicsContext.dispose();
        mediaLoader.dispose();
    }

    @Override
    public void resize(int width, int height) {
        canvas.resizeScreen(width, height);
    }

    @Override
    public void pause() {
        //TODO
    }

    @Override
    public void resume() {
        //TODO
    }

    @Override
    public void render() {
        context.getFrameStats().markFrameStart();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        float frameTime = 1f / framerate;
        input.update(frameTime);

        context.update(frameTime);
        context.getFrameStats().markFrameUpdate();

        renderStage(context.getStage());
        graphicsContext.switchMode(false, false);
        context.getFrameStats().markFrameRender();
    }

    private void renderStage(Stage stage) {
        if (graphicsMode == GraphicsMode.MODE_3D) {
            graphicsContext.render3D(stage.getLayer3D());
        }

        stage.visit(graphicsContext);
    }

    @Override
    public RenderCapabilities getCapabilities() {
        DisplayMode displayMode = new DisplayMode(canvas, framerate);
        Network network = new StandardNetwork();
        return new RenderCapabilities(graphicsMode, displayMode,
            graphicsContext, input, mediaLoader, network);
    }
}
