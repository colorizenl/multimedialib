//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import lombok.Getter;
import nl.colorize.multimedialib.stage.FontStyle;
import nl.colorize.multimedialib.stage.OutlineFont;
import nl.colorize.util.Platform;

public class GDXBitmapFont implements OutlineFont {

    private GDXMediaLoader fontLoader;
    private FileHandle source;
    @Getter private String family;
    @Getter private FontStyle style;
    @Getter private BitmapFont bitmapFont;
    @Getter private float lineOffset;

    protected GDXBitmapFont(GDXMediaLoader fontLoader, FileHandle source, String family, FontStyle style) {
        this.fontLoader = fontLoader;
        this.source = source;
        this.family = family;
        this.style = style;
        this.bitmapFont = fontLoader.getBitmapFont(source, family, style);
        this.lineOffset = style.size() * (0.6f * (Platform.isWindows() ? 0.5f : 1f));
    }

    @Override
    public OutlineFont derive(FontStyle newStyle) {
        return new GDXBitmapFont(fontLoader, source, family, newStyle);
    }
}
