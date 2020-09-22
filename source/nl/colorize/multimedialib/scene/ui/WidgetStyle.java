//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.ui;

import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.TTFont;

/**
 * Describes the style with which a user interface widget should be displayed.
 * This acts as a template that can be reused across multiple widgets.
 */
public class WidgetStyle {

    private Image background;
    private TTFont font;

    public WidgetStyle(Image background, TTFont font) {
        this.background = background;
        this.font = font;
    }

    public WidgetStyle(TTFont font) {
        this(null, font);
    }

    public Image getBackground() {
        return background;
    }

    public TTFont getFont() {
        return font;
    }
}
