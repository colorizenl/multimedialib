//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.FontStyle;
import nl.colorize.multimedialib.graphics.OutlineFont;

import java.awt.Font;

public class AWTFont implements OutlineFont {

    private Font font;
    private FontStyle style;

    protected AWTFont(Font font, FontStyle style) {
        this.font = font;
        this.style = style;
    }

    protected Font getFont() {
        return font;
    }

    @Override
    public FontStyle getStyle() {
        return style;
    }

    @Override
    public OutlineFont derive(FontStyle newStyle) {
        Preconditions.checkArgument(style.family().equals(newStyle.family()),
            "Font family mismatch: expected " + style.family());

        Font newFont = font.deriveFont(newStyle.bold() ? Font.BOLD : Font.PLAIN, newStyle.size());
        return new AWTFont(newFont, newStyle);
    }
}
