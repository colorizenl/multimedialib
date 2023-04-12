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
import nl.colorize.multimedialib.renderer.pixi.PixiGraphics;
import nl.colorize.multimedialib.renderer.pixi.PixiShader;
import nl.colorize.multimedialib.renderer.three.ThreeGraphics;
import nl.colorize.multimedialib.renderer.three.ThreeShader;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.multimedialib.stage.FontStyle;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.OutlineFont;
import nl.colorize.multimedialib.stage.PolygonModel;
import nl.colorize.multimedialib.stage.Shader;
import nl.colorize.multimedialib.stage.StageVisitor;
import nl.colorize.util.LoadUtils;
import nl.colorize.util.Promise;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLAudioElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLImageElement;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

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

    protected TeaMediaLoader(StageVisitor graphics) {
        this.document = Window.current().getDocument();
        this.graphics = graphics;
        this.manifest = Collections.emptyList();
    }

    @Override
    public Image loadImage(FilePointer file) {
        HTMLImageElement img = (HTMLImageElement) document.createElement("img");
        Promise<HTMLImageElement> imgPromise = new Promise<>();
        img.addEventListener("load", event -> imgPromise.resolve(img));
        img.setSrc("resources/" + normalizeFilePath(file, false));
        return new TeaImage(imgPromise, null);
    }

    @Override
    public Audio loadAudio(FilePointer file) {
        HTMLAudioElement audioElement = (HTMLAudioElement) document.createElement("audio");
        audioElement.setSrc("resources/" + normalizeFilePath(file, false));
        return new TeaAudio(audioElement);
    }

    @Override
    public OutlineFont loadFont(FilePointer file, FontStyle style) {
        String css = "";
        css += "@font-face {\n";
        css += "    font-family: '" + style.family() + "';\n";
        css += "    font-style: normal;\n";
        css += "    font-weight: 400;\n";
        css += "    src: url('resources/" + normalizeFilePath(file, false) + "') format('truetype');\n";
        css += "};\n";

        HTMLElement styleElement = document.createElement("style");
        styleElement.appendChild(document.createTextNode(css));

        HTMLElement fontContainer = document.getElementById("fontContainer");
        fontContainer.appendChild(styleElement);

        return new TeaFont(style);
    }

    @Override
    public PolygonModel loadModel(FilePointer file) {
        //TODO
        throw new UnsupportedGraphicsModeException();
    }

    @Override
    public GeometryBuilder getGeometryBuilder() {
        //TODO
        throw new UnsupportedGraphicsModeException();
    }

    @Override
    public Shader loadShader(FilePointer vertexShaderFile, FilePointer fragmentShaderFile) {
        String vertexGLSL = loadText(vertexShaderFile);
        String fragmentGLSL = loadText(fragmentShaderFile);

        if (graphics instanceof PixiGraphics) {
            return new PixiShader(vertexGLSL, fragmentGLSL);
        } else if (graphics instanceof ThreeGraphics) {
            return new ThreeShader(vertexGLSL, fragmentGLSL);
        } else {
            return Shader.NO_OP;
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
            .collect(Collectors.toList());

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
    public Properties loadApplicationData(String appName, String fileName) {
        String value = Browser.getLocalStorage(appName + "." + fileName);

        if (value != null && !value.isEmpty()) {
            return LoadUtils.loadProperties(value);
        } else {
            return new Properties();
        }
    }

    @Override
    public void saveApplicationData(Properties data, String appName, String fileName) {
        String serializedData = LoadUtils.serializeProperties(data);
        Browser.setLocalStorage(appName + "." + fileName, serializedData);
    }
}
