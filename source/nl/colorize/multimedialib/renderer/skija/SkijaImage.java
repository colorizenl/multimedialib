//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.skija;

import io.github.humbleui.skija.Bitmap;
import io.github.humbleui.types.IRect;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Image;

@RequiredArgsConstructor
@Getter
public class SkijaImage implements Image {

    private final io.github.humbleui.skija.Image image;
    private Bitmap bitmap;

    @Override
    public Region getRegion() {
        return new Region(0, 0, image.getWidth(), image.getHeight());
    }

    @Override
    public SkijaImage extractRegion(Region region) {
        IRect xywh = IRect.makeXYWH(region.x(), region.y(), region.width(), region.height());
        return new SkijaImage(image.makeSubset(xywh));
    }

    private void prepareBitmap() {
        if (bitmap == null) {
            bitmap = new Bitmap();
            image.readPixels(bitmap);
        }
    }

    @Override
    public ColorRGB getColor(int x, int y) {
        prepareBitmap();
        int rgba = bitmap.getColor(x, y);
        return new ColorRGB(rgba);
    }

    @Override
    public int getAlpha(int x, int y) {
        prepareBitmap();
        int rgba = bitmap.getColor(x, y);
        int alpha = (rgba >>> 24) & 0xFF;
        return Math.round(alpha / 2.55f);
    }
}
