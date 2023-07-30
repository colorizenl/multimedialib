//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.MediaLoader;

/**
 * Represents a TrueType or FreeType font that can be used to draw text. Fonts
 * can be loaded  using the {@link MediaLoader}. Once the font has been loaded,
 * variants with a different size, style, or color can be derived from the font
 * instance.
 */
public interface OutlineFont {

    public FontStyle getStyle();

    /**
     * Creates a new variant of this font using the specified font style. If
     * the requested font style was previously used, a cached version of the
     * font will be returned. If this is the first time the font style is used,
     * the new variant will be loaded or generated.
     *
     * @throws IllegalArgumentException if the provides font style is for a
     *         different font family.
     */
    public OutlineFont derive(FontStyle style);

    default OutlineFont derive(int size) {
        FontStyle style = getStyle();
        return derive(new FontStyle(style.family(), size, style.bold(), style.color()));
    }

    default OutlineFont derive(int size, boolean bold) {
        FontStyle style = getStyle();
        return derive(new FontStyle(style.family(), size, bold, style.color()));
    }

    default OutlineFont derive(ColorRGB color) {
        FontStyle style = getStyle();
        return derive(new FontStyle(style.family(), style.size(), style.bold(), color));
    }

    /**
     * Derives a version of this font that is scaled to match the specified
     * display mode. If the canvas is zoomed in or zoomed out, the size
     * indicated in the font style should also be scaled accordingly.
     */
    default OutlineFont scale(Canvas canvas) {
        FontStyle style = getStyle();
        int actualDisplaySize = Math.round(canvas.getZoomLevel() * style.size());
        return derive(new FontStyle(style.family(), actualDisplaySize, style.bold(), style.color()));
    }
}
