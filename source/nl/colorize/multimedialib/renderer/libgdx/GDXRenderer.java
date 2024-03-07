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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.ErrorHandler;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.RendererException;
import nl.colorize.multimedialib.renderer.WindowOptions;
import nl.colorize.multimedialib.renderer.java2d.StandardNetwork;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;
import nl.colorize.util.TextUtils;
import nl.colorize.util.swing.SwingUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.system.Configuration;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
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

    private GDXGraphics graphicsContext;
    private GDXInput input;
    private GDXMediaLoader mediaLoader;
    private Network network;

    private SceneContext context;
    private Scene initialScene;

    private static AtomicBoolean nativeLibrariesLoaded = new AtomicBoolean(false);

    //TODO support both Intel and M1 native libraries, but
    //     currently the application bundle itself is not
    //     universal and therefore requires x86 libraries.
    private static final List<NativeLibrary> NATIVE_LIBRARIES = List.of(
        new NativeLibrary("gdx", "libgdx64.dylib"),
        new NativeLibrary("lwjgl", "liblwjgl.dylib"),
        new NativeLibrary("lwjgl_opengl", "liblwjgl_opengl.dylib"),
        new NativeLibrary("lwjgl_stb", "liblwjgl_stb.dylib"),
        new NativeLibrary("glfw", "libglfw.dylib"),
        new NativeLibrary("openal", "libopenal.dylib"),
        new NativeLibrary("gdx-freetype", "libgdx-freetype64.dylib")
    );

    private static final Logger LOGGER = LogHelper.getLogger(GDXRenderer.class);

    public GDXRenderer(GraphicsMode graphicsMode, DisplayMode displayMode, WindowOptions window) {
        this.graphicsMode = graphicsMode;
        this.canvas = displayMode.canvas();
        this.framerate = displayMode.framerate();
        this.window = window;

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
        Configuration.GLFW_LIBRARY_NAME.set(lwjglMapper.apply("glfw"));

        // Rewrite libGDX -> LWJGL -> GLFX initialization process.

        initGLFW();

        // Load libGDX native libraries.

        GdxNativesLoader.disableNativesLoading = true;

        for (NativeLibrary lib : NATIVE_LIBRARIES) {
            SharedLibraryLoader.setLoaded(lib.name);
            File dylib = new File(nativeLibraryDir, lib.fileName);
            LOGGER.info("Loading native library " + lib.name + " from " + dylib.getAbsolutePath());
            System.load(dylib.getAbsolutePath());
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

    //TODO should be removed when libGDX fixes this in a future version.
    @Deprecated
    private void initGLFW() throws Exception {
        Field errorCallback = Lwjgl3Application.class.getDeclaredField("errorCallback");
        errorCallback.setAccessible(true);
        errorCallback.set(null, GLFWErrorCallback.createPrint(Lwjgl3ApplicationConfiguration.errorStream));

        Method awtInit = Lwjgl3Application.class.getDeclaredMethod("loadGlfwAwtMacos");
        awtInit.setAccessible(true);
        awtInit.invoke(null);

        GLFW.glfwSetErrorCallback((GLFWErrorCallbackI) errorCallback.get(null));
        GLFW.glfwInitHint(GLFW.GLFW_ANGLE_PLATFORM_TYPE, GLFW.GLFW_ANGLE_PLATFORM_TYPE_METAL);
        GLFW.glfwInitHint(GLFW.GLFW_JOYSTICK_HAT_BUTTONS, GLFW.GLFW_FALSE);
        if (!GLFW.glfwInit()) {
            throw new RendererException("Error while initializing GLFW");
        }
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
        graphicsContext = new GDXGraphics(canvas, mediaLoader);
        network = new StandardNetwork();

        resize(canvas.getWidth(), canvas.getHeight());

        context = new SceneContext(this, mediaLoader, input, network);
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
    public void terminate() {
        System.exit(0);
    }

    @Override
    public String toString() {
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
