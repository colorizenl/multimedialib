//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import nl.colorize.multimedialib.graphics.Audio;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.RendererException;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.swing.Utils2D;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Uses APIs from the Java standard library to load media files: Java2D and ImageIO
 * for loading images, and Java Sound for loading audio clips.
 */
public class StandardMediaLoader implements MediaLoader {

    @Override
    public Image loadImage(ResourceFile source) {
        try {
            BufferedImage loadedImage = Utils2D.loadImage(source.openStream());
            BufferedImage compatibleImage = Utils2D.makeImageCompatible(loadedImage);
            return new Java2DImage(compatibleImage);
        } catch (IOException e) {
            throw new RendererException("Cannot load image from " + source.getPath(), e);
        }
    }

    @Override
    public Audio loadAudio(ResourceFile source) {
        return new JavaSoundPlayer(source);
    }
}
