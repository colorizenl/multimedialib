//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.ErrorHandler;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.WindowOptions;
import nl.colorize.multimedialib.renderer.java2d.StandardNetwork;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.StageVisitor;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;
import nl.colorize.util.Stopwatch;
import nl.colorize.util.swing.SwingUtils;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;

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

    private GDXGraphics graphicsContext;
    private GDXInput input;
    private GDXMediaLoader mediaLoader;
    private SceneContext context;
    private Scene initialScene;
    private List<FileHandle> requestedScreenshots;

    private static final Logger LOGGER = LogHelper.getLogger(GDXRenderer.class);

    public GDXRenderer(GraphicsMode graphicsMode, DisplayMode displayMode, WindowOptions window) {
        this.graphicsMode = graphicsMode;
        this.canvas = displayMode.canvas();
        this.framerate = displayMode.framerate();
        this.window = window;

        this.requestedScreenshots = new ArrayList<>();
    }

    @Override
    public void start(Scene initialScene, ErrorHandler errorHandler) {
        this.initialScene = initialScene;

        try {
            Lwjgl3ApplicationConfiguration config = configure();
            new Lwjgl3Application(this, config);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during animation loop", e);
            errorHandler.onError(context, e);
        }
    }

    private Lwjgl3ApplicationConfiguration configure() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setIdleFPS(framerate);
        config.setForegroundFPS(framerate);
        config.setHdpiMode(HdpiMode.Logical);
        config.setTitle(window.getTitle());
        config.setWindowIcon(Files.FileType.Internal, window.getIconFile().path());
        config.setDecorated(true);
        configureDisplayMode(config);
        return config;
    }

    private void configureDisplayMode(Lwjgl3ApplicationConfiguration config) {
        if (window.isFullscreen()) {
            configureFullScreen(config);
        } else {
            configureWindow(config);
        }
    }

    private void configureFullScreen(Lwjgl3ApplicationConfiguration config) {
        if (Platform.isMac()) {
            // There is an issue in LWJGL that causes the application
            // to crash using LWJGL's own fullscreen display mode.
            Dimension screen = SwingUtils.getScreenSize();
            config.setDecorated(false);
            config.setWindowedMode(screen.width, screen.height);
            config.setMaximized(true);
        } else {
            config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        }
    }

    private void configureWindow(Lwjgl3ApplicationConfiguration config) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        if (Platform.isWindows()) {
            float uiScale = SwingUtils.getDesktopScaleFactor();
            config.setWindowedMode(Math.round(width * uiScale), Math.round(height * uiScale));
        } else {
            config.setWindowedMode(width, height);
        }
    }

    @Override
    public void create() {
        input = new GDXInput(canvas);
        mediaLoader = new GDXMediaLoader();
        graphicsContext = new GDXGraphics(canvas);

        resize(canvas.getWidth(), canvas.getHeight());

        context = new SceneContext(this, new Stopwatch());
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
        HdpiUtils.glViewport(0, 0, width, height);
        graphicsContext.restartBatch();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        context.syncFrame();
        renderStage(context.getStage());
        graphicsContext.switchMode(false, false);

        renderRequestedScreenshots();
    }

    private void renderStage(Stage stage) {
        context.getFrameStats().markStart(FrameStats.PHASE_FRAME_RENDER);

        if (graphicsMode == GraphicsMode.MODE_3D) {
            graphicsContext.render3D(stage.getWorld());
        }

        stage.visit(graphicsContext);

        context.getFrameStats().markEnd(FrameStats.PHASE_FRAME_RENDER);
    }

    @Override
    public GraphicsMode getGraphicsMode() {
        return graphicsMode;
    }

    @Override
    public DisplayMode getDisplayMode() {
        return new DisplayMode(canvas, framerate);
    }

    @Override
    public StageVisitor getGraphics() {
        return graphicsContext;
    }

    @Override
    public InputDevice getInput() {
        return input;
    }

    @Override
    public MediaLoader getMediaLoader() {
        return mediaLoader;
    }

    @Override
    public Network getNetwork() {
        return new StandardNetwork();
    }

    @Override
    public void takeScreenshot(File outputFile) {
        requestedScreenshots.add(new FileHandle(outputFile));
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        File workDir = Platform.getUserWorkingDirectory();
        return new File(workDir, "build.gradle").exists();
    }

    /**
     * Renders all screenshots that were requested during the frame update.
     * This is not done immediately in {@link #takeScreenshot(File)}, as that
     * would result in an empty or partial frame. Screenshots are therefore
     * delayed until the entire frame has been rendered.
     */
    private void renderRequestedScreenshots() {
        if (requestedScreenshots.isEmpty()) {
            return;
        }

        int width = Gdx.graphics.getBackBufferWidth();
        int height = Gdx.graphics.getBackBufferHeight();
        Pixmap pixels = Pixmap.createFromFrameBuffer(0, 0, width, height);

        for (FileHandle outputFile : requestedScreenshots) {
            PixmapIO.writePNG(outputFile, pixels, Deflater.DEFAULT_COMPRESSION, true);
        }

        pixels.dispose();
        requestedScreenshots.clear();
    }

    @Override
    public void terminate() {
        System.exit(0);
    }
}
