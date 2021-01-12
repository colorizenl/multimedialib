//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.Splitter;
import nl.colorize.multimedialib.graphics.AnimationInfo;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.PolygonMesh;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.MediaLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Delegates media loading to the browser. Images, audio, and fonts are loaded
 * using the conventional browser APIs. Text files are embedded into the HTML
 * during the build, and can therefore be loaded immediately.
 */
public class TeaMediaLoader implements MediaLoader {

    private Map<FilePointer, ProgressTracker> progressTrackers;

    private List<TeaImage> loadedImages;
    private int nextAudioId;
    private int nextFontId;

    private static final Splitter LINE_SPLITTER = Splitter.on("\n").trimResults().omitEmptyStrings();

    public TeaMediaLoader() {
        this.progressTrackers = new LinkedHashMap<>();

        this.loadedImages = new ArrayList<>();
        this.nextAudioId = 1;
        this.nextFontId = 1;
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
        Browser.loadImage(id, normalizeFilePath(file, false));
        
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
        Browser.loadAudio(id, normalizeFilePath(file, false));
        nextAudioId++;
        return new TeaAudio(id);
    }

    @Override
    public TTFont loadFont(FilePointer file, String family, int size, ColorRGB color, boolean bold) {
        String id = String.valueOf(nextFontId);
        Browser.loadFont(id, normalizeFilePath(file, false), family);
        nextFontId++;
        return new TTFont(family, size, color, false);
    }

    @Override
    public PolygonMesh loadMesh(FilePointer file) {
        UUID id = UUID.randomUUID();
        PolygonMesh mesh = new PolygonMesh(id, file.getPath(), Collections.emptyList());
        List<String> loadCount = new ArrayList<>();

        Browser.loadModel(id.toString(), file.getPath(), (names, durations) -> {
            for (int i = 0; i < names.length; i++) {
                mesh.addAnimation(new AnimationInfo(names[i], durations[i]));
            }
            loadCount.add(String.valueOf(loadCount.size() + 1));
        });

        progressTrackers.put(file, () -> loadCount.size() > 0);
        return mesh;
    }

    @Override
    public String loadText(FilePointer file) {
        return Browser.loadTextResourceFile(normalizeFilePath(file, true));
    }

    @Override
    public boolean containsResourceFile(FilePointer file) {
        String fileEntry = file.getPath();
        if (fileEntry.indexOf('/') != -1) {
            fileEntry = fileEntry.substring(fileEntry.lastIndexOf('/') + 1);
        }

        for (FilePointer entry : loadResourceFileManifest()) {
            if (entry.getPath().equals(fileEntry)) {
                return true;
            }
        }

        return false;
    }

    private List<FilePointer> loadResourceFileManifest() {
        String manifest = loadText(new FilePointer("resource-file-manifest"));

        return LINE_SPLITTER.splitToList(manifest).stream()
            .map(path -> new FilePointer(path))
            .collect(Collectors.toList());
    }

    protected String normalizeFilePath(FilePointer file, boolean replaceDot) {
        String normalized = file.getPath();
        if (normalized.indexOf('/') != -1) {
            normalized = normalized.substring(normalized.lastIndexOf('/') + 1);
        }
        if (replaceDot) {
            normalized = normalized.replace(".", "_");
        }
        return normalized;
    }

    /**
     * Callback interface for polling whether a resource is still loading.
     */
    @FunctionalInterface
    private static interface ProgressTracker {

        public boolean isLoadingComplete();
    }
}
