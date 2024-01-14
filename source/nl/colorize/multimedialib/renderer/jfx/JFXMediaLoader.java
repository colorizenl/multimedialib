//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.jfx;

import javafx.scene.image.Image;
import javafx.scene.text.Font;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.GeometryBuilder;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.java2d.StandardMediaLoader;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.FontStyle;
import nl.colorize.multimedialib.stage.PolygonModel;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.stats.Cache;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Media implementation for JavaFX. Everything related to graphics uses a
 * JavaFX-specific implementation that does not rely on AWT, Java2D, or Swing.
 * Everything <em>not</em> related to graphics is delegated to the Java2D
 * renderer.
 */
public class JFXMediaLoader implements MediaLoader {

    private StandardMediaLoader delegate;
    private Cache<FontFace, Font> fontCache;

    public JFXMediaLoader() {
        this.delegate = new StandardMediaLoader();
        this.fontCache = Cache.from(this::loadFont);
    }

    @Override
    public JFXImage loadImage(FilePointer file) {
        ResourceFile source = delegate.toResourceFile(file);

        try (InputStream stream = source.openStream()) {
            Image fxImage = new Image(stream);
            Region region = new Region(0, 0, (int) fxImage.getWidth(), (int) fxImage.getHeight());
            return new JFXImage(fxImage, region);
        } catch (IOException e) {
            throw new MediaException("Could not load image: " + file, e);
        }
    }

    @Override
    public Audio loadAudio(FilePointer file) {
        return delegate.loadAudio(file);
    }

    @Override
    public FontFace loadFont(FilePointer file, String family, FontStyle style) {
        FontFace font = new FontFace(this, file, family, style);
        // Make sure the font is cached.
        getFont(font);
        return font;
    }

    private Font loadFont(FontFace key) {
        ResourceFile source = delegate.toResourceFile(key.origin());

        try (InputStream stream = source.openStream()) {
            return Font.loadFont(stream, key.style().size());
        } catch (IOException e) {
            throw new MediaException("Could not load font: " + key.origin(), e);
        }
    }

    protected Font getFont(FontFace key) {
        return fontCache.get(key);
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
        return delegate.loadText(file);
    }

    @Override
    public boolean containsResourceFile(FilePointer file) {
        return delegate.containsResourceFile(file);
    }

    @Override
    public Properties loadApplicationData(String appName) {
        return delegate.loadApplicationData(appName);
    }

    @Override
    public void saveApplicationData(String appName, Properties data) {
        delegate.saveApplicationData(appName, data);
    }
}
