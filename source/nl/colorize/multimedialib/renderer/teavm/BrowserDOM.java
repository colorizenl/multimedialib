//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.util.http.PostData;
import nl.colorize.util.stats.Cache;
import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLImageElement;

/**
 * Provides access to the browser's DOM contents. This is always available to
 * the TeaVM renderer, regardless of which graphics library is used.
 * <p>
 * Note this class is different from {@link Browser}, which can be used to
 * access the browser's native JavaScript API. The reason these are two
 * separate classes is purely because TeaVM does not allow "native"
 * implementations and Java implementations to be mixed within the same class.
 */
public class BrowserDOM {

    private Cache<MaskImage, HTMLCanvasElement> maskImageCache;

    private static final int IMAGE_CACHE_SIZE = 500;

    public BrowserDOM() {
        this.maskImageCache = Cache.from(this::createMaskImage, IMAGE_CACHE_SIZE);
    }

    public HTMLCanvasElement createFullScreenCanvas(HTMLElement container) {
        Window window = Window.current();
        HTMLDocument document = window.getDocument();

        HTMLCanvasElement canvas = (HTMLCanvasElement) document.createElement("canvas");
        container.appendChild(canvas);
        resizeCanvas(canvas, container);
        window.addEventListener("resize", e -> resizeCanvas(canvas, container));

        return canvas;
    }

    private void resizeCanvas(HTMLCanvasElement canvas, HTMLElement container) {
        HTMLDocument document = Window.current().getDocument();
        int width = container.getOffsetWidth();
        int height = document.getDocumentElement().getClientHeight();

        resizeCanvas(canvas, width, height);
    }

    private void resizeCanvas(HTMLCanvasElement canvas, int width, int height) {
        float devicePixelRatio = (float) Window.current().getDevicePixelRatio();

        canvas.getStyle().setProperty("width", width + "px");
        canvas.getStyle().setProperty("height", height + "px");
        canvas.setWidth(Math.round(width * devicePixelRatio));
        canvas.setHeight(Math.round(height * devicePixelRatio));
    }

    /**
     * Creates an alternative version of the image with the specified mask
     * color applied to every non-transparent pixel. This is a relatively heavy
     * operation, so masked images are cached to avoid having to create them
     * every single frame.
     */
    public HTMLCanvasElement applyMask(TeaImage image, ColorRGB mask) {
        MaskImage cacheKey = new MaskImage(image, mask);
        return maskImageCache.get(cacheKey);
    }

    private HTMLCanvasElement createMaskImage(MaskImage key) {
        Preconditions.checkState(key.image.isLoaded(), "Image is still loading");

        HTMLDocument document = Window.current().getDocument();
        HTMLImageElement img = key.image().getImageElement().get();

        HTMLCanvasElement canvas = (HTMLCanvasElement) document.createElement("canvas");
        canvas.setWidth(img.getWidth());
        canvas.setHeight(img.getHeight());

        CanvasRenderingContext2D maskContext = (CanvasRenderingContext2D) canvas.getContext("2d");
        maskContext.drawImage(img, 0, 0, img.getWidth(), img.getHeight());
        maskContext.setGlobalCompositeOperation("source-atop");
        maskContext.setFillStyle(key.mask().toHex());
        maskContext.fillRect(0, 0, img.getWidth(), img.getHeight());

        return canvas;
    }

    public HTMLCanvasElement createColorCanvas(int width, int height, ColorRGB color) {
        HTMLDocument document = Window.current().getDocument();
        HTMLCanvasElement canvas = (HTMLCanvasElement) document.createElement("canvas");
        resizeCanvas(canvas, width, height);

        CanvasRenderingContext2D context = (CanvasRenderingContext2D) canvas.getContext("2d");
        context.setFillStyle(color.toHex());
        context.fillRect(0, 0, width, height);

        return canvas;
    }

    /**
     * Parses the current URL's query string visible in the browser, and
     * returns the result as a {@link PostData} instance.
     */
    public static PostData getQueryString() {
        String queryString = Window.current().getLocation().getSearch();
        return PostData.parse(queryString, Charsets.UTF_8);
    }

    /**
     * Used as a cache key for masking images. The entire image is masked,
     * not just the image region. If we need a masked region, we just extract
     * the corresponding region from the masked image.
     */
    private record MaskImage(TeaImage image, ColorRGB mask) {
    }
}
