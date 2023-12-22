//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import lombok.Getter;
import nl.colorize.multimedialib.stage.FontStyle;
import nl.colorize.multimedialib.stage.OutlineFont;

@Getter
public class TeaFont implements OutlineFont {

    private String family;
    private FontStyle style;

    protected TeaFont(String family, FontStyle style) {
        this.family = family;
        this.style = style;
    }

    @Override
    public OutlineFont derive(FontStyle newStyle) {
        return new TeaFont(family, newStyle);
    }

    /**
     * Returns the CSS description of this font, as it would be described in
     * the CSS {@code font} shorthand property.
     */
    public String getFontString() {
        return (style.bold() ? "bold " : "") + style.size() + "px " + family;
    }
}
