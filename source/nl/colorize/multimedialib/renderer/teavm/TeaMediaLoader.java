//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.Splitter;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.GeometryBuilder;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.UnsupportedGraphicsModeException;
import nl.colorize.multimedialib.renderer.pixi.PixiGraphics;
import nl.colorize.multimedialib.renderer.pixi.PixiShader;
import nl.colorize.multimedialib.renderer.three.ThreeGraphics;
import nl.colorize.multimedialib.renderer.three.ThreeShader;
import nl.colorize.multimedialib.stage.FontStyle;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.OutlineFont;
import nl.colorize.multimedialib.stage.PolygonModel;
import nl.colorize.multimedialib.stage.Shader;
import nl.colorize.multimedialib.stage.StageVisitor;
import nl.colorize.util.AppProperties;
import nl.colorize.util.LoadUtils;
import nl.colorize.util.PlatformFamily;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Delegates media loading to the browser. Images, audio, and fonts are loaded
 * using the conventional browser APIs. Text files are embedded into the HTML
 * during the build, and can therefore be loaded immediately.
 */
public class TeaMediaLoader implements MediaLoader {

    private StageVisitor graphics;
    private Map<FilePointer, ProgressTracker> progressTrackers;
    private List<TeaImage> loadedImages;
    private int nextAudioId;
    private int nextFontId;
    private List<FilePointer> manifest;

    private static final FilePointer MANIFEST_FILE = new FilePointer("resource-file-manifest");
    private static final Splitter LINE_SPLITTER = Splitter.on("\n").trimResults().omitEmptyStrings();

    protected TeaMediaLoader(StageVisitor graphics) {
        this.graphics = graphics;
        this.progressTrackers = new LinkedHashMap<>();
        this.loadedImages = new ArrayList<>();
        this.nextAudioId = 1;
        this.nextFontId = 1;
        this.manifest = Collections.emptyList();
    }

    /**
     * Checks the loading status of all resources, and returns true if all
     * previously requested resources have completed loading.In JavaScript
     * all operations that involve loading resources from files are done
     * asynchronously, but managing a scene that is partially loaded would
     * be extremely inconvenient, so this method can be used to suspend the
     * scene until loading has been completed.
     */
    public boolean checkLoadingProgress() {
        Map<FilePointer, ProgressTracker> remaining = new LinkedHashMap<>();

        for (Map.Entry<FilePointer, ProgressTracker> entry : progressTrackers.entrySet()) {
            if (!entry.getValue().isLoadingComplete()) {
                remaining.put(entry.getKey(), entry.getValue());
            }
        }

        progressTrackers = remaining;
        return remaining.isEmpty();
    }

    @Override
    public Image loadImage(FilePointer file) {
        String id = String.valueOf(loadedImages.size() + 1);
        Browser.loadImage(id, "resources/" + normalizeFilePath(file, false));
        
        // The browser will load the image asynchronously, but we can
        // already return a pointer that will be used to retrieve the
        // image information once it becomes available.
        TeaImage imagePointer = new TeaImage(id, file, null);
        loadedImages.add(imagePointer);
        progressTrackers.put(file, () -> Browser.getImageHeight(id) > 0);
        return imagePointer;
    }

    @Override
    public Audio loadAudio(FilePointer file) {
        String id = String.valueOf(nextAudioId);
        Browser.loadAudio(id, "resources/" + normalizeFilePath(file, false));
        nextAudioId++;
        return new TeaAudio(id);
    }

    @Override
    public OutlineFont loadFont(FilePointer file, FontStyle style) {
        String id = String.valueOf(nextFontId);
        Browser.loadFont(id, "resources/" + normalizeFilePath(file, false), style.family());
        nextFontId++;
        return new TeaFont(id, style);
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
        return Browser.loadTextResourceFile(normalizeFilePath(file, true));
    }

    @Override
    public boolean containsResourceFile(FilePointer file) {
        String fileEntry = file.path();
        if (fileEntry.indexOf('/') != -1) {
            fileEntry = fileEntry.substring(fileEntry.lastIndexOf('/') + 1);
        }

        for (FilePointer entry : loadResourceFileManifest()) {
            if (entry.path().equals(fileEntry)) {
                return true;
            }
        }

        return false;
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
    public AppProperties loadApplicationData(String appName, String fileName) {
        String value = Browser.getLocalStorage(appName + "." + fileName);
        if (value == null || value.isEmpty()) {
            return AppProperties.from(new Properties());
        }

        //TODO this doesn't support non-ASCII characters, TeaVM
        //     does not support Properties.load(Reader).
        try (ByteArrayInputStream stream = new ByteArrayInputStream(value.getBytes(UTF_8))) {
            Properties properties = new Properties();
            properties.load(stream);
            return AppProperties.from(properties);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void saveApplicationData(Properties data, String appName, String fileName) {
        String serializedData = LoadUtils.serializeProperties(data);
        Browser.setLocalStorage(appName + "." + fileName, serializedData);
    }

    /**
     * Returns the underlying platform. This will not return a generic
     * "browser" or "web" value, but instead return the platform that is
     * running the browser. The detection is based on the browser's User-Agent
     * header.
     */
    @Override
    public PlatformFamily getPlatformFamily() {
        String userAgent = Browser.getUserAgent().toLowerCase();

        if (userAgent.contains("iphone") || userAgent.contains("ipad")) {
            return PlatformFamily.IOS;
        } else if (userAgent.contains("android")) {
            return PlatformFamily.ANDROID;
        } else if (userAgent.contains("mac")) {
            return PlatformFamily.MAC;
        } else {
            return PlatformFamily.WINDOWS;
        }
    }

    /**
     * Callback interface for polling whether a resource is still loading.
     */
    @FunctionalInterface
    private static interface ProgressTracker {

        public boolean isLoadingComplete();
    }
}
