//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.MediaLoader;

/**
 * Describes a TrueType or FreeType font that can be used by the renderer
 * to draw text. The font consists of the font family and the font style.
 * <p>
 * MultimediaLib applications should not make assumptions on which system
 * fonts are available on the current platform. All fonts used by the
 * application should be included in the application's resource files.
 * Fonts can then be loaded from these files using {@link MediaLoader}.
 */
public record FontFace(FilePointer origin, String family, int size, ColorRGB color) {

    public FontFace {
        Preconditions.checkArgument(!family.isEmpty(), "Missing font family");
        Preconditions.checkArgument(size >= 1, "Invalid font size");
    }

    public FontFace derive(int size) {
        return new FontFace(origin, family, size, color);
    }

    public FontFace derive(ColorRGB color) {
        return new FontFace(origin, family, size, color);
    }

    /**
     * Derives a version of this font that is scaled to match the specified
     * display mode. If the canvas is zoomed in or zoomed out, the size
     * indicated in the font style should also be scaled accordingly.
     */
    public FontFace scale(Canvas canvas) {
        int actualDisplaySize = Math.round(canvas.getZoomLevel() * size);
        return derive(actualDisplaySize);
    }

    @Override
    public String toString() {
        return family;
    }
}
