//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.Preconditions;
import lombok.Getter;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Promise;
import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.canvas.ImageData;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLImageElement;
import org.teavm.jso.typedarrays.Uint8ClampedArray;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Image implementation that is based on an HTML {@code img} element. Since all
 * browser images are loaded asynchronously, an instance of this class can refer
 * to either the full image, or to an image that is currently being loaded.
 */
public class TeaImage implements Image {

    @Getter private UUID id;
    @Getter private Promise<HTMLImageElement> imagePromise;
    private Region region;

    private CanvasRenderingContext2D imageData;

    private static final Region IMAGE_LOADING_REGION = new Region(0, 0, 1, 1);
    private static final int[] UNKNOWN_RGBA = {0, 0, 0, 0};
    private static final Logger LOGGER = LogHelper.getLogger(TeaImage.class);

    protected TeaImage(Promise<HTMLImageElement> imagePromise, Region region) {
        this.id = UUID.randomUUID();
        this.imagePromise = imagePromise;
        this.region = region;
    }

    public Optional<HTMLImageElement> getImageElement() {
        return imagePromise.getValue();
    }

    public boolean isLoaded() {
        return imagePromise.getValue().isPresent();
    }

    @Override
    public Region getRegion() {
        if (region == null) {
            if (imagePromise.getValue().isEmpty()) {
                return IMAGE_LOADING_REGION;
            }

            HTMLImageElement imageElement = imagePromise.getValue().get();
            region = new Region(0, 0, imageElement.getWidth(), imageElement.getHeight());
        }

        return region;
    }

    @Override
    public TeaImage extractRegion(Region region) {
        return new TeaImage(imagePromise, region);
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
        HTMLImageElement img = imagePromise.getValue().orElse(null);
        Region region = getRegion();

        if (img == null) {
            LOGGER.warning("Trying to retrieve image data when image is still loading");
            return UNKNOWN_RGBA;
        }

        if (imageData == null) {
            HTMLDocument document = Window.current().getDocument();
            HTMLCanvasElement canvas = (HTMLCanvasElement) document.createElement("canvas");
            canvas.setWidth(region.width());
            canvas.setHeight(region.height());
            imageData = (CanvasRenderingContext2D) canvas.getContext("2d");
            imageData.drawImage(img, 0, 0);
        }

        ImageData pixels = imageData.getImageData(x, y, 1, 1);
        Uint8ClampedArray pixelData = pixels.getData();
        return new int[] {pixelData.get(0), pixelData.get(1), pixelData.get(2), pixelData.get(3)};
    }

    /**
     * Returns a {@link TeaImage} instance that uses the same {@code <img>}
     * element as this instance, but points to the entire image rather than
     * a specific image region.
     *
     * @throws IllegalStateException if the underlying {@code <img>} is still
     *         loading, and this image therefore is not available yet.
     */
    public TeaImage forParentImage() {
        Preconditions.checkState(isLoaded(), "Image is still loading");

        if (region == null) {
            return this;
        } else {
            return new TeaImage(imagePromise, null);
        }
    }

    public boolean isFullImage() {
        return region == null;
    }

    @Override
    public String toString() {
        HTMLImageElement imageElement = imagePromise.getValue().orElse(null);
        if (imageElement == null) {
            return "<loading>";
        }
        return imageElement.getSrc();
    }
}
