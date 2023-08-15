//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.stage.FontStyle;
import nl.colorize.multimedialib.stage.OutlineFont;
import nl.colorize.util.Promise;

public class TeaFont implements OutlineFont {

    private Promise<Boolean> fontPromise;
    private FontStyle style;

    protected TeaFont(Promise<Boolean> fontPromise, FontStyle style) {
        this.fontPromise = fontPromise;
        this.style = style;
    }

    public boolean isLoaded() {
        return fontPromise.getValue().isPresent();
    }

    @Override
    public FontStyle getStyle() {
        return style;
    }

    @Override
    public OutlineFont derive(FontStyle newStyle) {
        Preconditions.checkArgument(style.family().equals(newStyle.family()),
            "Font family mismatch: expected " + style.family());

        return new TeaFont(fontPromise, newStyle);
    }

    /**
     * Returns the CSS description of this font, as it would be described in
     * the CSS {@code font} shorthand property.
     */
    public String getFontString() {
        return (style.bold() ? "bold " : "") + style.size() + "px " + style.family();
    }
}
