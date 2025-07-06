//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import lombok.Getter;
import nl.colorize.multimedialib.math.Box;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.math.Shape3D;
import nl.colorize.multimedialib.math.Size;
import nl.colorize.multimedialib.math.Sphere;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.RenderConfig;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.RendererException;
import nl.colorize.multimedialib.renderer.java2d.StandardNetwork;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.SceneManager;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;
import nl.colorize.util.TextUtils;
import nl.colorize.util.swing.SwingUtils;
import org.lwjgl.system.Configuration;

import java.awt.Dimension;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;

import static com.badlogic.gdx.Files.FileType.Internal;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Position;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.TextureCoordinates;

/**
 * Renderer built on top of the libGDX framework. In turn, libGDX supports multiple
 * back-end implementations that determine which platforms are supported and which
 * libraries are used.
 */
public class GDXRenderer implements Renderer, SceneContext, ApplicationListener {

    @Getter private RenderConfig config;
    private Scene initialScene;

    private GDXGraphics graphics;
    @Getter private GDXInput input;
    @Getter private GDXMediaLoader mediaLoader;
    @Getter private Network network;
    @Getter private SceneManager sceneManager;
    @Getter private Stage stage;

    private static AtomicBoolean nativeLibrariesLoaded = new AtomicBoolean(false);

    private static final List<NativeLibrary> INTEL_MAC_NATIVE_LIBRARIES = List.of(
        new NativeLibrary("gdx", "libgdx64.dylib"),
        new NativeLibrary("lwjgl", "liblwjgl.dylib"),
        new NativeLibrary("lwjgl_opengl", "liblwjgl_opengl.dylib"),
        new NativeLibrary("lwjgl_stb", "liblwjgl_stb.dylib"),
        new NativeLibrary("glfw", "libglfw.dylib"),
        new NativeLibrary("glfw_async", "libglfw_async.dylib"),
        new NativeLibrary("openal", "libopenal.dylib"),
        new NativeLibrary("gdx-freetype", "libgdx-freetype64.dylib")
    );

    private static final List<NativeLibrary> ARM_MAC_NATIVE_LIBRARIES = List.of(
        new NativeLibrary("gdx", "libgdxarm64.dylib"),
        new NativeLibrary("lwjgl", "liblwjgl.dylib"),
        new NativeLibrary("lwjgl_opengl", "liblwjgl_opengl.dylib"),
        new NativeLibrary("lwjgl_stb", "liblwjgl_stb.dylib"),
        new NativeLibrary("glfw", "libglfw.dylib"),
        new NativeLibrary("glfw_async", "libglfw_async.dylib"),
        new NativeLibrary("openal", "libopenal.dylib"),
        new NativeLibrary("gdx-freetype", "libgdx-freetypearm64.dylib")
    );

    private static final int TEXTURE_FLAGS = Position | Normal | TextureCoordinates;
    private static final int SPHERE_SEGMENTS = 32;
    private static final float HALF_PI = (float) Math.PI / 2f;
    private static final Logger LOGGER = LogHelper.getLogger(GDXRenderer.class);

    public GDXRenderer() {
        if (Platform.isMac()) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        if (Platform.isMacAppStore() && !nativeLibrariesLoaded.get()) {
            try {
                nativeLibrariesLoaded.set(true);
                loadApplicationBundleNativeLibraries();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error while loading libGDX native libraries", e);
            }
        }
    }

    /**
     * Loads the libGDX native libraries from the Mac application bundle.
     * These native libraries are normally extracted from the JAR files,
     * but this is not allowed by the Mac app sandbox.
     * <p>
     * This needs to bypass and rewrite significant chunks of the libGDX
     * and LWJGL initialization logic. The old approach used to be allowed,
     * but newer Mac OS versions have become considerably more strict when
     * it comes to loading native libraries. This will hopefully be
     * addressed in future libGDX versions. For now, this logic is nessacary
     * when submitting MultimediaLib applications to the Mac App Store.
     */
    private void loadApplicationBundleNativeLibraries() throws Exception {
        LOGGER.info("Loading native libraries from Mac application bundle");
        LOGGER.info("Native library path: " + System.getProperty("java.library.path"));

        File nativeLibraryDir = Platform.getNativeLibraryPath().stream()
            .filter(dir -> dir.getName().equalsIgnoreCase("MacOS"))
            .findFirst()
            .orElseThrow(() -> new RendererException("Cannot locate native libraries"));

        // Load LWJGL native libraries.

        Function<String, String> lwjglMapper = lib -> locateNativeLWJGL(lib, nativeLibraryDir);
        Configuration.LIBRARY_PATH.set(nativeLibraryDir.getAbsolutePath());
        Configuration.BUNDLED_LIBRARY_PATH_MAPPER.set(lwjglMapper);
        Configuration.BUNDLED_LIBRARY_NAME_MAPPER.set(lwjglMapper);
        Configuration.GLFW_LIBRARY_NAME.set(lwjglMapper.apply("glfw_async"));

        // Load libGDX native libraries.

        GdxNativesLoader.disableNativesLoading = true;

        for (NativeLibrary lib : getMacNativeLibraries()) {
            SharedLibraryLoader.setLoaded(lib.name);
            File dylib = new File(nativeLibraryDir, lib.fileName);
            LOGGER.info("Loading native library " + lib.name + " from " + dylib.getAbsolutePath());
            System.load(dylib.getAbsolutePath());
        }
    }

    /**
     * Returns the list of native libraries for the current Mac CPU
     * architecture. This detection is based on the CPU architecture
     * used by the Java runtime, which might be different from the
     * native hardware architecture.
     */
    private List<NativeLibrary> getMacNativeLibraries() {
        String cpuArchitecture = System.getProperty("os.arch", "");

        if (cpuArchitecture.contains("x86")) {
            return INTEL_MAC_NATIVE_LIBRARIES;
        } else if (cpuArchitecture.contains("aarch")) {
            return ARM_MAC_NATIVE_LIBRARIES;
        } else {
            throw new RendererException("Unsupported Mac CPU architecture: " + cpuArchitecture);
        }
    }

    private String locateNativeLWJGL(String lib, File nativeLibraryDir) {
        lib = lib.substring(lib.lastIndexOf("/") + 1);
        if (TextUtils.startsWith(lib, List.of("lwjgl", "glfw", "openal"))) {
            lib = TextUtils.addLeading(lib, "lib");
        }
        lib = TextUtils.addTrailing(lib, ".dylib");

        File dylib = new File(nativeLibraryDir, lib);
        LOGGER.info("Loading native LWJGL library " + dylib.getAbsolutePath());
        return dylib.getAbsolutePath();
    }

    @Override
    public void start(RenderConfig config, Scene initialScene) {
        this.config = config;
        this.initialScene = initialScene;

        try {
            Lwjgl3ApplicationConfiguration gdxConfig = configure();
            new Lwjgl3Application(this, gdxConfig);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during animation loop", e);
            config.getErrorHandler().onError(this, e);
        }
    }

    private Lwjgl3ApplicationConfiguration configure() {
        Lwjgl3ApplicationConfiguration gdxConfig = new Lwjgl3ApplicationConfiguration();
        gdxConfig.setIdleFPS(config.getFramerate());
        gdxConfig.setForegroundFPS(config.getFramerate());
        gdxConfig.setHdpiMode(HdpiMode.Logical);
        gdxConfig.setTitle(config.getWindowOptions().getTitle());
        gdxConfig.setWindowIcon(Internal, config.getWindowOptions().getIconFile().path());
        if (config.getWindowOptions().isFullscreen()) {
            //     see https://github.com/libgdx/libgdx/issues/6896
            //     Also note this extension will be retired altogether
            //     in https://github.com/libgdx/libgdx/pull/7361
            Dimension screen = SwingUtils.getScreenSize();
            gdxConfig.setDecorated(true);
            gdxConfig.setWindowedMode(screen.width, screen.height);
            gdxConfig.setMaximized(true);
        } else {
            gdxConfig.setDecorated(true);
            gdxConfig.setWindowedMode(getWindowSize().width(), getWindowSize().height());
        }
        return gdxConfig;
    }

    private Size getWindowSize() {
        Size windowSize = config.getWindowOptions().getWindowSize()
            .orElse(config.getCanvas().getSize());
        if (Platform.isWindows()) {
            float uiScale = SwingUtils.getDesktopScaleFactor();
            windowSize = windowSize.multiply(uiScale);
        }
        return new Size(windowSize.width(), windowSize.height());
    }

    @Override
    public void create() {
        input = new GDXInput(config);
        mediaLoader = new GDXMediaLoader();
        graphics = new GDXGraphics(config.getGraphicsMode(), config.getCanvas(), mediaLoader);
        network = new StandardNetwork();
        sceneManager = new SceneManager();
        stage = new Stage(config.getGraphicsMode(), config.getCanvas());

        resize(config.getCanvas().getWidth(), config.getCanvas().getHeight());
        changeScene(initialScene);
    }

    @Override
    public void dispose() {
        graphics.dispose();
        mediaLoader.dispose();

        // Hard quit because libGDX otherwise takes several
        // seconds to close the application.
        System.exit(0);
    }

    @Override
    public void resize(int width, int height) {
        config.getCanvas().resizeScreen(width, height);
        HdpiUtils.glViewport(0, 0, width, height);
        graphics.restartBatch();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void render() {
        sceneManager.requestFrameUpdate(this);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        getFrameStats().markStart(FrameStats.PHASE_FRAME_RENDER);
        getStage().visit(graphics);
        getFrameStats().markEnd(FrameStats.PHASE_FRAME_RENDER);
    }

    @Override
    public void terminate() {
        System.exit(0);
    }

    @Override
    public Mesh createMesh(Shape3D shape, ColorRGB color) {
        Material material = createMaterial(color);
        Model model = buildModel(shape, material);
        return new GDXModel(model);
    }

    private Model buildModel(Shape3D shape, Material material) {
        if (shape instanceof Box box) {
            ModelBuilder modelBuilder = new ModelBuilder();
            // Need to manipulate the box created by ModelBuilder so we end
            // up with the same texture coordinates as used by other renderers.
            Model boxModel = modelBuilder.createBox(box.depth(), box.width(), box.height(),
                material, TEXTURE_FLAGS);
            Quaternion quaternionY = new Quaternion().setFromAxis(0, 1, 0, 90);
            Quaternion quaternionZ = new Quaternion().setFromAxis(1, 0, 0, 90);
            boxModel.nodes.get(0).rotation.set(quaternionY.mul(quaternionZ));
            return boxModel;
        } else if (shape instanceof Sphere sphere) {
            ModelBuilder modelBuilder = new ModelBuilder();
            float diameter = sphere.radius() * 2f;
            return modelBuilder.createSphere(diameter, diameter, diameter,
                SPHERE_SEGMENTS, SPHERE_SEGMENTS, material, TEXTURE_FLAGS);
        } else {
            throw new IllegalArgumentException("Unknown shape: " + shape.getClass());
        }
    }

    private Material createMaterial(ColorRGB color) {
        ColorAttribute colorAttr = ColorAttribute.createDiffuse(GDXMediaLoader.toColor(color));
        return new Material(colorAttr);
    }

    @Override
    public Point2D project(Point3D position) {
        Vector3 positionVector = new Vector3(position.x(), position.y(), position.z());
        Vector3 screenPosition = graphics.camera.project(positionVector);
        return new Point2D(
            getCanvas().toCanvasX(Math.round(screenPosition.x)),
            getCanvas().toCanvasY(Gdx.graphics.getHeight() - Math.round(screenPosition.y))
        );
    }

    @Override
    public boolean castPickRay(Point2D canvasPosition, Box area) {
        float screenX = getCanvas().toScreenX(canvasPosition.x());
        float screenY = getCanvas().toScreenY(canvasPosition.y());

        BoundingBox boundingBox = new BoundingBox(
            new Vector3(area.x(), area.y(), area.z()),
            new Vector3(area.getEndX(), area.getEndY(), area.getEndZ())
        );

        Ray pickRay = graphics.camera.getPickRay(screenX, screenY);
        Vector3 intersection = new Vector3();
        return Intersector.intersectRayBounds(pickRay, boundingBox, intersection);
    }

    @Override
    public void takeScreenshot(File screenshotFile) {
        FileHandle file = Gdx.files.absolute(screenshotFile.getAbsolutePath());
        Pixmap screenshot = Pixmap.createFromFrameBuffer(0, 0,
            Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        PixmapIO.writePNG(file, screenshot, Deflater.DEFAULT_COMPRESSION, true);
        screenshot.dispose();
    }

    @Override
    public boolean isSupported(GraphicsMode graphicsMode) {
        return graphicsMode == GraphicsMode.MODE_2D || graphicsMode == GraphicsMode.MODE_3D;
    }

    @Override
    public String getRendererName() {
        return "libGDX renderer";
    }

    /**
     * Native library used by libGDX. These are normally managed by libGDX
     * itself, but when running from a Mac application bundle a different
     * mechanism is used. See {@link #loadApplicationBundleNativeLibraries()}.
     */
    private record NativeLibrary(String name, String fileName) {
    }
}
