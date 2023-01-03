//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.base.Splitter;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.FontStyle;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.OutlineFont;
import nl.colorize.multimedialib.stage.PolygonModel;
import nl.colorize.multimedialib.stage.Shader;
import nl.colorize.multimedialib.stage.SpriteAtlas;
import nl.colorize.util.AppProperties;
import nl.colorize.util.Platform;
import nl.colorize.util.PlatformFamily;

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
     * Loads an image from a file. JPEG and PNG images are guaranteed to be
     * supported, though other image formats may also be supported depending
     * on the platform.
     *
     * @throws MediaException if the image format is not supported by the
     *         renderer or by the platform, or if the file does not exist.
     */
    public Image loadImage(FilePointer file);

    /**
     * Loads a sprite atlas based on the libGDX {@code .atlas} file format.
     *
     * @throws MediaException if the image format is not supported by the
     *        renderer or by the platform, or if the file does not exist.
     */
    default SpriteAtlas loadAtlas(FilePointer file) {
        SpriteAtlasLoader atlasLoader = new SpriteAtlasLoader(this);
        return atlasLoader.load(file);
    }

    /**
     * Loads an audio clip from a file. OGG and MP3 files are guaranteed to
     * be supported, though other audio formats may be also be supported
     * depending on the platform.
     *
     * @throws MediaException if the audio format is not supported by the
     *         renderer or by the platform, or if the file does not exist.
     */
    public Audio loadAudio(FilePointer file);

    /**
     * Loads a TrueType or FreeType font and converts it to a format that
     * can be used by the renderer. The loaded font will be attached to the
     * font family name specified in the font style.
     *
     * @throws MediaException if the file does not exist, or if the font
     *         cannot be loaded on the current platform.
     */
    public OutlineFont loadFont(FilePointer file, FontStyle style);

    /**
     * Loads the default font, the open source font Open Sans. This is included
     * in MultimediaLib and therefore guaranteed to be always available.
     */
    default OutlineFont loadDefaultFont(int size, ColorRGB color) {
        FilePointer file = new FilePointer("OpenSans-Regular.ttf");
        FontStyle style = new FontStyle("Open Sans", size, false, color);
        return loadFont(file, style);
    }

    /**
     * Loads a polygon model from the specified file. Only the GLTF format is
     * guaranteed to be supported, other file formats are only supported by
     * specific renderers.
     *
     * @throws MediaException if the model cannot be loaded.
     * @throws UnsupportedGraphicsModeException if the current renderer does
     *         not support 3D graphics.
     */
    public PolygonModel loadModel(FilePointer file);

    /**
     * Provides access to a {@link GeometryBuilder} instance that can be used
     * to create simple 3D geometry in a programmatic way.
     *
     * @throws UnsupportedGraphicsModeException if the current renderer does
     *         not support 3D graphics.
     */
    public GeometryBuilder getGeometryBuilder();

    /**
     * Loads a GLSL shader based on two {@code .glsl} files, one for the vertex
     * shader and one for the fragment shader. If the renderer does not support
     * shaders, this will return a no-op shader implementation.
     *
     * @throws MediaException if the shader cannot be parsed.
     */
    public Shader loadShader(FilePointer vertexShaderFile, FilePointer fragmentShaderFile);

    /**
     * Loads a text-based resource file using UTF-8 encoding.
     *
     * @throws MediaException if the file does not exist.
     */
    public String loadText(FilePointer file);

    /**
     * Loads a text-based resource file using UTF-8 encoding, and returns it as
     * a list of lines.
     *
     * @throws MediaException if the file does not exist.
     */
    default List<String> loadTextLines(FilePointer file) {
        return Splitter.on("\n").splitToList(loadText(file));
    }

    /**
     * Returns whether the specified resource file is available.
     */
    public boolean containsResourceFile(FilePointer file);

    /**
     * Loads application data for the application with the specified name. If no
     * such file exists, new application data will be created. The file will be
     * loaded from the platform's standard location for application data.
     *
     * @throws MediaException if the file cannot be loaded.
     */
    public AppProperties loadApplicationData(String appName, String fileName);

    /**
     * Saves application data for the application with the specified name. The
     * file will be stored to the platform's standard location for application
     * data.
     *
     * @throws MediaException if the file could not be saved.
     */
    public void saveApplicationData(Properties data, String appName, String fileName);

    @Deprecated
    default PlatformFamily getPlatformFamily() {
        return Platform.getPlatformFamily();
    }
}
