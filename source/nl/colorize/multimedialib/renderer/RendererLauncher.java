//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Size;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import nl.colorize.multimedialib.renderer.java2d.Java2DRenderer;
import nl.colorize.multimedialib.renderer.jfx.JFXRenderer;
import nl.colorize.multimedialib.renderer.libgdx.GDXRenderer;
import nl.colorize.multimedialib.renderer.teavm.TeaRenderer;
import nl.colorize.util.LogHelper;

import java.util.logging.Logger;

/**
 * Creates and initialize {@link Renderer} instances. This class should be
 * used from the application's {@code main} method, and will start the
 * requested renderer in the appropriate thread. The current platform is
 * detected automatically, and platform-specific logic will be applied
 * accordingly when starting the renderer.
 * <p>
 * <strong>Development note:</strong> The launcher behavior can be influenced
 * using the system property {@code colorize.multimedialib.emulation}. When
 * enabled, this system property will "emulate" the appearance and behavior
 * of non-native platforms. This can be used for development and testing
 * purposes. Emulation is only available on desktop platforms.
 */
public final class RendererLauncher {

    private DisplayMode displayMode;
    private WindowOptions windowOptions;

    public static final String EMULATION_SYSTEM_PROPERTY = "colorize.multimedialib.emulation";
    private static final Size IPHONE_EMULATION_WINDOW = new Size(350, 760);
    private static final Size IPAD_EMULATION_WINDOW = new Size(570, 760);
    private static final Logger LOGGER = LogHelper.getLogger(RendererLauncher.class);

    private RendererLauncher() {
    }

    /**
     * Initializes and returns the renderer with the specified name,
     * configuring the renderer so that it can be used on desktop platforms
     * in 2D graphics mode.
     *
     * @throws UnsupportedOperationException if the renderer with the
     *         requested name does not exist, or if that renderer does not
     *         support the requested graphics mode and/or platforms.
     * @throws IllegalStateException if this launcher has not configured any
     *         window options.
     */
    public Renderer forDesktop2D(String requestedRendererName) {
        Preconditions.checkState(windowOptions != null,
            "Launcher configuration is missing window options");

        prepareWindowOptions();

        return switch (requestedRendererName) {
            case "java2d" -> new Java2DRenderer(displayMode, windowOptions);
            case "javafx", "jfx" -> JFXRenderer.launch(displayMode, windowOptions);
            case "libgdx", "gdx" -> new GDXRenderer(GraphicsMode.MODE_2D, displayMode, windowOptions);
            default -> throw new UnsupportedOperationException("Requested renderer not supported");
        };
    }

    /**
     * Initializes and returns the renderer with the specified name,
     * configuring the renderer so that it can be used on desktop platforms
     * in 3D graphics mode.
     *
     * @throws UnsupportedOperationException if the renderer with the
     *         requested name does not exist, or if that renderer does not
     *         support the requested graphics mode and/or platforms.
     * @throws IllegalStateException if this launcher has not configured any
     *         window options.
     */
    public Renderer forDesktop3D(String requestedRendererName) {
        Preconditions.checkState(windowOptions != null,
            "Launcher configuration is missing window options");

        prepareWindowOptions();

        return switch (requestedRendererName) {
            case "libgdx", "gdx" -> new GDXRenderer(GraphicsMode.MODE_3D, displayMode, windowOptions);
            default -> throw new UnsupportedOperationException("Requested renderer not supported");
        };
    }

    /**
     * Initializes and returns the renderer with the specified name,
     * configuring the renderer so that it can be used on web and mobile
     * platforms in 2D graphics mode.
     *
     * @throws UnsupportedOperationException if the renderer with the
     *         requested name does not exist, or if that renderer does not
     *         support the requested graphics mode and/or platforms.
     */
    public Renderer forBrowser2D(String requestedRendererName) {
        return switch (requestedRendererName) {
            case "html5", "canvas" -> TeaRenderer.withCanvas(displayMode);
            case "pixi", "pixijs" -> TeaRenderer.withPixi(displayMode);
            default -> throw new UnsupportedOperationException("Requested renderer not supported");
        };
    }

    /**
     * Initializes and returns the renderer with the specified name,
     * configuring the renderer so that it can be used on web and mobile
     * platforms in 3D graphics mode.
     *
     * @throws UnsupportedOperationException if the renderer with the
     *         requested name does not exist, or if that renderer does not
     *         support the requested graphics mode and/or platforms.
     */
    public Renderer forBrowser3D(String requestedRendererName) {
        return switch (requestedRendererName) {
            case "three", "threejs" -> TeaRenderer.withThree(displayMode);
            default -> throw new UnsupportedOperationException("Requested renderer not supported");
        };
    }

    /**
     * Initializes and returns a headless renderer that can be used for
     * testing and simulation purposes.
     */
    public Renderer forHeadless() {
        return new HeadlessRenderer(displayMode, false);
    }

    private void prepareWindowOptions() {
        String emulation = System.getProperty(EMULATION_SYSTEM_PROPERTY);

        if ("ios".equals(emulation) || "iphone".equals(emulation)) {
            LOGGER.info("Launching application in iPhone emulation mode");
            windowOptions.setWindowSize(IPHONE_EMULATION_WINDOW);
        } else if ("ipad".equals(emulation)) {
            LOGGER.info("Launching application in iPad emulation mode");
            windowOptions.setWindowSize(IPAD_EMULATION_WINDOW);
        }
    }

    /**
     * Returns a launcher instance that will start the renderer using the
     * specified display mode and window options. Note the window options
     * will only be used on desktop platforms.
     */
    public static RendererLauncher configure(DisplayMode displayMode, WindowOptions windowOptions) {
        RendererLauncher launcher = new RendererLauncher();
        launcher.displayMode = displayMode;
        launcher.windowOptions = windowOptions;
        return launcher;
    }

    /**
     * Returns a launcher instance that will start the renderer using the
     * specified display mode. The launcher configuration will not include
     * window options, meaning this launcher cannot be used on desktop
     * platforms.
     */
    public static RendererLauncher configure(DisplayMode displayMode) {
        RendererLauncher launcher = new RendererLauncher();
        launcher.displayMode = displayMode;
        return launcher;
    }
}
