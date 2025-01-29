//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.canvas.CanvasImageSource;

/**
 * TeaVM interface for the {@code pixi-bridge.js} JavaScript implementation.
 * Includes TeaVM stubs for <a href="https://pixijs.com">PixiJS</a> classes.
 */
public interface PixiBridge extends JSObject {

    public void init();

    @JSProperty
    public PixiApplication getPixiApp();

    @JSProperty
    public PixiDisplayObject getRootContainer();

    public void changeBackgroundColor(int backgroundColor);

    public PixiDisplayObject createContainer();

    public PixiTexture createTexture(String id, CanvasImageSource image,
                                     float regionX, float regionY,
                                     float regionWidth, float regionHeight);

    public PixiDisplayObject createSprite(PixiTexture texture);

    public PixiDisplayObject createGraphics();

    public PixiDisplayObject createText(String family, float size, boolean bold,
                                        String align, float lineHeight, int color);

    /**
     * <a href="https://api.pixijs.io/@pixi/app/PIXI/Application.html">Application</a>
     */
    interface PixiApplication extends JSObject {

        @JSProperty
        public PixiRectangle getScreen();
    }

    /**
     * <a href="https://api.pixijs.io/@pixi/display/PIXI/DisplayObject.html">DisplayObject</a>
     */
    interface PixiDisplayObject extends JSObject {

        @JSProperty
        public void setVisible(boolean visible);

        @JSProperty
        public void setX(float x);

        @JSProperty
        public void setY(float y);

        @JSProperty
        public void setAlpha(float alpha);

        @JSProperty
        public void setAngle(float angle);

        @JSProperty
        public PixiDisplayObject getScale();

        @JSProperty
        public void setText(String text);

        @JSProperty
        public void setTexture(PixiTexture texture);

        @JSProperty
        public PixiTexture getTexture();

        @JSProperty
        public void setTintEnabled(boolean tintEnabled);

        @JSProperty
        public boolean isTintEnabled();

        @JSProperty
        public PixiTexture getOriginalTexture();

        @JSProperty
        public void setOriginalTexture(PixiTexture texture);

        @JSProperty
        public PixiDisplayObject[] getChildren();

        public void addChild(PixiDisplayObject child);

        public void removeChild(PixiDisplayObject child);

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

    /**
     * <a href="https://api.pixijs.io/@pixi/math/PIXI/Rectangle.html">Rectangle</a>
     */
    interface PixiRectangle extends JSObject {

        @JSProperty
        public void setX(float x);

        @JSProperty
        public float getX();

        @JSProperty
        public void setY(float y);

        @JSProperty
        public float getY();

        @JSProperty
        public void setWidth(float width);

        @JSProperty
        public float getWidth();

        @JSProperty
        public void setHeight(float height);

        @JSProperty
        public float getHeight();
    }

    /**
     * <a href="https://api.pixijs.io/@pixi/core/PIXI/Texture.html">Texture</a>
     */
    interface PixiTexture extends JSObject {

        @JSProperty
        public PixiRectangle getFrame();

        @JSProperty
        public String getTextureImageId();

        public void update();

        public void updateUvs();
    }
}
