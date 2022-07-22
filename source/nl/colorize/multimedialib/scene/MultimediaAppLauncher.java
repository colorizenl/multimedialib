//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.annotations.VisibleForTesting;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.WindowOptions;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import nl.colorize.multimedialib.renderer.java2d.Java2DRenderer;
import nl.colorize.multimedialib.renderer.libgdx.GDXRenderer;
import nl.colorize.multimedialib.renderer.teavm.TeaRenderer;
import nl.colorize.util.Platform;
import nl.colorize.util.swing.ApplicationMenuListener;

/**
 * Entry point for configuring and launching MultimediaLib applications. It
 * provides access to the various renderer implementations.
 * <p>
 * Note that not every renderer implementation will support every configuration
 * option. For example, some renderers will always use the device's native
 * framerate and will not allow setting a custom framerate for the application.
 */
public final class MultimediaAppLauncher {

    private DisplayMode displayMode;
    private WindowOptions windowOptions;

    private static final String DEFAULT_WINDOW_TITLE = "MultimediaLib";
    private static final FilePointer DEFAULT_ICON = new FilePointer("colorize-icon-32.png");

    private MultimediaAppLauncher(Canvas canvas) {
        this.displayMode = new DisplayMode(canvas, 60);
        this.windowOptions = new WindowOptions(DEFAULT_WINDOW_TITLE, DEFAULT_ICON);
    }

    public MultimediaAppLauncher withFramerate(int framerate) {
        this.displayMode = new DisplayMode(displayMode.getCanvas(), framerate);
        return this;
    }

    public MultimediaAppLauncher withDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
        return withFramerate(displayMode.getFramerate());
    }

    public MultimediaAppLauncher withWindowTitle(String title) {
        windowOptions.setTitle(title);
        return this;
    }

    public MultimediaAppLauncher withWindowIcon(FilePointer iconFile) {
        windowOptions.setIconFile(iconFile);
        return this;
    }

    public MultimediaAppLauncher withFullscreen(boolean fullscreen) {
        windowOptions.setFullscreen(fullscreen);
        return this;
    }

    public MultimediaAppLauncher withFullscreen() {
        return withFullscreen(true);
    }

    public MultimediaAppLauncher withEmbedded(boolean embedded) {
        windowOptions.setEmbedded(embedded);
        return this;
    }

    public MultimediaAppLauncher withMacApplicationMenu(ApplicationMenuListener listener) {
        windowOptions.setAppMenuListener(listener);
        return this;
    }

    public MultimediaAppLauncher withWindowOptions(WindowOptions windowOptions) {
        this.windowOptions = windowOptions;
        return this;
    }

    /**
     * Starts a MultimediaLib application using the {@link Java2DRenderer}. This
     * is guaranteed to work on all desktop environments. Returns the renderer
     * that was created and used to start the application.
     */
    public Renderer startJava2D(Scene initialScene) {
        Java2DRenderer renderer = new Java2DRenderer(displayMode, windowOptions);
        renderer.start(initialScene);
        return renderer;
    }

    /**
     * Starts a MultimediaLib application using the {@link GDXRenderer} that
     * will use the LWJGL back-end to render graphics, suitable for all desktop
     * platforms. Returns the renderer that was created and used to start the
     * application.
     */
    public Renderer startGDX(Scene initialScene) {
        GDXRenderer renderer = new GDXRenderer(displayMode, windowOptions);
        renderer.start(initialScene);
        return renderer;
    }

    /**
     * Starts a MultimediaLib application using the {@link TeaRenderer}. Graphics
     * can be 2D (via HTML5 canvas or WebGL) or 3D (via three.js), depending on
     * which transpile options were used in {@code TeaVMTranspiler}. Returns the
     * renderer that was created and used to start the application.
     */
    public Renderer startTea(Scene initialScene) {
        Platform.enableTeaVM();
        TeaRenderer renderer = new TeaRenderer(displayMode);
        renderer.start(initialScene);
        return renderer;
    }

    /**
     * Starts a MultimediaLib application using the {@link HeadlessRenderer},
     * intended for testing, verification, and simulation purposes.
     */
    @VisibleForTesting
    public void startHeadless(Scene initialScene) {
        HeadlessRenderer renderer = new HeadlessRenderer();
        renderer.start(initialScene);
    }

    /**
     * Creates a launcher that will initially use the default application
     * configuration.
     */
    public static MultimediaAppLauncher create(Canvas canvas) {
        return new MultimediaAppLauncher(canvas);
    }
}
