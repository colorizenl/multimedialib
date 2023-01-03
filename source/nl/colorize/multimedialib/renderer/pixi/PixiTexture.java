//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.pixi;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface PixiTexture extends JSObject {

    @JSProperty
    public PixiRectangle getFrame();

    public void update();

    public void updateUvs();
}
