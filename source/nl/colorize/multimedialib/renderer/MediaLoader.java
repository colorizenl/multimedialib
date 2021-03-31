//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.PolygonModel;
import nl.colorize.multimedialib.graphics.SpriteSheet;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.util.ApplicationData;
import nl.colorize.util.CSVRecord;

import java.util.List;

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
     * Convenience method that loads a sprite sheet from its image and its
     * metadata file in CSV format. The sprite sheet is assumed to have been
     * created using the {@code SpriteSheetPackerTool}.
     * <p>
     * Note that no convenience method for loading sprite sheets with YAML
     * metadata is provided, as this library does not include out-of-the-box
     * support for YAML.
     *
     * @throws MediaException if the sprite sheet cannot be loaded.
     * @throws IllegalArgumentException when attempting to load non-CSV metadata.
     */
    default SpriteSheet loadSpriteSheetCSV(FilePointer imageFile, FilePointer metadataFile) {
        Preconditions.checkArgument(metadataFile.getPath().endsWith(".csv"),
            "Only sprite sheet metadata in CSV format is supported");

        Image image = loadImage(imageFile);
        List<CSVRecord> metadata = CSVRecord.parseRecords(loadText(metadataFile), ";", true);

        SpriteSheet spriteSheet = new SpriteSheet(image);
        for (CSVRecord region : metadata) {
            Rect bounds = new Rect(region.getFloat(1), region.getFloat(2), region.getFloat(3),
                region.getFloat(4));
            spriteSheet.markRegion(region.get(0), bounds);
        }
        return spriteSheet;
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
     * Loads a TrueType font from a {@code .ttf} file and converts it to a
     * format that can be used by the renderer.
     *
     * @throws MediaException if the file does not exist, or if the font
     *         cannot be loaded on the current platform.
     */
    public TTFont loadFont(FilePointer file, String family, int size, ColorRGB color, boolean bold);

    /**
     * Loads a TrueType font from a {@code .ttf} file and converts it to a
     * format that can be used by the renderer.
     *
     * @throws MediaException if the file does not exist, or if the font
     *         cannot be loaded on the current platform.
     */
    default TTFont loadFont(FilePointer file, String family, int size, ColorRGB color) {
        return loadFont(file, family, size, color, false);
    }

    /**
     * Loads the default font, the open source font Open Sans. This is included
     * in MultimediaLib and therefore guaranteed to be always available.
     */
    default TTFont loadDefaultFont(int size, ColorRGB color) {
        FilePointer file = new FilePointer("OpenSans-Regular.ttf");
        return loadFont(file, "Open Sans", size, color, false);
    }

    /**
     * Loads a polygon model from the specified file. Only the GLTF format is
     * guaranteed to be supported, other file formats are only supported by
     * specific renderers.
     *
     * @throws MediaException if the model cannot be loaded.
     * @throws UnsupportedGraphicsModeException if the renderer does not support
     *         3D graphics.
     */
    public PolygonModel loadModel(FilePointer file);

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
    public ApplicationData loadApplicationData(String appName, String fileName);

    /**
     * Saves application data back to the file with the specified name. The file
     * will be stored to the platform's standard location for application data.
     *
     * @throws MediaException if the file could not be saved.
     */
    public void saveApplicationData(ApplicationData data, String appName, String fileName);

    /**
     * Access to a {@link GeometryBuilder} that can be used to create 3D models
     * programatically.
     *
     * @throws UnsupportedGraphicsModeException if the renderer does not support
     *         3D graphics.
     */
    public GeometryBuilder getGeometryBuilder();
}
