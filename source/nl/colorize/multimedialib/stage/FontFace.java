//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.MediaLoader;

/**
 * Describes a TrueType or FreeType font that can be used by the renderer to
 * draw text. The font consists of the font family and the font style.
 * <p>
 * MultimediaLib does not rely on system fonts, any fonts used in the
 * application must be loaded explicitly using
 * {@link MediaLoader#loadFont(FilePointer, String, FontStyle)}, which will
 * loads the font from a resource file packaged with the application.
 * Applications should therefore not attempt to create instances of this class
 * directly, instances are obtained from the {@link MediaLoader} when the font
 * is first loaded.
 */
public record FontFace(FilePointer origin, String family, FontStyle style) {

    public FontFace derive(FontStyle style) {
        return new FontFace(origin, family, style);
    }

    public FontFace derive(int size) {
        return derive(new FontStyle(size, style.bold(), style.color()));
    }

    public FontFace derive(int size, boolean bold) {
        return derive(new FontStyle(size, bold, style.color()));
    }

    public FontFace derive(ColorRGB color) {
        return derive(new FontStyle(style.size(), style.bold(), color));
    }

    /**
     * Derives a version of this font that is scaled to match the specified
     * display mode. If the canvas is zoomed in or zoomed out, the size
     * indicated in the font style should also be scaled accordingly.
     */
    public FontFace scale(Canvas canvas) {
        int actualDisplaySize = Math.round(canvas.getZoomLevel() * style.size());
        FontStyle derivedStyle = new FontStyle(actualDisplaySize, style.bold(), style.color());
        return derive(derivedStyle);
    }

    @Override
    public String toString() {
        return family + " @ " + style;
    }
}
