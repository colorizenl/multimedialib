//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.stage.StageVisitor;
import org.teavm.jso.browser.Window;

/**
 * Base interface for the different JavaScript graphics frameworks supported by
 * the TeaVM renderer.
 */
public interface TeaGraphics extends StageVisitor {

    public GraphicsMode getGraphicsMode();

    public void init();

    public int getDisplayWidth();

    public int getDisplayHeight();

    default float getDevicePixelRatio() {
        return (float) Window.current().getDevicePixelRatio();
    }
}
