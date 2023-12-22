//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import lombok.Getter;
import nl.colorize.multimedialib.stage.FontStyle;
import nl.colorize.multimedialib.stage.OutlineFont;

import java.awt.Font;

@Getter
public class AWTFont implements OutlineFont {

    private Font font;
    private String family;
    private FontStyle style;

    protected AWTFont(Font font, String family, FontStyle style) {
        this.font = font;
        this.family = family;
        this.style = style;
    }

    @Override
    public AWTFont derive(FontStyle newStyle) {
        Font newFont = font.deriveFont(newStyle.bold() ? Font.BOLD : Font.PLAIN, newStyle.size());
        return new AWTFont(newFont, family, newStyle);
    }
}
