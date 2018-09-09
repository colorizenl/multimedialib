//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.graphics.Audio;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.util.ResourceFile;

/**
 * Loads media files such as images or audio in a format that can be played
 * by the renderer.
 */
public interface MediaLoader {

    /**
     * Loads an image from a file.
     * @throws RendererException if the image format is not supported by the
     *         renderer.
     */
    public Image loadImage(ResourceFile source);

    /**
     * Loads an audio clip from a file.
     * @throws RendererException if the audio format is not supported by the
     *         renderer.
     */
    public Audio loadAudio(ResourceFile source);
}
