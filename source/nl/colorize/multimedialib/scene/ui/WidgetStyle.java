//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.ui;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.renderer.Drawable;

/**
 * Describes the style with which a user interface widget should be displayed.
 * This acts as a template that can be reused across multiple widgets. Note
 * that not all widgets will actually use every single property, the set of
 * properties that apply is different for each widget type.
 */
public class WidgetStyle {

    private TTFont font;
    private Image background;
    private ColorRGB backgroundColor;
    private Drawable backgroundGraphics;
    private ColorRGB borderColor;
    private int borderSize;

    public WidgetStyle(TTFont font) {
        this.font = font;
    }

    public WidgetStyle(TTFont font, Image background) {
        this.font = font;
        this.background = background;
    }

    public TTFont getFont() {
        return font;
    }

    public void setFont(TTFont font) {
        this.font = font;
    }

    public Image getBackground() {
        return background;
    }

    public void setBackground(Image background) {
        this.background = background;
    }

    public ColorRGB getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(ColorRGB backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Drawable getBackgroundGraphics() {
        return backgroundGraphics;
    }

    public void setBackgroundGraphics(Drawable backgroundGraphics) {
        this.backgroundGraphics = backgroundGraphics;
    }

    public ColorRGB getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(ColorRGB borderColor) {
        this.borderColor = borderColor;
    }

    public int getBorderSize() {
        return borderSize;
    }

    public void setBorderSize(int borderSize) {
        this.borderSize = borderSize;
    }
}
