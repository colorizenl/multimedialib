//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import lombok.AllArgsConstructor;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.teavm.TeaMediaLoader;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.Subject;

import java.util.Properties;

/**
 * {@link MediaLoader} implementation that is used when using the libGDX
 * renderer in the browser via TeaVM. This basically has to build on top
 * of two different media loaders: {@link GDXMediaLoader} and
 * {@link TeaMediaLoader}. This class will delegate to either of those,
 * depending on the resource type.
 */
@AllArgsConstructor
public class GDXBrowserMediaLoader implements MediaLoader {

    private GDXMediaLoader gdxMediaLoader;
    private TeaMediaLoader teaMediaLoader;

    @Override
    public Image loadImage(ResourceFile file) {
        return gdxMediaLoader.loadImage(file);
    }

    @Override
    public Audio loadAudio(ResourceFile file) {
        return teaMediaLoader.loadAudio(file);
    }

    @Override
    public Subject<Audio> getAudioQueue() {
        return teaMediaLoader.getAudioQueue();
    }

    @Override
    public FontFace loadFont(ResourceFile file, String family, int size, ColorRGB color) {
        return gdxMediaLoader.loadFont(file, family, size, color);
    }

    @Override
    public Mesh loadModel(ResourceFile file) {
        return gdxMediaLoader.loadModel(file);
    }

    @Override
    public String loadText(ResourceFile file) {
        return teaMediaLoader.loadText(file);
    }

    @Override
    public boolean containsResourceFile(ResourceFile file) {
        return teaMediaLoader.containsResourceFile(file);
    }

    @Override
    public Properties loadApplicationData(String appName) {
        return teaMediaLoader.loadApplicationData(appName);
    }

    @Override
    public void saveApplicationData(String appName, Properties data) {
        teaMediaLoader.saveApplicationData(appName, data);
    }
}
