//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.renderer.java2d.Java2DRenderer;
import nl.colorize.multimedialib.renderer.java2d.WindowOptions;
import nl.colorize.multimedialib.renderer.libgdx.GDXRenderer;
import nl.colorize.multimedialib.renderer.teavm.TeaRenderer;

/**
 * Central access point for obtaining {@link Renderer} instances.
 */
public class RendererFactory {

    private int framerate;
    private WindowOptions windowOptions;

    public RendererFactory() {
        this.framerate = 60;
        this.windowOptions = new WindowOptions("MultimediaLib");
    }

    public void setFrameRate(int framerate) {
        Preconditions.checkArgument(ImmutableList.of(20, 25, 30, 60).contains(framerate),
            "Frame rate not supported: " + framerate);
        this.framerate = framerate;
    }

    public int getFrameRate() {
        return framerate;
    }

    public void setWindowOptions(WindowOptions windowOptions) {
        this.windowOptions = windowOptions;
    }

    public WindowOptions getWindowOptions() {
        return windowOptions;
    }

    /**
     * Creates a renderer with the specified type.
     *
     * @throws UnsupportedOperationException if the requested renderer
     *         implementation does not support the current platform.
     */
    public Renderer create(Implementation type, Canvas canvas) {
        switch (type) {
            case JAVA2D : return new Java2DRenderer(canvas, framerate, windowOptions);
            case GDX : return new GDXRenderer(canvas, framerate, windowOptions);
            case TEAVM : return new TeaRenderer(canvas);
            default : throw new AssertionError("Unknown renderer: " + type);
        }
    }

    /**
     * Lists all available renderer implementations. Note that not every renderer
     * will support every platform.
     */
    public static enum Implementation {
        JAVA2D,
        GDX,
        TEAVM
    }
}
