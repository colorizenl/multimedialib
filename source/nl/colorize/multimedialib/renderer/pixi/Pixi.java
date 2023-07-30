//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.pixi;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * TeaVM interface stubs for the Pixi.js JavaScript library.
 * <p>
 * <a href="https://pixijs.download/release/docs/PIXI.html">Documentation</a>
 */
public interface Pixi extends JSObject {

    public interface App extends JSObject {

        @JSProperty
        Rectangle getScreen();
    }

    public interface DisplayObject extends JSObject {

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
        public DisplayObject getScale();

        @JSProperty
        public void setText(String text);

        @JSProperty
        public void setTexture(Texture texture);

        @JSProperty
        public Texture getTexture();

        @JSProperty
        public void setTintEnabled(boolean tintEnabled);

        @JSProperty
        public boolean isTintEnabled();

        @JSProperty
        public Pixi.Texture getOriginalTexture();

        @JSProperty
        public void setOriginalTexture(Pixi.Texture texture);

        @JSProperty
        public DisplayObject[] getChildren();

        public void addChild(DisplayObject child);

        public void removeChild(DisplayObject child);

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

    public interface Rectangle extends JSObject {

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

    public interface Texture extends JSObject {

        @JSProperty
        public Rectangle getFrame();

        @JSProperty
        public String getTextureImageId();

        public void update();

        public void updateUvs();
    }
}
