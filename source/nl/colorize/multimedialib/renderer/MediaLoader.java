//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.base.Splitter;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.LoadStatus;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.multimedialib.stage.SpriteAtlas;
import nl.colorize.util.PropertyUtils;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.SubscribableCollection;
import nl.colorize.util.TranslationBundle;

import java.util.List;
import java.util.Properties;

/**
 * Loads media files such as images or audio in a format that can later be used
 * by the renderer. Using this interface to load resource files is guaranteed
 * to work on all platforms that are supported by MultimediaLib. Using other
 * ways of loading files, such as directly from the file system or from the
 * classpath, may not be supported on some platforms.
 */
public interface MediaLoader {

    /**
     * Loads an image from a file. Images in JPEG and PNG format are supported
     * by all renderers.
     *
     * @throws MediaException if the format is not supported by the renderer.
     */
    public Image loadImage(ResourceFile file);

    /**
     * Loads a sprite atlas based on the libGDX {@code .atlas} file format.
     * This will parse the {@code .atlas} file, and will then load all images
     * used within the sprite atlas.
     *
     * @throws MediaException if one of the images used in the sprite atlas
     *         uses a format that is not supported by the renderer,
     */
    default SpriteAtlas loadAtlas(ResourceFile file) {
        SpriteAtlasLoader atlasLoader = new SpriteAtlasLoader(this);
        return atlasLoader.load(file);
    }

    /**
     * Loads an audio clip from a file. MP3 files are supported by all
     * renderers.
     *
     * @throws MediaException if the format is not supported by the renderer.
     */
    public Audio loadAudio(ResourceFile file);

    /**
     * Loads a TrueType or FreeType font so the renderer can use that font
     * for text rendering. This will return a font for the specified style,
     * alternative styles can be derived from the returned font.
     *
     * @throws MediaException if the format is not supported by the renderer.
     */
    public FontFace loadFont(ResourceFile file, String family, int size, ColorRGB color);

    /**
     * Loads the default font, the open source font Open Sans. This is included
     * in MultimediaLib and therefore guaranteed to be always available.
     */
    default FontFace loadDefaultFont(int size, ColorRGB color) {
        ResourceFile file = new ResourceFile("OpenSans-Regular.ttf");
        return loadFont(file, "Open Sans", size, color);
    }

    /**
     * Loads a polygon model from the specified file. Only the OBJ and GLTF
     * format are guaranteed to be supported, though certain renderers may
     * support additional file formats.
     *
     * @throws MediaException if the format is not supported by this renderer.
     * @throws UnsupportedOperationException if this renderer does not support
     *         3D graphics.
     */
    public Mesh loadModel(ResourceFile file);

    /**
     * Loads a text-based resource file using UTF-8 encoding.
     *
     * @throws MediaException if the file does not exist.
     */
    public String loadText(ResourceFile file);

    /**
     * Loads a text-based resource file using UTF-8 encoding, and returns
     * it as a list of lines.
     *
     * @throws MediaException if the file does not exist.
     */
    default List<String> loadTextLines(ResourceFile file) {
        return Splitter.on("\n").splitToList(loadText(file));
    }

    /**
     * Convenience method that loads and then parses the contents of a
     * {@code .properties} file. By default, reading the file contents
     * is delegated to {@link #loadText(ResourceFile)}.
     */
    default Properties loadProperties(ResourceFile file) {
        String contents = loadText(file);
        return PropertyUtils.loadProperties(contents);
    }

    /**
     * Convenience method that loads and then parses the contents of a
     * {@code .properties} file and returns a {@link TranslationBundle}.
     * By default, reading the file contents is delegated to
     * {@link #loadText(ResourceFile)}.
     */
    default TranslationBundle loadTranslationBundle(ResourceFile file) {
        Properties properties = loadProperties(file);
        return TranslationBundle.from(properties);
    }

    /**
     * Returns whether the specified resource file is available.
     */
    public boolean containsResourceFile(ResourceFile file);

    /**
     * Loads the application data for the application with the specified name.
     * Application data is limited to key/value properties, as this type of
     * data is supported by all platforms. Returns an empty {@link Properties}
     * when no application data exists.
     */
    public Properties loadApplicationData(String appName);

    /**
     * Saves the application data for the application with the specified name.
     * Application data is limited to key/value properties, as this type of
     * data is supported by all platforms.
     */
    public void saveApplicationData(String appName, Properties data);

    /**
     * Returns a buffer containing the load status of all media files that
     * have been loaded by this {@link MediaLoader}.
     */
    public SubscribableCollection<LoadStatus> getLoadStatus();
}
