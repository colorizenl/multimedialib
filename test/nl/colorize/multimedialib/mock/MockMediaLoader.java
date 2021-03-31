//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.PolygonModel;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.GeometryBuilder;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.util.ApplicationData;

import java.util.ArrayList;
import java.util.List;

public class MockMediaLoader implements MediaLoader {

    private List<FilePointer> loaded;

    public MockMediaLoader() {
        this.loaded = new ArrayList<>();
    }

    @Override
    public Image loadImage(FilePointer file) {
        loaded.add(file);
        return new MockImage();
    }

    @Override
    public Audio loadAudio(FilePointer file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TTFont loadFont(FilePointer file, String family, int size, ColorRGB color, boolean bold) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String loadText(FilePointer file) {
        loaded.add(file);
        return "";
    }

    @Override
    public PolygonModel loadModel(FilePointer file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsResourceFile(FilePointer file) {
        return true;
    }

    @Override
    public ApplicationData loadApplicationData(String appName, String fileName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveApplicationData(ApplicationData data, String appName, String fileName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GeometryBuilder getGeometryBuilder() {
        throw new UnsupportedOperationException();
    }

    public List<FilePointer> getLoaded() {
        return loaded;
    }
}
