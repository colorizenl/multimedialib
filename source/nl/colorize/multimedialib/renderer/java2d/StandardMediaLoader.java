//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.google.common.base.Charsets;
import com.google.common.reflect.ClassPath;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.PolygonModel;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.UnsupportedGraphicsModeException;
import nl.colorize.util.Configuration;
import nl.colorize.util.LoadUtils;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.swing.Utils2D;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Uses APIs from the Java standard library to load media files: Java2D and ImageIO
 * for loading images, Java Sound for loading audio clips, and AWT for loading fonts.
 * These APIs are available on server and desktop platforms, but not on headless
 * server environments and not on Android.
 */
public class StandardMediaLoader implements MediaLoader {

    private Map<TTFont, Font> loadedFonts;
    private Set<String> classPathResources;

    private static final Logger LOGGER = LogHelper.getLogger(StandardMediaLoader.class);

    public StandardMediaLoader() {
        this.loadedFonts = new HashMap<>();
        this.classPathResources = Collections.emptySet();

        try {
            ClassLoader classLoader = StandardMediaLoader.class.getClassLoader();

            classPathResources = ClassPath.from(classLoader).getResources().stream()
                .map(resource -> resource.getResourceName())
                .collect(Collectors.toSet());;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to preload classpath resources", e);
        }
    }

    @Override
    public Image loadImage(FilePointer file) {
        try {
            ResourceFile source = new ResourceFile(file.getPath());
            BufferedImage loadedImage = Utils2D.loadImage(source.openStream());
            return new AWTImage(loadedImage, file);
        } catch (IOException e) {
            throw new MediaException("Cannot load image from " + file.getPath(), e);
        }
    }

    @Override
    public Audio loadAudio(FilePointer file) {
        return new MP3(new ResourceFile(file.getPath()));
    }

    @Override
    public TTFont loadFont(FilePointer file, String family, int size, ColorRGB color, boolean bold) {
        ResourceFile source = new ResourceFile(file.getPath());
        int style = bold ? Font.BOLD : Font.PLAIN;

        try (InputStream stream = source.openStream()) {
            Font awtFont = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(style, size);

            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            env.registerFont(awtFont);

            // This ignores the value of the fontFamily parameter and
            // will use whatever font family name defined in the file
            // itself, since this is considered more reliable.
            TTFont font = new TTFont(awtFont.getFamily(), size, color, bold);
            loadedFonts.put(font, awtFont);
            return font;
        } catch (IOException | FontFormatException e) {
            throw new MediaException("Cannot load font from " + file.getPath(), e);
        }
    }

    protected Font getFont(TTFont font) {
        Font awtFont = loadedFonts.get(font);
        if (awtFont == null) {
            LOGGER.warning("Unknown font: " + font);
            int style = font.bold() ? Font.BOLD : Font.PLAIN;
            awtFont = new Font(Font.SANS_SERIF, style, font.size());
            loadedFonts.put(font, awtFont);
        }
        return awtFont;
    }

    @Override
    public String loadText(FilePointer file) {
        ResourceFile resourceFile = new ResourceFile(file.getPath());
        return resourceFile.read(Charsets.UTF_8);
    }

    @Override
    public PolygonModel loadModel(FilePointer file) {
        throw new UnsupportedGraphicsModeException();
    }

    @Override
    public boolean containsResourceFile(FilePointer file) {
        if (classPathResources.isEmpty()) {
            return new ResourceFile(file.getPath()).exists();
        } else {
            return classPathResources.contains(file.getPath());
        }
    }

    @Override
    public Configuration loadApplicationData(String appName, String fileName) {
        File file = Platform.getApplicationData(appName, fileName);

        if (!file.exists()) {
            return Configuration.fromProperties();
        }

        try {
            Properties properties = LoadUtils.loadProperties(file, Charsets.UTF_8);
            return Configuration.fromProperties(properties);
        } catch (IOException e) {
            throw new MediaException("Unable to load application data", e);
        }
    }

    @Override
    public void saveApplicationData(Configuration data, String appName, String fileName) {
        try {
            File file = Platform.getApplicationData(appName, fileName);
            Files.writeString(file.toPath(), data.serialize(), Charsets.UTF_8);
        } catch (IOException e) {
            throw new MediaException("Unable to save application data", e);
        }
    }
}
