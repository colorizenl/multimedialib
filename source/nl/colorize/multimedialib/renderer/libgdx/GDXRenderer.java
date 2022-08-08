//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.math.Vector3;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.PolygonModel;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.WindowOptions;
import nl.colorize.multimedialib.renderer.java2d.StandardNetworkAccess;
import nl.colorize.multimedialib.scene.ErrorHandler;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.Stage;
import nl.colorize.multimedialib.scene.Stage3D;
import nl.colorize.util.LoadUtils;
import nl.colorize.util.LogHelper;
import nl.colorize.util.swing.Utils2D;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

    private Canvas canvas;
    private int framerate;
    private WindowOptions window;

    private GDXInput input;
    private GDXMediaLoader mediaLoader;
    private SceneContext context;
    private Scene initialScene;

    private PerspectiveCamera camera;
    private DirectionalLight light;
    private Environment environment;
    private ModelBatch modelBatch;
    private List<ModelInstance> displayList;
    private GDXGraphics2D graphicsContext;

    private static final int FIELD_OF_VIEW = 75;
    private static final float NEAR_PLANE = 1f;
    private static final float FAR_PLANE = 300f;
    private static final Logger LOGGER = LogHelper.getLogger(GDXRenderer.class);

    public GDXRenderer(DisplayMode displayMode, WindowOptions window) {
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
            config.setWindowIcon(Files.FileType.Internal, window.iconFile().getPath());
        }

        try {
            new Lwjgl3Application(this, config);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during animation loop", e);
        }
    }

    @Override
    public void create() {
        mediaLoader = new GDXMediaLoader();
        input = new GDXInput(canvas);
        StandardNetworkAccess network = new StandardNetworkAccess();
        context = new SceneContext(getDisplayMode(), input, mediaLoader, network);
        context.changeScene(initialScene);

        resize(canvas.getWidth(), canvas.getHeight());
        modelBatch = new ModelBatch();
        displayList = new ArrayList<>();
        graphicsContext = new GDXGraphics2D(canvas);

        initStage();
    }

    private void initStage() {
        camera = new PerspectiveCamera(FIELD_OF_VIEW, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = NEAR_PLANE;
        camera.far = FAR_PLANE;
        camera.update();

        light = new DirectionalLight();
        environment = new Environment();
        environment.add(light);
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
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
        if (stage instanceof Stage3D stage3D) {
            camera.position.set(toVector(stage3D.getCameraPosition()));
            camera.up.set(0f, 1f, 0f);
            camera.lookAt(toVector(stage3D.getCameraTarget()));
            camera.update();

            environment.set(new ColorAttribute(ColorAttribute.AmbientLight,
                toColor(stage3D.getAmbientLight())));
            light.set(toColor(stage3D.getLightColor()), toVector(stage3D.getLightPosition()));
        }

        updateDisplayList();

        modelBatch.begin(camera);
        modelBatch.render(displayList, environment);
        modelBatch.end();

        stage.visit(graphicsContext);
    }

    private void updateDisplayList() {
        displayList.clear();

        if (context.getStage() instanceof Stage3D stage3D) {
            for (PolygonModel model : stage3D.getModels()) {
                GDXModel gdxModel = (GDXModel) model;
                displayList.add(gdxModel.getInstance());
            }
        }
    }

    private Vector3 toVector(Point3D point) {
        return new Vector3(point.getX(), point.getY(), point.getZ());
    }

    private Color toColor(ColorRGB color) {
        return mediaLoader.toColor(color);
    }

    @Override
    public DisplayMode getDisplayMode() {
        return new DisplayMode(canvas, framerate);
    }

    @Override
    public String takeScreenshot() {
        Pixmap screenshot = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getWidth(),
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
}
