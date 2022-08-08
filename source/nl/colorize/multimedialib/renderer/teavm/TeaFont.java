//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.FontStyle;
import nl.colorize.multimedialib.graphics.OutlineFont;

public class TeaFont implements OutlineFont {

    private String id;
    private FontStyle style;

    protected TeaFont(String id, FontStyle style) {
        this.id = id;
        this.style = style;
    }

    protected String getId() {
        return id;
    }

    @Override
    public FontStyle getStyle() {
        return style;
    }

    @Override
    public OutlineFont derive(FontStyle newStyle) {
        Preconditions.checkArgument(style.family().equals(newStyle.family()),
            "Font family mismatch: expected " + style.family());

        return new TeaFont(id, newStyle);
    }
}
