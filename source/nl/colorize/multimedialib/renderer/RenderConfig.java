//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import nl.colorize.multimedialib.math.Size;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import nl.colorize.multimedialib.renderer.java2d.Java2DRenderer;
import nl.colorize.multimedialib.renderer.libgdx.GDXBrowserRenderer;
import nl.colorize.multimedialib.renderer.libgdx.GDXDesktopRenderer;
import nl.colorize.multimedialib.renderer.skija.SkijaRenderer;
import nl.colorize.multimedialib.renderer.teavm.Browser;
import nl.colorize.multimedialib.renderer.teavm.HtmlCanvasRenderer;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.util.Development;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;
import nl.colorize.util.Tuple;
import nl.colorize.util.http.PostData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static lombok.AccessLevel.PRIVATE;

/**
 * Used to configure and launch the renderer, and can also be used to obtain
 * this configuration at runtime. Launching the renderer effectively "starts"
 * the MultimediaLib application, and is therefore typically done from the
 * application's {@code main} method. Application code will not directly
 * interact with the renderer. The renderer runs in a separate thread, and
 * will invoke callback methods that receive the {@link SceneContext} to
 * interact with the underlying renderer.
 * <p>
 * The renderer configuration consists of the following aspects:
 * <p>
 * <strong>Renderer:</strong> MultimediaLib supports multiple renderer
 * implementations. Applications can select a renderer implementation based
 * on the target platform(s) and whether the application uses 2D or 3D
 * graphics.
 * <p>
 * <strong>Canvas:</strong> Defines how the renderer should display graphics
 * for the current screen size and resolution. See {@link Canvas} for more
 * information.
 * <p>
 * <strong>Framerate:</strong> The renderer will attempt to perform frame
 * updates as close as possible to the target framerate.
 * <p>
 * <strong>Window:</strong> Defines how the application window should be
 * displayed on desktop platforms. See {@link WindowOptions} for more
 * information.
 * <p>
 * <strong>Error handler:</strong> The default error handler will log the
 * error and then terminate the renderer. Additional error handlers can be
 * added in order to customize this behavior.
 * <p>
 * <strong>Global handlers:</strong> The configuration can define "global"
 * handlers that are always active, independent of the currently active
 * scene. Global handlers can also be registered at runtime, using
 * {@link SceneContext#attachGlobalSubScene(Scene)}.
 * <p>
 * <strong>Simulation mode:</strong> During development, the launcher can
 * "simulate" the behavior of mobile platforms when running on desktop
 * platforms. This is comparable to "responsive design mode" in desktop
 * browsers. Simulation mode can be activated programmatically, but it can
 * also be enabled using the system property {@code multimedialib.simulation}.
 */
@AllArgsConstructor(access=PRIVATE)
@Getter
public final class RenderConfig {

    private Supplier<Renderer> launcher;
    private String rendererName;
    private GraphicsMode graphicsMode;
    private Canvas canvas;
    @With private int framerate;
    @With private WindowOptions windowOptions;
    @With private ErrorHandler errorHandler;
    @With private List<Scene> globalHandlers;
    @With private String simulationMode;
    private Locale userLocale;

    private static final Size SIMULATION_MODE_PHONE = new Size(350, 760);
    private static final Size SIMULATION_MODE_TABLET = new Size(570, 760);
    private static final Pattern LOCALE_PATTERN = Pattern.compile("([a-z]{2})[-_]([a-zA-Z]{2})");
    private static final Logger LOGGER = LogHelper.getLogger(RenderConfig.class);

    private RenderConfig(Supplier<Renderer> launcher, GraphicsMode graphicsMode, Canvas canvas) {
        this.launcher = launcher;
        this.rendererName = "<launcher>";
        this.graphicsMode = graphicsMode;
        this.canvas = canvas;
        this.framerate = 60;
        this.errorHandler = ErrorHandler.DEFAULT;
        this.windowOptions = new WindowOptions("MultimediaLib", WindowOptions.DEFAULT_ICON, false);
        this.globalHandlers = new CopyOnWriteArrayList<>();
        this.simulationMode = System.getProperty("multimedialib.simulation");
        this.userLocale = Locale.getDefault();
    }

    public boolean isSimulationMode() {
        return simulationMode != null && !simulationMode.isEmpty();
    }

    /**
     * Uses this configuration to start the renderer, initially displaying
     * the provided scene. Once the renderer has been started, this
     * configuration will be available to the scene at runtime via the
     * {@link SceneContext}.
     *
     * @throws UnsupportedOperationException if the renderer does not
     *         support the graphics mode defined in this configuration.
     */
    public void start(Scene initialScene) {
        if (isSimulationMode()) {
            applySimulationMode();
        }

        if (Platform.isTeaVM()) {
            initQueryStringSystemProperties();
            initBrowserLocale();
        }

        Renderer renderer = launcher.get();
        rendererName = renderer.getDisplayName();

        if (!renderer.getSupportedGraphicsModes().contains(graphicsMode)) {
            throw new UnsupportedOperationException("Renderer does not support graphics mode");
        }

        List<Scene> combinedGlobalHandlers = new ArrayList<>();
        combinedGlobalHandlers.addAll(globalHandlers);
        combinedGlobalHandlers.addAll(renderer.getGlobalHandlers());

        BootstrapScene bootstrapScene = new BootstrapScene(initialScene, combinedGlobalHandlers);
        renderer.start(this, bootstrapScene);
    }

    private void applySimulationMode() {
        LOGGER.info("Using simulation mode '" + simulationMode + "'");

        Size simulationModeScreenSize = switch (simulationMode) {
            case "tablet", "ipad" -> SIMULATION_MODE_TABLET;
            default -> SIMULATION_MODE_PHONE;
        };

        windowOptions = new WindowOptions(
            windowOptions.getTitle(),
            windowOptions.getIconFile(),
            false,
            simulationModeScreenSize
        );
    }

    private void initQueryStringSystemProperties() {
        String queryString = Browser.getQueryString();
        PostData queryParams = PostData.parse(queryString);

        for (Tuple<String, String> param : queryParams) {
            if (!param.left().isEmpty() && !param.right().isEmpty()) {
                if (System.getProperty(param.left()) == null) {
                    System.setProperty(param.left(), param.right());
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void initBrowserLocale() {
        String browserLanguage = Browser.getLanguage();
        Matcher matcher = LOCALE_PATTERN.matcher(browserLanguage);

        if (matcher.matches()) {
            userLocale = new Locale(matcher.group(1).toLowerCase(), matcher.group(2).toUpperCase());
        } else if (browserLanguage.length() == 2) {
            userLocale = new Locale(matcher.group(1).toLowerCase());
        }
    }

    /**
     * Starts configuring the desktop platform renderer with the specified
     * name and graphics mode.
     *
     * @throws IllegalStateException if the current platform is not a desktop
     *         platform.
     * @throws IllegalArgumentException if no desktop renderer with the
     *         requested name exists.
     * @throws UnsupportedOperationException if the requested renderer does
     *         not support the requested graphics mode.
     */
    public static RenderConfig forDesktop(String renderer, GraphicsMode graphicsMode, Canvas canvas) {
        Preconditions.checkState(Platform.isDesktopPlatform(),
            "Cannot launch desktop renderer on non-desktop platform: " + Platform.getPlatform());

        Supplier<Renderer> launcher = switch (renderer.toLowerCase()) {
            case "java2d" -> Java2DRenderer::new;
            case "libgdx", "gdx" -> GDXDesktopRenderer::new;
            case "skija" -> SkijaRenderer::new;
            default -> throw new IllegalArgumentException("Unknown desktop renderer: " + renderer);
        };

        return new RenderConfig(launcher, graphicsMode, canvas);
    }

    /**
     * Starts configuring the browser-based renderer with the specified name
     * and graphics mode.
     *
     * @throws IllegalStateException if the current platform is not
     *         browser-based.
     * @throws IllegalArgumentException if no browser-based renderer with the
     *         requested name exists.
     * @throws UnsupportedOperationException if the requested renderer does
     *         not support the requested graphics mode.
     */
    public static RenderConfig forBrowser(String renderer, GraphicsMode graphicsMode, Canvas canvas) {
        Preconditions.checkState(Platform.isTeaVM(), "Browser-based renderer requires TeaVM");

        Supplier<Renderer> launcher = switch (renderer.toLowerCase()) {
            case "canvas", "tea", "teavm" -> HtmlCanvasRenderer::new;
            case "libgdx", "gdx" -> GDXBrowserRenderer::new;
            default -> throw new IllegalArgumentException("Unknown browser renderer: " + renderer);
        };

        return new RenderConfig(launcher, graphicsMode, canvas);
    }

    /**
     * Starts configuring a headless renderer that will simulate the requested
     * graphics mode.
     */
    @Development
    public static RenderConfig headless(GraphicsMode graphicsMode, Canvas canvas) {
        return new RenderConfig(() -> new HeadlessRenderer(), graphicsMode, canvas);
    }

    /**
     * Registers a number of global handlers depending on the configuration,
     * then immediately proceeds to the application's actual initial scene.
     * Bootstrapping is required because the global handlers can only be
     * done from callback methods.
     */
    @AllArgsConstructor
    private static class BootstrapScene implements Scene {

        private Scene initialScene;
        private List<Scene> globalHandlers;

        @Override
        public void start(SceneContext context) {
            for (Scene handler : globalHandlers) {
                context.attachGlobalSubScene(handler);
            }

            context.changeScene(initialScene);
        }

        @Override
        public void update(SceneContext context, float deltaTime) {
        }
    }
}
