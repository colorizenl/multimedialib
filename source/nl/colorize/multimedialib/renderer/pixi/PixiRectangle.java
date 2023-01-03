//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.pixi;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface PixiRectangle extends JSObject {

    @JSProperty
    public void setX(float x);

    @JSProperty
    public void setY(float y);

    @JSProperty
    public void setWidth(float width);

    @JSProperty
    public void setHeight(float height);
}
