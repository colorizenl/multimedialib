//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.google.common.base.Charsets;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.swing.Utils2D;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Uses APIs from the Java standard library to load media files: Java2D and ImageIO
 * for loading images, Java Sound for loading audio clips, and AWT for loading fonts.
 * These APIs are available on server and desktop platforms, but not on headless
 * server environments and not on Android.
 */
public class StandardMediaLoader implements MediaLoader {

    private Map<TTFont, Font> loadedFonts;

    public StandardMediaLoader() {
        this.loadedFonts = new HashMap<>();
    }

    @Override
    public Image loadImage(FilePointer file) {
        try {
            ResourceFile source = new ResourceFile(file.getPath());
            BufferedImage loadedImage = Utils2D.loadImage(source.openStream());
            BufferedImage compatibleImage = Utils2D.makeImageCompatible(loadedImage);
            return new AWTImage(compatibleImage);
        } catch (IOException e) {
            throw new MediaException("Cannot load image from " + file.getPath(), e);
        }
    }

    @Override
    public Audio loadAudio(FilePointer file) {
        return new MP3(new ResourceFile(file.getPath()));
    }

    @Override
    public TTFont loadFont(String fontFamily, FilePointer file) {
        ResourceFile source = new ResourceFile(file.getPath());
        try (InputStream stream = source.openStream()) {
            Font awtFont = Font.createFont(Font.TRUETYPE_FONT, stream);
            awtFont = awtFont.deriveFont(Font.PLAIN, TTFont.DEFAULT_SIZE);

            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            env.registerFont(awtFont);

            // This ignores the value of the fontFamily parameter and
            // will use whatever font family name defined in the file
            // itself, since this is considered more reliable.
            TTFont font = new TTFont(awtFont.getFamily(), TTFont.DEFAULT_SIZE,
                ColorRGB.BLACK);
            loadedFonts.put(font, awtFont);

            return font;
        } catch (IOException | FontFormatException e) {
            throw new MediaException("Cannot load font from " + file.getPath(), e);
        }
    }

    protected Font getFont(TTFont font) {
        Font awtFont = loadedFonts.get(font);
        if (awtFont == null) {
            awtFont = new Font(font.getFamily(), Font.PLAIN, font.getSize());
        }
        return awtFont;
    }

    @Override
    public String loadText(FilePointer file) {
        ResourceFile resourceFile = new ResourceFile(file.getPath());
        return resourceFile.read(Charsets.UTF_8);
    }

    @Override
    public boolean containsResourceFile(FilePointer file) {
        return new ResourceFile(file.getPath()).exists();
    }
}
