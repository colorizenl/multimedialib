//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.graphics.BitmapFont;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Rect;

/**
 * Provides access to the renderer's drawing operations. The renderer only allows
 * drawing operations at the end of a frame updates, when the frame is rendered.
 */
public interface RenderContext {

    public int getCanvasWidth();

    public int getCanvasHeight();

    public void drawBackground(ColorRGB backgroundColor);

    public void drawRect(Rect rect, ColorRGB color, Transform transform);

    public void drawImage(Image image, int x, int y, Transform transform);

    default void drawSprite(Sprite sprite, int x, int y, Transform transform) {
        drawImage(sprite.getCurrentGraphics(), x, y, transform);
    }

    public void drawText(String text, BitmapFont font, int x, int y);

    public RenderStats getStats();
}
