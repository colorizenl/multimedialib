//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.google.common.base.Preconditions;
import lombok.Getter;
import nl.colorize.multimedialib.stage.FontStyle;
import nl.colorize.multimedialib.stage.OutlineFont;

import java.awt.Font;

@Getter
public class AWTFont implements OutlineFont {

    private Font font;
    private FontStyle style;

    protected AWTFont(Font font, FontStyle style) {
        this.font = font;
        this.style = style;
    }

    @Override
    public AWTFont derive(FontStyle newStyle) {
        Preconditions.checkArgument(style.family().equals(newStyle.family()),
            "Font family mismatch: expected " + style.family());

        Font newFont = font.deriveFont(newStyle.bold() ? Font.BOLD : Font.PLAIN, newStyle.size());
        return new AWTFont(newFont, newStyle);
    }
}
