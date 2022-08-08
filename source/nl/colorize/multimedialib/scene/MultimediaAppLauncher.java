//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.annotations.VisibleForTesting;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.WindowOptions;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import nl.colorize.multimedialib.renderer.java2d.Java2DRenderer;
import nl.colorize.multimedialib.renderer.libgdx.GDXRenderer;
import nl.colorize.multimedialib.renderer.teavm.TeaRenderer;
import nl.colorize.multimedialib.renderer.teavm.WebGraphics;
import nl.colorize.util.Platform;

/**
 * Entry point for launching MultimediaLib applications, by providing access to
 * the various renderer implementations. Note that this only *creates* the
 * application, the application will not actually *start* until the initial
 * scene is created and used to call {@link Renderer#start(Scene, ErrorHandler)}.
 */
public final class MultimediaAppLauncher {

    private MultimediaAppLauncher() {
    }

    public static Renderer launchJava2D(DisplayMode displayMode, WindowOptions window) {
        return new Java2DRenderer(displayMode, window);
    }

    public static Renderer launchGDX(DisplayMode displayMode, WindowOptions window) {
        return new GDXRenderer(displayMode, window);
    }

    public static Renderer launchTea(DisplayMode displayMode, WebGraphics graphicsMode) {
        Platform.enableTeaVM();
        return new TeaRenderer(displayMode, graphicsMode);
    }

    /**
     * Creates a MultimediaLib application using the {@link HeadlessRenderer},
     * intended for testing, verification, and simulation purposes.
     */
    @VisibleForTesting
    public static Renderer headless() {
        return new HeadlessRenderer();
    }
}
