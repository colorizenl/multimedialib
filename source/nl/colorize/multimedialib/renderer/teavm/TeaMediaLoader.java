//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import lombok.Getter;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.teavm.ThreeBridge.ThreeObject;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.LoadStatus;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.util.LogHelper;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.Subject;
import nl.colorize.util.SubscribableCollection;
import nl.colorize.util.stats.Cache;
import org.teavm.jso.browser.Storage;
import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLAudioElement;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLImageElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Delegates media loading to the browser. Images, audio, and fonts are loaded
 * using the conventional browser APIs. Text files are embedded into the HTML
 * during the build, and can therefore be loaded immediately.
 */
public class TeaMediaLoader implements MediaLoader {

    private GraphicsMode graphicsMode;
    private HTMLDocument document;
    private BrowserBridge bridge;
    private String timestamp;

    private List<ResourceFile> manifest;
    @Getter private SubscribableCollection<LoadStatus> loadStatus;
    private Cache<MaskImage, HTMLCanvasElement> maskImageCache;

    private static final ResourceFile MANIFEST_FILE = new ResourceFile("resource-file-manifest");
    private static final Splitter LINE_SPLITTER = Splitter.on("\n").trimResults().omitEmptyStrings();
    private static final int IMAGE_CACHE_SIZE = 500;
    private static final Logger LOGGER = LogHelper.getLogger(TeaMediaLoader.class);

    protected TeaMediaLoader(GraphicsMode graphicsMode) {
        this.graphicsMode = graphicsMode;
        this.document = Window.current().getDocument();
        this.bridge = Browser.getBrowserBridge();
        this.timestamp = bridge.getMeta("build-id", String.valueOf(System.currentTimeMillis()));

        this.manifest = Collections.emptyList();
        this.loadStatus = SubscribableCollection.wrap(new ArrayList<>());
        this.maskImageCache = Cache.from(this::createMaskImage, IMAGE_CACHE_SIZE);
    }

    @Override
    public Image loadImage(ResourceFile file) {
        HTMLImageElement imageElement = (HTMLImageElement) document.createElement("img");
        Subject<HTMLImageElement> imagePromise = new Subject<>();
        loadStatus.add(LoadStatus.track(file, imagePromise));
        imageElement.setCrossOrigin("anonymous");
        imageElement.addEventListener("load", event -> imagePromise.next(imageElement));
        imageElement.setSrc(getResourceFileURL(file));
        return new TeaImage(imagePromise, null);
    }

    @Override
    public Audio loadAudio(ResourceFile file) {
        HTMLAudioElement audioElement = (HTMLAudioElement) document.createElement("audio");
        Subject<HTMLAudioElement> audioPromise = new Subject<>();
        loadStatus.add(LoadStatus.track(file, audioPromise));
        audioElement.setCrossOrigin("anonymous");
        audioElement.addEventListener("loadeddata", event -> audioPromise.next(audioElement));
        audioElement.setSrc(getResourceFileURL(file));
        return new TeaAudio(audioPromise);
    }

    @Override
    public FontFace loadFont(ResourceFile file, String family, int size, ColorRGB color) {
        String url = "url('" + getResourceFileURL(file) + "')";
        Subject<String> promise = new Subject<>();
        loadStatus.add(LoadStatus.track(file, promise));

        bridge.preloadFontFace(family, url, error -> {
            promise.next(url);
            if (error != null && !error.isEmpty()) {
                LOGGER.warning("Failed to load font '" + family + "': " + error);
            }
        });

        return new FontFace(file, family, size, color);
    }

    @Override
    public Mesh loadModel(ResourceFile file) {
        if (graphicsMode != GraphicsMode.MODE_3D) {
            throw new UnsupportedOperationException("Renderer does not support 3D graphics");
        }

        //TODO this is now hard-coded to the Three.js renderer
        //     when you use 3D graphics.
        Subject<ThreeObject> modelPromise = new Subject<>();

        if (file.getName().endsWith(".gltf")) {
            Browser.getThreeBridge().loadGLTF(getResourceFileURL(file), modelPromise::next);
        } else if (file.getName().endsWith(".obj")) {
            Browser.getThreeBridge().loadOBJ(getResourceFileURL(file), modelPromise::next);
        } else {
            throw new IllegalArgumentException("Unknown model format: " + file);
        }

        loadStatus.add(LoadStatus.track(file, modelPromise));
        return new ThreeMeshWrapper(modelPromise);
    }

    @Override
    public String loadText(ResourceFile file) {
        HTMLElement resource = document.getElementById(normalizeFilePath(file, true));
        if (resource == null) {
            throw new MediaException("Unknown text resource file: " + file);
        }
        return resource.getInnerText().trim();
    }

    @Override
    public boolean containsResourceFile(ResourceFile file) {
        String fileEntry = file.path().contains("/")
            ? file.path().substring(file.path().lastIndexOf("/") + 1)
            : file.path();

        return loadResourceFileManifest().stream()
            .anyMatch(entry -> entry.path().equals(fileEntry));
    }

    private List<ResourceFile> loadResourceFileManifest() {
        if (!manifest.isEmpty()) {
            return manifest;
        }

        manifest = LINE_SPLITTER.splitToList(loadText(MANIFEST_FILE)).stream()
            .map(path -> new ResourceFile(path))
            .toList();

        return manifest;
    }

    private String getResourceFileURL(ResourceFile file) {
        return "resources/" + normalizeFilePath(file, false) + "?t=" + timestamp;
    }

    protected String normalizeFilePath(ResourceFile file, boolean replaceDot) {
        String normalized = file.path();
        if (normalized.indexOf('/') != -1) {
            normalized = normalized.substring(normalized.lastIndexOf('/') + 1);
        }
        if (replaceDot) {
            normalized = normalized.replace(".", "_");
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
