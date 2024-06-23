//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.Splitter;
import nl.colorize.multimedialib.math.Buffer;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.GeometryBuilder;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.FontStyle;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.LoadStatus;
import nl.colorize.multimedialib.stage.PolygonModel;
import nl.colorize.multimedialib.stage.StageVisitor;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Subscribable;
import org.teavm.jso.browser.Storage;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLAudioElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLImageElement;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Delegates media loading to the browser. Images, audio, and fonts are loaded
 * using the conventional browser APIs. Text files are embedded into the HTML
 * during the build, and can therefore be loaded immediately.
 */
public class TeaMediaLoader implements MediaLoader {

    private HTMLDocument document;
    private StageVisitor graphics;
    private List<FilePointer> manifest;
    private Buffer<LoadStatus> loading;
    private String timestamp;

    private static final FilePointer MANIFEST_FILE = new FilePointer("resource-file-manifest");
    private static final Splitter LINE_SPLITTER = Splitter.on("\n").trimResults().omitEmptyStrings();
    private static final Logger LOGGER = LogHelper.getLogger(TeaMediaLoader.class);

    protected TeaMediaLoader(StageVisitor graphics) {
        this.document = Window.current().getDocument();
        this.graphics = graphics;
        this.manifest = Collections.emptyList();
        this.loading = new Buffer<>();
        this.timestamp = Browser.getMeta("build-id", String.valueOf(System.currentTimeMillis()));
    }

    @Override
    public Image loadImage(FilePointer file) {
        HTMLImageElement imageElement = (HTMLImageElement) document.createElement("img");
        Subscribable<HTMLImageElement> imagePromise = new Subscribable<>();
        loading.push(LoadStatus.track(file, imagePromise));
        imageElement.setCrossOrigin("anonymous");
        imageElement.addEventListener("load", event -> imagePromise.next(imageElement));
        imageElement.setSrc("resources/" + normalizeFilePath(file, false) + "?t=" + timestamp);
        return new TeaImage(imagePromise, null);
    }

    @Override
    public Audio loadAudio(FilePointer file) {
        HTMLAudioElement audioElement = (HTMLAudioElement) document.createElement("audio");
        Subscribable<HTMLAudioElement> audioPromise = new Subscribable<>();
        loading.push(LoadStatus.track(file, audioPromise));
        audioElement.setCrossOrigin("anonymous");
        audioElement.addEventListener("loadeddata", event -> audioPromise.next(audioElement));
        audioElement.setSrc("resources/" + normalizeFilePath(file, false) + "?t=" + timestamp);
        return new TeaAudio(audioPromise);
    }

    @Override
    public FontFace loadFont(FilePointer file, String family, FontStyle style) {
        String url = "url('resources/" + normalizeFilePath(file, false) + "')";
        AtomicBoolean loaded = new AtomicBoolean(false);
        loading.push(new LoadStatus(file, loaded::get));

        Browser.preloadFontFace(family, url, error -> {
            loaded.set(true);
            if (error != null && !error.isEmpty()) {
                LOGGER.warning("Failed to load font '" + family + "': " + error);
            }
        });

        return new FontFace(file, family, style);
    }

    @Override
    public PolygonModel loadModel(FilePointer file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GeometryBuilder getGeometryBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String loadText(FilePointer file) {
        HTMLElement resource = document.getElementById(normalizeFilePath(file, true));
        if (resource == null) {
            throw new MediaException("Unknown text resource file: " + file);
        }
        return resource.getInnerText().trim();
    }

    @Override
    public boolean containsResourceFile(FilePointer file) {
        String fileEntry = file.path().contains("/")
            ? file.path().substring(file.path().lastIndexOf("/") + 1)
            : file.path();

        return loadResourceFileManifest().stream()
            .anyMatch(entry -> entry.path().equals(fileEntry));
    }

    private List<FilePointer> loadResourceFileManifest() {
        if (!manifest.isEmpty()) {
            return manifest;
        }

        manifest = LINE_SPLITTER.splitToList(loadText(MANIFEST_FILE)).stream()
            .map(path -> new FilePointer(path))
            .toList();

        return manifest;
    }

    protected String normalizeFilePath(FilePointer file, boolean replaceDot) {
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
        Browser.loadApplicationData(appName);
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
            Browser.saveApplicationData(appName, name, value);
        }
    }

    @Override
    public Buffer<LoadStatus> getLoadStatus() {
        return loading;
    }
}
