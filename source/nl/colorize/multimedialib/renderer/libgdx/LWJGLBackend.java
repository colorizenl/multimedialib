//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.java2d.WindowOptions;

/**
 * Starts the application using the <a href="https://www.lwjgl.org">LWJGL</a>
 * back-end for libGDX. The application will run at the specified framerate,
 * but note that only 30, 60, and 120 FPS are supported.
 */
public class LWJGLBackend implements GDXBackend {

    private int framerate;
    private WindowOptions windowOptions;

    public LWJGLBackend(int framerate, WindowOptions windowOptions) {
        Preconditions.checkArgument(framerate == 30 || framerate == 60 || framerate == 120,
            "Framerate not supported: " + framerate);

        this.framerate = framerate;
        this.windowOptions = windowOptions;
    }

    @Override
    public void start(ApplicationListener app, Canvas canvas) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(canvas.getWidth(), canvas.getHeight());
        config.setDecorated(true);
        config.setIdleFPS(framerate);
        config.setHdpiMode(HdpiMode.Pixels);
        config.setTitle(windowOptions.getTitle());
        if (windowOptions.hasIcon()) {
            config.setWindowIcon(Files.FileType.Internal, windowOptions.getIconFile().getPath());
        }

        new Lwjgl3Application(app, config);
    }

    @Override
    public int getFramerate() {
        return framerate;
    }
}
