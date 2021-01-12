//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.utils.ScreenUtils;
import nl.colorize.multimedialib.renderer.ApplicationData;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.NestedRenderCallback;
import nl.colorize.multimedialib.renderer.NetworkAccess;
import nl.colorize.multimedialib.renderer.RenderCallback;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.Stage;
import nl.colorize.multimedialib.renderer.java2d.StandardApplicationData;
import nl.colorize.multimedialib.renderer.java2d.StandardNetworkAccess;
import nl.colorize.multimedialib.renderer.java2d.WindowOptions;
import nl.colorize.util.LoadUtils;
import nl.colorize.util.Platform;
import nl.colorize.util.PlatformFamily;
import nl.colorize.util.swing.Utils2D;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.zip.Deflater;

/**
 * Renderer built on top of the libGDX framework. In turn, libGDX supports multiple
 * back-end implementations that determine which platforms are supported and which
 * libraries are used.
 */
public class GDXRenderer implements Renderer, ApplicationListener {

    private GDXBackend backend;
    private Canvas canvas;
    private NestedRenderCallback callbacks;

    private GDXStage stage;
    private GDXInput input;
    private GDXMediaLoader mediaLoader;

    private GDXGraphics2D graphicsContext;
    private ModelBatch modelBatch;
    private boolean freeCamera;
    private CameraInputController freeCameraController;

    public GDXRenderer(Canvas canvas, GDXBackend backend) {
        this.backend = backend;
        this.canvas = canvas;
        this.callbacks = new NestedRenderCallback();

        this.freeCamera = false;
    }

    @Deprecated
    public GDXRenderer(Canvas canvas, int framerate, WindowOptions options) {
        this(canvas, new LWJGLBackend(framerate, options));
    }

    @Override
    public void attach(RenderCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void start() {
        backend.start(this, canvas);
    }

    @Override
    public void create() {
        mediaLoader = new GDXMediaLoader();
        stage = new GDXStage(mediaLoader);
        input = new GDXInput(canvas);

        resize(getCanvas().getWidth(), getCanvas().getHeight());
        modelBatch = new ModelBatch();
        graphicsContext = new GDXGraphics2D(canvas, mediaLoader);

        if (freeCamera) {
            freeCameraController = new CameraInputController(stage.getCamera());
            Gdx.input.setInputProcessor(freeCameraController);
        }
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        graphicsContext.dispose();
        mediaLoader.dispose();
    }

    @Override
    public void resize(int width, int height) {
        getCanvas().resizeScreen(width, height);
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

        float frameTime = 1f / backend.getFramerate();
        input.update(frameTime);
        callbacks.update(this, frameTime);
        stage.update(frameTime);
        if (freeCamera) {
            freeCameraController.update();
        }

        modelBatch.begin(stage.getCamera());
        modelBatch.render(stage.getModelDisplayList(), stage.getEnvironment());
        modelBatch.end();

        callbacks.render(this, graphicsContext);
        graphicsContext.switchMode(false, false);
    }

    @Override
    public GraphicsMode getSupportedGraphicsMode() {
        return GraphicsMode.ALL;
    }

    @Override
    public Canvas getCanvas() {
        return canvas;
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public InputDevice getInputDevice() {
        return input;
    }

    @Override
    public MediaLoader getMediaLoader() {
        return mediaLoader;
    }

    @Override
    public ApplicationData getApplicationData(String appName) {
        if (Platform.isWindows() || Platform.isMac()) {
            return new StandardApplicationData(appName);
        } else {
            return new GDXApplicationData(appName);
        }
    }

    @Override
    public NetworkAccess getNetwork() {
        return new StandardNetworkAccess();
    }

    @Override
    public String takeScreenshot() {
        Pixmap screenshot = ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight());
        File tempFile = LoadUtils.getTempFile(".png");
        PixmapIO.writePNG(Gdx.files.external(tempFile.getAbsolutePath()), screenshot,
            Deflater.DEFAULT_COMPRESSION, true);
        screenshot.dispose();

        try {
            BufferedImage screenshotImage = Utils2D.loadImage(tempFile);
            return Utils2D.toDataURL(screenshotImage);
        } catch (IOException e) {
            throw new MediaException("Screenshot failed", e);
        }
    }

    @Override
    public PlatformFamily getPlatform() {
        return Platform.getPlatformFamily();
    }

    public void enableFreeCamera() {
        freeCamera = true;
    }
}
