//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.Splitter;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.GeometryBuilder;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.UnsupportedGraphicsModeException;
import nl.colorize.multimedialib.renderer.three.ThreeGraphics;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.multimedialib.stage.FontStyle;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.OutlineFont;
import nl.colorize.multimedialib.stage.PolygonModel;
import nl.colorize.multimedialib.stage.StageVisitor;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Promise;
import org.teavm.jso.browser.Storage;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLAudioElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLImageElement;

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

    private HTMLDocument document;
    private StageVisitor graphics;
    private List<FilePointer> manifest;

    private static final FilePointer MANIFEST_FILE = new FilePointer("resource-file-manifest");
    private static final Splitter LINE_SPLITTER = Splitter.on("\n").trimResults().omitEmptyStrings();
    private static final Logger LOGGER = LogHelper.getLogger(TeaMediaLoader.class);

    protected TeaMediaLoader(StageVisitor graphics) {
        this.document = Window.current().getDocument();
        this.graphics = graphics;
        this.manifest = Collections.emptyList();
    }

    @Override
    public Image loadImage(FilePointer file) {
        HTMLImageElement imageElement = (HTMLImageElement) document.createElement("img");
        Promise<HTMLImageElement> imagePromise = new Promise<>();
        imageElement.addEventListener("load", event -> imagePromise.resolve(imageElement));
        imageElement.setSrc("resources/" + normalizeFilePath(file, false));
        return new TeaImage(imagePromise, null);
    }

    @Override
    public Audio loadAudio(FilePointer file) {
        HTMLAudioElement audioElement = (HTMLAudioElement) document.createElement("audio");
        Promise<HTMLAudioElement> audioPromise = new Promise<>();
        audioElement.addEventListener("loadeddata", event -> audioPromise.resolve(audioElement));
        audioElement.setSrc("resources/" + normalizeFilePath(file, false));
        return new TeaAudio(audioPromise);
    }

    @Override
    public OutlineFont loadFont(FilePointer file, FontStyle style) {
        Promise<Boolean> fontPromise = new Promise<>();
        String url = "resources/" + normalizeFilePath(file, false);
        FontLoadCallback callback = success -> fontPromise.resolve(success);

        Browser.preloadFontFace(style.family(), url, callback);

        return new TeaFont(fontPromise, style);
    }

    @Override
    public PolygonModel loadModel(FilePointer file) {
        if (graphics instanceof ThreeGraphics three) {
            //TODO
            throw new UnsupportedGraphicsModeException();
        } else {
            throw new UnsupportedGraphicsModeException();
        }
    }

    @Override
    public GeometryBuilder getGeometryBuilder() {
        if (graphics instanceof ThreeGraphics three) {
            //TODO
            throw new UnsupportedGraphicsModeException();
        } else {
            throw new UnsupportedGraphicsModeException();
        }
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
        Storage localStorage = Storage.getLocalStorage();

        for (String name : data.stringPropertyNames()) {
            localStorage.setItem(name, data.getProperty(name, ""));
        }
    }
}
