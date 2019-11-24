//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.Splitter;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.MediaLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TeaMediaLoader implements MediaLoader {

    private Map<Image, String> imageIds;
    private Map<Audio, String> audioIds;
    private Map<TTFont, String> fontIds;

    private static final Splitter LINE_SPLITTER = Splitter.on("\n").trimResults().omitEmptyStrings();

    public TeaMediaLoader() {
        this.imageIds = new HashMap<>();
        this.audioIds = new HashMap<>();
        this.fontIds = new HashMap<>();
    }

    @Override
    public Image loadImage(FilePointer file) {
        String id = String.valueOf(imageIds.size() + 1);
        Browser.loadImage(id, normalizeFilePath(file, false));

        // The browser will load the image asynchronously, but we can
        // already return a pointer that will be used to retrieve the
        // image information once it becomes available.
        TeaImage imagePointer = new TeaImage(id, normalizeFilePath(file, false), null);
        imageIds.put(imagePointer, id);
        return imagePointer;
    }

    @Override
    public Audio loadAudio(FilePointer file) {
        String id = String.valueOf(audioIds.size() + 1);
        Browser.loadAudio(id, normalizeFilePath(file, false));
        return new TeaAudio(id);
    }

    @Override
    public TTFont loadFont(String fontFamily, FilePointer file) {
        String id = String.valueOf(fontIds.size() + 1);
        Browser.loadFont(id, normalizeFilePath(file, false), fontFamily);
        return new TTFont(fontFamily, TTFont.DEFAULT_SIZE, ColorRGB.BLACK);
    }

    @Override
    public String loadText(FilePointer file) {
        return Browser.loadTextResourceFile(normalizeFilePath(file, true));
    }

    private List<FilePointer> loadResourceFileManifest() {
        String manifest = loadText(new FilePointer("resource-file-manifest"));

        return LINE_SPLITTER.splitToList(manifest).stream()
            .map(path -> new FilePointer(path))
            .collect(Collectors.toList());
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

    private String normalizeFilePath(FilePointer file, boolean replaceDot) {
        String normalized = file.getPath();
        if (normalized.indexOf('/') != -1) {
            normalized = normalized.substring(normalized.lastIndexOf('/') + 1);
        }
        if (replaceDot) {
            normalized = normalized.replace(".", "_");
        }
        return normalized;
    }
}
