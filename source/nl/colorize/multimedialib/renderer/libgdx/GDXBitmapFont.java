//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.stage.FontStyle;
import nl.colorize.multimedialib.stage.OutlineFont;

public class GDXBitmapFont implements OutlineFont {

    private GDXMediaLoader fontLoader;
    private FileHandle source;
    private FontStyle style;
    private BitmapFont bitmapFont;

    protected GDXBitmapFont(GDXMediaLoader fontLoader, FileHandle source, FontStyle style) {
        this.fontLoader = fontLoader;
        this.source = source;
        this.style = style;
        this.bitmapFont = fontLoader.getBitmapFont(source, style);
    }

    protected BitmapFont getBitmapFont() {
        return bitmapFont;
    }

    @Override
    public FontStyle getStyle() {
        return style;
    }

    @Override
    public OutlineFont derive(FontStyle newStyle) {
        Preconditions.checkArgument(style.family().equals(newStyle.family()),
            "Font family mismatch: expected " + style.family());

        return new GDXBitmapFont(fontLoader, source, newStyle);
    }
}
