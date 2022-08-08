//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.headless;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.FontStyle;
import nl.colorize.multimedialib.graphics.OutlineFont;

public class HeadlessFont implements OutlineFont {

    private FontStyle style;

    public HeadlessFont(FontStyle style) {
        this.style = style;
    }

    public HeadlessFont() {
        this(new FontStyle("headless", 10, false, ColorRGB.BLACK));
    }

    @Override
    public FontStyle getStyle() {
        return style;
    }

    @Override
    public OutlineFont derive(FontStyle style) {
        return new HeadlessFont(style);
    }
}
