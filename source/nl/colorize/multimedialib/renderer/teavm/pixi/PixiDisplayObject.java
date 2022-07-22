//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm.pixi;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * TeaVM/Java interface for PixiJS's {@code PIXI.DisplayObject} class and all
 * of its subclasses.
 */
public interface PixiDisplayObject extends JSObject {

    @JSProperty
    public void setVisible(boolean visible);

    @JSProperty
    public void setX(float x);

    @JSProperty
    public void setY(float y);

    @JSProperty
    public void setZIndex(int zIndex);

    @JSProperty
    public void setAlpha(float alpha);

    @JSProperty
    public void setAngle(float angle);

    @JSProperty
    public PixiDisplayObject getScale();

    @JSProperty
    public void setText(String text);

    @JSProperty
    public PixiTexture getTexture();

    @JSProperty
    public void setTint(int color);

    @JSProperty
    public void setTintEnabled(boolean tintEnabled);

    @JSProperty
    public boolean isTintEnabled();

    public void beginFill(int color, float alpha);

    public void drawRect(float x, float y, float width, float height);

    public void drawCircle(float x, float y, float radius);

    public void drawPolygon(float[] points);

    public void endFill();

    public void clear();

    public void lineStyle(int thickness, int color);

    public void moveTo(float x, float y);

    public void lineTo(float x, float y);
}
