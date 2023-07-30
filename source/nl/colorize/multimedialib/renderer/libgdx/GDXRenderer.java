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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
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

import java.awt.GraphicsEnvironment;
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

        int windowWidth = Math.round(canvas.getWidth() * getDesktopScaleFactor());
        int windowHeight = Math.round(canvas.getHeight() * getDesktopScaleFactor());

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(windowWidth, windowHeight);
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

    /**
     * Returns the platform's user interface scale factor. On Mac, this always
     * returns 1.0 because Mac OS applies a consistent scale factor to the
     * entire desktop. On Windows, desktop resolution/scale factor and UI scale
     * factor are two independent settings.
     * <p>
     * This method returns the UI scale factor for the <em>default</em> screen.
     * This could be different from the screen that is going to display the
     * application, but we don't know this yet because this method is called
     * before the window is even created.
     */
    private float getDesktopScaleFactor() {
        //TODO it is not ideal to depend on AWT and Swing for this,
        //     but since libGDX does not provide this information
        //     and we currently only use the libGDX renderer on
        //     desktop it should be OK for now.
        if (Platform.isWindows()) {
            return (float) GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration()
                .getDefaultTransform().getScaleX();
        } else {
            return 1f;
        }
    }

    @Override
    public void create() {
        resize(canvas.getWidth(), canvas.getHeight());

        input = new GDXInput(canvas);
        mediaLoader = new GDXMediaLoader();
        graphicsContext = new GDXGraphics(canvas);

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
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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
