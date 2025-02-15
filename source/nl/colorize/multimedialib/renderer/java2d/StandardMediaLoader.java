//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.google.common.base.Preconditions;
import lombok.Getter;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.headless.NullAudio;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.LoadStatus;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;
import nl.colorize.util.PropertyUtils;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.SubscribableCollection;
import nl.colorize.util.stats.Cache;
import nl.colorize.util.swing.Utils2D;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Uses APIs from the Java standard library to load media files: Java2D and ImageIO
 * for loading images, Java Sound for loading audio clips, and AWT for loading fonts.
 * These APIs are available on server and desktop platforms, but not on headless
 * server environments and not on Android.
 */
@Getter
public class StandardMediaLoader implements MediaLoader {

    private SubscribableCollection<LoadStatus> loadStatus;

    // The font cache is global because loaded AWT fonts need to
    // be registered with the graphics environment.
    public static Map<String, Font> fontFamilies = new HashMap<>();
    public static Cache<FontFace, Font> fontCache = Cache.from(key -> prepareFont(key));

    private static final String APPLICATION_DATA_FILE_NAME = "data.properties";
    private static final Logger LOGGER = LogHelper.getLogger(StandardMediaLoader.class);

    public StandardMediaLoader() {
        this.loadStatus = SubscribableCollection.wrap(new CopyOnWriteArrayList<>());
    }

    /**
     * Returns a {@link ResourceFile} for the resource file at the specified
     * location. The default implementation loads files from the classpath,
     * subclasses can override this method to load files from alternative
     * locations.
     */
    protected ResourceFile locateFile(ResourceFile location) {
        return new ResourceFile(location.path());
    }

    @Override
    public Image loadImage(ResourceFile file) {
        try {
            ResourceFile source = locateFile(file);
            BufferedImage original = Utils2D.loadImage(source.openStream());

            if (Platform.isWindows()) {
                return prepareImage(file, original);
            } else {
                return new AWTImage(original, file);
            }
        } catch (IOException e) {
            throw new MediaException("Cannot load image from " + file.path(), e);
        }
    }

    /**
     * Prepares the image in order to make it hardware-accelerated. If this
     * is not possible on the current platform, the original image will be
     * used.
     */
    private Image prepareImage(ResourceFile file, BufferedImage original) {
        try {
            BufferedImage compatible = Utils2D.makeImageCompatible(original);
            return new AWTImage(compatible, file);
        } catch (HeadlessException e) {
            return new AWTImage(original, file);
        }
    }

    @Override
    public Audio loadAudio(ResourceFile file) {
        if (Platform.isMac()) {
            return new JavaSoundPlayer(locateFile(file));
        } else {
            LOGGER.warning("Java Sound not supported on platform " + Platform.getPlatformName());
            return new NullAudio();
        }
    }

    @Override
    public FontFace loadFont(ResourceFile file, String family, int size, ColorRGB color) {
        ResourceFile source = locateFile(file);

        try (InputStream stream = source.openStream()) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, stream);

            if (!fontFamilies.containsKey(family)) {
                fontFamilies.put(family, font);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            }

            return new FontFace(file, family, size, color);
        } catch (IOException | FontFormatException e) {
            throw new MediaException("Cannot load font from " + file.path(), e);
        }
    }

    @Override
    public String loadText(ResourceFile file) {
        return locateFile(file).read(UTF_8);
    }

    @Override
    public Mesh loadModel(ResourceFile file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsResourceFile(ResourceFile file) {
        return locateFile(file).exists();
    }

    protected File getApplicationDataFile(String appName) {
        Preconditions.checkArgument(appName.length() >= 2, "Invalid application name");
        return Platform.getApplicationData(appName, APPLICATION_DATA_FILE_NAME);
    }

    @Override
    public Properties loadApplicationData(String appName) {
        File dataFile = getApplicationDataFile(appName);

        if (dataFile.exists()) {
            try {
                return PropertyUtils.loadProperties(dataFile, UTF_8);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Unable to load application data", e);
            }
        }

        // Default to returning an empty Properties instance
        // if the file cannot be loaded for whatever reason.
        return new Properties();
    }

    @Override
    public void saveApplicationData(String appName, Properties data) {
        try {
            File dataFile = getApplicationDataFile(appName);
            PropertyUtils.saveProperties(data, dataFile, UTF_8);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to save application data", e);
        }
    }

    private static Font prepareFont(FontFace key) {
        Font baseFont = fontFamilies.get(key.family());
        if (baseFont == null) {
            LOGGER.warning("Unknown font family: " + key.family());
            baseFont = new Font("sans-serif", Font.PLAIN, 10);
        }
        return baseFont.deriveFont(Font.PLAIN, key.size());
    }
}
