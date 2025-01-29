//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Size;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import nl.colorize.multimedialib.renderer.java2d.Java2DRenderer;
import nl.colorize.multimedialib.renderer.jfx.JFXRenderer;
import nl.colorize.multimedialib.renderer.libgdx.GDXRenderer;
import nl.colorize.multimedialib.renderer.teavm.PixiGraphics;
import nl.colorize.multimedialib.renderer.teavm.HtmlCanvasGraphics;
import nl.colorize.multimedialib.renderer.teavm.TeaRenderer;
import nl.colorize.multimedialib.renderer.teavm.ThreeGraphics;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.util.Development;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;

import java.io.File;
import java.util.function.Supplier;
import java.util.logging.Logger;

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
 * <strong>Simulation mode:</strong> During development, the launcher can
 * "simulate" the behavior of mobile platforms when running on desktop
 * platforms. This is comparable to "responsive design mode" in desktop
 * browsers. Simulation mode can be activated programmatically, but it can
 * also be enabled using the system property {@code multimedialib.simulation}.
 */
@Getter
@Setter
public final class RenderConfig {

    private final Supplier<Renderer> launcher;
    private final GraphicsMode graphicsMode;
    private final Canvas canvas;
    private int framerate;
    private final WindowOptions windowOptions;
    private ErrorHandler errorHandler;
    private String simulationMode;

    private static final Size SIMULATION_MODE_PHONE = new Size(350, 760);
    private static final Size SIMULATION_MODE_TABLET = new Size(570, 760);
    private static final Logger LOGGER = LogHelper.getLogger(RenderConfig.class);

    private RenderConfig(Supplier<Renderer> launcher, GraphicsMode graphicsMode, Canvas canvas) {
        this.launcher = launcher;
        this.graphicsMode = graphicsMode;
        this.canvas = canvas;
        this.framerate = 60;
        this.errorHandler = ErrorHandler.DEFAULT;
        this.windowOptions = new WindowOptions();
        this.simulationMode = System.getProperty("multimedialib.simulation");
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

        Renderer renderer = launcher.get();

        if (!renderer.isSupported(graphicsMode)) {
            throw new UnsupportedOperationException("Renderer does not support graphics mode");
        }

        renderer.start(this, initialScene);

        if (Platform.isDesktopPlatform() && renderer instanceof SceneContext context) {
            context.attachGlobal((ctx, deltaTime) -> checkScreenshotHandler(ctx));
        }
    }

    private void applySimulationMode() {
        LOGGER.info("Using simulation mode '" + simulationMode + "'");

        Size simulationModeScreenSize = switch (simulationMode) {
            case "tablet", "ipad" -> SIMULATION_MODE_TABLET;
            default -> SIMULATION_MODE_PHONE;
        };

        windowOptions.setFullscreen(false);
        windowOptions.setWindowSize(simulationModeScreenSize);
    }

    /**
     * Global handler that saves screenshots to the platform default location
     * whenever the F12 is pressed. This handler is only available on desktop
     * platforms.
     */
    private void checkScreenshotHandler(SceneContext context) {
        if (context.getInput().isKeyReleased(KeyCode.F12)) {
            try {
                File screenshotFile = new File(Platform.getUserDesktopDir(),
                    "screenshot-" + System.currentTimeMillis() + ".png");
                context.takeScreenshot(screenshotFile);
                LOGGER.info("Saved screenshot to " + screenshotFile.getAbsolutePath());
            } catch (UnsupportedOperationException e) {
                LOGGER.warning("Screenshots not supported");
            }
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
            case "libgdx", "gdx" -> GDXRenderer::new;
            case "javafx", "jfx", "openjfx" -> JFXRenderer::launch;
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
            case "canvas", "html5" -> () -> new TeaRenderer(new HtmlCanvasGraphics());
            case "pixi", "pixijs" -> () -> new TeaRenderer(new PixiGraphics());
            case "three", "threejs" -> () -> new TeaRenderer(new ThreeGraphics());
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
        boolean graphicsEnv = graphicsMode != GraphicsMode.HEADLESS;
        return new RenderConfig(() -> new HeadlessRenderer(graphicsEnv), graphicsMode, canvas);
    }
}
