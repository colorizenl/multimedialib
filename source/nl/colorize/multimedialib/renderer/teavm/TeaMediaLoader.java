//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.Preconditions;
import lombok.Getter;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.util.Cache;
import nl.colorize.util.LogHelper;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.Subject;
import org.teavm.jso.browser.Storage;
import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLAudioElement;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLImageElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Delegates media loading to the browser. Images, audio, and fonts are loaded
 * using the conventional browser APIs. Text files are embedded into the HTML
 * during the build and can therefore be loaded immediately.
 * <p>
 * Media files are loaded asynchronously. Media classes (e.g. {@link TeaImage}
 * are able to handle situations where the underlying resource is still
 * loading. However, it is also possible to explicitly preload all media
 * files, which would typically be done at application startup, using
 * {@link #preload()}.
 */
public class TeaMediaLoader implements MediaLoader {

    private HTMLDocument document;
    private BrowserBridge bridge;
    private String timestamp;

    @Getter private Subject<Audio> audioQueue;
    private Map<ResourceFile, HTMLImageElement> preloadedImages;
    private Map<ResourceFile, HTMLAudioElement> preloadedAudio;
    private Map<ResourceFile, String> preloadedFonts;
    private Cache<MaskImage, HTMLCanvasElement> maskImageCache;

    private static final ResourceFile MANIFEST_FILE = new ResourceFile("resource-file-manifest");
    private static final int IMAGE_CACHE_SIZE = 500;
    private static final Logger LOGGER = LogHelper.getLogger(TeaMediaLoader.class);

    public TeaMediaLoader() {
        this.document = Window.current().getDocument();
        this.bridge = Browser.getBrowserBridge();
        this.timestamp = bridge.getMeta("build-id", String.valueOf(System.currentTimeMillis()));

        audioQueue = new Subject<>();
        preloadedImages = new HashMap<>();
        preloadedAudio = new HashMap<>();
        preloadedFonts = new HashMap<>();
        maskImageCache = Cache.from(this::createMaskImage, IMAGE_CACHE_SIZE);
    }

    /**
     * Preloads all media files and returns a {@link Subject} to subscribe to
     * the results. Audio files and text files are exempt: Audio files can be
     * streamed, and text files are "baked" into the HTML and therefore do
     * not need to be preloaded.
     * <p>
     * Subscribers will only be notified once, when all media files have been
     * loaded successfully. Subscribers will also be notified when loading
     * any of the media files fails.
     */
    public Subject<List<ResourceFile>> preload() {
        List<ResourceFile> loading = new CopyOnWriteArrayList<>();
        List<ResourceFile> loaded = new CopyOnWriteArrayList<>();
        Subject<List<ResourceFile>> status = new Subject<>();

        Consumer<ResourceFile> checkStatus = file -> {
            loaded.add(file);
            if (loading.size() == loaded.size()) {
                status.next(loaded);
            }
        };

        for (ResourceFile file : loadResourceManifest()) {
            if (file.path().endsWith(".png") || file.path().endsWith("jpg")) {
                loading.add(file);
                appendImageElement(file).subscribe(_ -> checkStatus.accept(file));
            } else if (file.path().endsWith(".ogg")) {
                loading.add(file);
                appendAudioElement(file).subscribe(_ -> checkStatus.accept(file));
            } else if (file.path().endsWith(".ttf")) {
                loading.add(file);
                appendFont(file).subscribe(_ -> checkStatus.accept(file));
            }
        }

        return status;
    }

    @Override
    public Image loadImage(ResourceFile file) {
        if (preloadedImages.containsKey(file)) {
            HTMLImageElement imageElement = preloadedImages.get(file);
            return new TeaImage(Subject.of(imageElement), null);
        } else {
            Subject<HTMLImageElement> imageElement = appendImageElement(file);
            return new TeaImage(imageElement, null);
        }
    }

    private Subject<HTMLImageElement> appendImageElement(ResourceFile file) {
        Subject<HTMLImageElement> promise = new Subject<>();
        HTMLImageElement imageElement = (HTMLImageElement) document.createElement("img");
        imageElement.setCrossOrigin("anonymous");
        imageElement.addEventListener("load", _ -> {
            preloadedImages.put(file, imageElement);
            promise.next(imageElement);
        });
        imageElement.setSrc(getResourceFileURL(file));
        return promise;
    }

    @Override
    public Audio loadAudio(ResourceFile file) {
        if (preloadedAudio.containsKey(file)) {
            HTMLAudioElement audioElement = preloadedAudio.get(file);
            return new TeaAudio(Subject.of(audioElement), audioQueue);
        } else {
            Subject<HTMLAudioElement> audioElement = appendAudioElement(file);
            return new TeaAudio(audioElement, audioQueue);
        }
    }

    private Subject<HTMLAudioElement> appendAudioElement(ResourceFile file) {
        Subject<HTMLAudioElement> promise = new Subject<>();
        HTMLAudioElement audioElement = (HTMLAudioElement) document.createElement("audio");
        audioElement.setCrossOrigin("anonymous");
        audioElement.setSrc(getResourceFileURL(file));
        // The loading events don't fire on iOS until
        // the audio clip is played for the first time,
        // meaning we cannot use this events to wait
        // for the audio to pre-load.
        preloadedAudio.put(file, audioElement);
        promise.next(audioElement);
        return promise;
    }

    @Override
    public FontFace loadFont(ResourceFile file, String family, int size, ColorRGB color) {
        FontFace fontRef = new FontFace(file, family, size, color);
        if (!preloadedFonts.containsKey(file)) {
            appendFont(file);
        }
        return fontRef;
    }

    private Subject<String> appendFont(ResourceFile file) {
        String id = "MultimediaLib-Font-" + UUID.randomUUID();
        String url = "url('" + getResourceFileURL(file) + "')";
        Subject<String> promise = new Subject<>();

        bridge.preloadFontFace(id, url, error -> {
            preloadedFonts.put(file, id);
            promise.next(id);
            if (error != null && !error.isEmpty()) {
                LOGGER.warning("Failed to load font " + file + ": " + error);
            }
        });

        return promise;
    }

    protected String getFontId(FontFace font) {
        return preloadedFonts.getOrDefault(font.origin(), font.family());
    }

    @Override
    public Mesh loadModel(ResourceFile file) {
        throw new UnsupportedOperationException("Renderer does not support 3D graphics");
    }

    @Override
    public String loadText(ResourceFile file) {
        HTMLElement resource = document.getElementById(normalizeFilePath(file));
        if (resource == null) {
            throw new MediaException("Unknown text resource file: " + file);
        }
        return resource.getInnerText().trim();
    }

    @Override
    public boolean containsResourceFile(ResourceFile file) {
        return loadResourceManifest().contains(file);
    }

    private List<ResourceFile> loadResourceManifest() {
        return loadTextLines(MANIFEST_FILE).stream()
            .filter(line -> !line.isEmpty())
            .map(path -> new ResourceFile(path.trim()))
            .toList();
    }

    private String getResourceFileURL(ResourceFile file) {
        return "assets/" + file.path() + "?t=" + timestamp;
    }

    protected String normalizeFilePath(ResourceFile file) {
        String normalized = file.path().replace(".", "_");
        if (normalized.indexOf('/') != -1) {
            normalized = normalized.substring(normalized.lastIndexOf('/') + 1);
        }
        return normalized;
    }

    @Override
    public Properties loadApplicationData(String appName) {
        bridge.loadApplicationData();
        Storage localStorage = Storage.getLocalStorage();
        Properties data = new Properties();

        for (int i = 0; i < localStorage.getLength(); i++) {
            String name = localStorage.key(i);
            String value = localStorage.getItem(name);
            data.setProperty(name, value);
        }

        return data;
    }

    @Override
    public void saveApplicationData(String appName, Properties data) {
        for (String name : data.stringPropertyNames()) {
            String value = data.getProperty(name);
            bridge.saveApplicationData(name, value);
        }
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
        Preconditions.checkState(key.image().isLoaded(), "Image is still loading");

        HTMLImageElement img = key.image().getImageElement().get();

        HTMLCanvasElement canvas = (HTMLCanvasElement) this.document.createElement("canvas");
        canvas.setWidth(img.getWidth());
        canvas.setHeight(img.getHeight());

        CanvasRenderingContext2D maskContext = (CanvasRenderingContext2D) canvas.getContext("2d");
        maskContext.drawImage(img, 0, 0, img.getWidth(), img.getHeight());
        maskContext.setGlobalCompositeOperation("source-atop");
        maskContext.setFillStyle(key.mask().toHex());
        maskContext.fillRect(0, 0, img.getWidth(), img.getHeight());

        return canvas;
    }

    /**
     * Used as a cache key for masking images. The entire image is masked,
     * not just the image region. If we need a masked region, we just extract
     * the corresponding region from the masked image.
     */
    private record MaskImage(TeaImage image, ColorRGB mask) {
    }
}
