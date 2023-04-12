//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Promise;
import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLImageElement;
import org.teavm.jso.typedarrays.Uint8ClampedArray;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Image implementation that is based on an HTML {@code img} element. Since all
 * browser images are loaded asynchronously, an instance of this class can refer
 * to either the full image, or to an image that is currently being loaded.
 */
public class TeaImage implements Image {

    private Promise<HTMLImageElement> imgPromise;
    private Region region;

    private CanvasRenderingContext2D imageData;

    private static final Region IMAGE_LOADING_REGION = new Region(0, 0, 1, 1);
    private static final Logger LOGGER = LogHelper.getLogger(TeaImage.class);

    protected TeaImage(Promise<HTMLImageElement> imgPromise, Region region) {
        this.imgPromise = imgPromise;
        this.region = region;
    }

    public Promise<HTMLImageElement> getImagePromise() {
        return imgPromise;
    }

    public Optional<HTMLImageElement> getImageElement() {
        return imgPromise.getValue();
    }

    public boolean isLoaded() {
        return imgPromise.getValue().isPresent();
    }

    @Override
    public Region getRegion() {
        if (region != null) {
            return region;
        } else {
            return imgPromise.getValue()
                .map(img -> new Region(0, 0, img.getWidth(), img.getHeight()))
                .orElse(IMAGE_LOADING_REGION);
        }
    }

    @Override
    public TeaImage extractRegion(Region region) {
        return new TeaImage(imgPromise, region);
    }

    @Override
    public ColorRGB getColor(int x, int y) {
        int[] rgba = getImageData(x, y);
        return new ColorRGB(rgba[0], rgba[1], rgba[2]);
    }

    @Override
    public int getAlpha(int x, int y) {
        int[] rgba = getImageData(x, y);
        return Math.round(rgba[3] / 2.55f);
    }

    private int[] getImageData(int x, int y) {
        HTMLImageElement img = imgPromise.getValue().orElse(null);

        if (img == null) {
            return new int[] { 0, 0, 0, 0 };
        }

        if (imageData == null) {
            HTMLDocument document = Window.current().getDocument();
            HTMLCanvasElement canvas = (HTMLCanvasElement) document.createElement("canvas");
            imageData = (CanvasRenderingContext2D) canvas.getContext("2d");
            imageData.drawImage(img, 0, 0);
        }

        Uint8ClampedArray rgba = imageData.getImageData(x, y, 1, 1).getData();
        return new int[] { rgba.get(0), rgba.get(1), rgba.get(2), rgba.get(3) };
    }

    @Override
    public String toString() {
        return imgPromise.getValue()
            .map(HTMLImageElement::getSrc)
            .orElse("<loading>");
    }
}
