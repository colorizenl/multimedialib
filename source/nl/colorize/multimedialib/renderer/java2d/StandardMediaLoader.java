//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.headless.NullAudio;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;
import nl.colorize.util.PropertyUtils;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.swing.Utils2D;

import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Uses APIs from the Java standard library to load media files: Java2D and ImageIO
 * for loading images, Java Sound for loading audio clips, and AWT for loading fonts.
 * These APIs are available on server and desktop platforms, but not on headless
 * server environments and not on Android.
 */
public class StandardMediaLoader implements MediaLoader {

    private static final String APPLICATION_DATA_FILE_NAME = "data.properties";
    private static final Logger LOGGER = LogHelper.getLogger(StandardMediaLoader.class);

    /**
     * Returns the location in the classpath for the specified resource file.
     * The default implementation just returns {@code file}, but subclasses
     * can override this method to look for resource files in alternative
     * locations.
     */
    protected ResourceFile locateFile(ResourceFile file) {
        return file;
    }

    @Override
    public Image loadImage(ResourceFile file) {
        try {
            ResourceFile source = locateFile(file);
            BufferedImage original = Utils2D.loadImage(source.openStream());

            if (Platform.isWindows()) {
                return prepareImage(file, original);
            } else {
                return new AWTImage(original);
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
            return new AWTImage(compatible);
        } catch (HeadlessException e) {
            LOGGER.warning("Failed to make image compatible with graphics environment");
            return new AWTImage(original);
        }
    }

    @Override
    public Audio loadAudio(ResourceFile file) {
        if (Platform.isWindows() || Platform.isMac()) {
            return new LWJGLAudio(file);
        } else {
            return new NullAudio();
        }
    }

    @Override
    public FontFace loadFont(ResourceFile file, String family, int size, ColorRGB color) {
        // AWT fonts are loaded just-in-time when they first
        // need to be rendered.
        return new FontFace(file, family, size, color);
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
}
