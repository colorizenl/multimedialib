//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import nl.colorize.multimedialib.math.Size;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.RenderConfig;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.RendererException;
import nl.colorize.multimedialib.renderer.java2d.StandardMediaLoader;
import nl.colorize.multimedialib.renderer.java2d.StandardNetwork;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneManager;
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

import static com.badlogic.gdx.Files.FileType.Internal;

/**
 * Renderer built on top of the libGDX framework. In turn, libGDX supports multiple
 * back-end implementations that determine which platforms are supported and which
 * libraries are used.
 */
public class GDXDesktopRenderer extends GDXContext implements Renderer {

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

    private static final Logger LOGGER = LogHelper.getLogger(GDXDesktopRenderer.class);

    public GDXDesktopRenderer() {
        super();

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
        this.sceneManager = new SceneManager(this, initialScene);

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
        gdxConfig.setDecorated(true);
        if (config.getWindowOptions().isFullscreen()) {
            //     see https://github.com/libgdx/libgdx/issues/6896
            //     Also note this extension will be retired altogether
            //     in https://github.com/libgdx/libgdx/pull/7361
            Dimension screen = SwingUtils.getScreenSize();
            gdxConfig.setWindowedMode(screen.width, screen.height);
            gdxConfig.setMaximized(true);
        } else {
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
    protected void initContext() {
        input = new GDXInput(config);
        mediaLoader = new GDXMediaLoader(new StandardMediaLoader());
        graphics = new GDXGraphics(config.getGraphicsMode(), config.getCanvas(), mediaLoader);
        network = new StandardNetwork();
    }

    @Override
    protected void prepareFrame() {
    }

    @Override
    public void terminate() {
        System.exit(0);
    }

    @Override
    public String getDisplayName() {
        return "libGDX renderer";
    }

    @Override
    public List<GraphicsMode> getSupportedGraphicsModes() {
        return List.of(GraphicsMode.MODE_2D, GraphicsMode.MODE_3D);
    }

    /**
     * Native library used by libGDX. These are normally managed by libGDX
     * itself, but when running from a Mac application bundle a different
     * mechanism is used. See {@link #loadApplicationBundleNativeLibraries()}.
     */
    private record NativeLibrary(String name, String fileName) {
    }
}
