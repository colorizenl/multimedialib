//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.graphics.Audio;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.TrueTypeFont;

/**
 * Loads media files such as images or audio in a format that can later be used
 * by the renderer. Using this interface to load resource files is guaranteed
 * to work on all platforms that are supported by MultimediaLib. Using other
 * ways of loading files, such as directly from the file system or from the
 * classpath, may not be supported on some platforms.
 */
public interface MediaLoader {

    /**
     * Loads an image from a file. Only JPEG and PNG images are guaranteed
     * to be supported, though other image formats may also be supported
     * depending on the platform.
     * @throws MediaException if the image format is not supported by the
     *         renderer or by the platform, or if the file does not exist.
     */
    public Image loadImage(FilePointer file);

    /**
     * Loads an audio clip from a file. Only MP3 files are guaranteed to be
     * supported, though other audio formats may be also be supported
     * depending on the platform.
     * @throws MediaException if the audio format is not supported by the
     *         renderer or by the platform, or if the file does not exist.
     */
    public Audio loadAudio(FilePointer file);

    /**
     * Loads a TrueType font from a {@code .ttf} file.
     * @param fontFamily The logical name of the font family that is being
     *                   loaded.
     * @throws MediaException if the file does not exist, or if the font
     *         cannot be loaded on the current platform.
     */
    public TrueTypeFont loadFont(String fontFamily, FilePointer file);

    /**
     * Loads the default font, the open source font Open Sans. This is included
     * in MultimediaLib and therefore guaranteed to be always available.
     */
    default TrueTypeFont loadDefaultFont() {
        FilePointer file = new FilePointer("OpenSans-Regular.ttf");
        return loadFont("Open Sans", file);
    }

    /**
     * Loads a text-based resource file using UTF-8 encoding. T
     * @throws MediaException if the file does not exist.
     */
    public String loadText(FilePointer file);

    public boolean containsResourceFile(FilePointer file);
}
