//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.reflect.ClassPath;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.GeometryBuilder;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.UnsupportedGraphicsModeException;
import nl.colorize.multimedialib.renderer.headless.HeadlessAudio;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.multimedialib.stage.FontStyle;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.OutlineFont;
import nl.colorize.multimedialib.stage.PolygonModel;
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
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
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

    private Function<FilePointer, ResourceFile> locator;
    private Set<String> classPathResources;

    private static final String APPLICATION_DATA_FILE_NAME = "data.properties";
    private static final Logger LOGGER = LogHelper.getLogger(StandardMediaLoader.class);

    /**
     * Creates a {@link StandardMediaLoader} that will look for resource files
     * in the classpath, with a fallback to the local file system.
     */
    public StandardMediaLoader() {
        this.locator = file -> new ResourceFile(file.path());
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

    /**
     * Creates a {@link StandardMediaLoader} that will only look for resource
     * files in the specified directory. This can be used for testing, where
     * files are located in a temporary directory and not on the classpath.
     */
    @VisibleForTesting
    public StandardMediaLoader(File resourceDir) {
        Preconditions.checkArgument(resourceDir.exists() && resourceDir.isDirectory(),
            "Invalid resource directory: " + resourceDir.getAbsolutePath());

        this.locator = file -> {
            File localFile = new File(resourceDir.getAbsolutePath() + "/" + file.path());
            return new ResourceFile(localFile);
        };
        this.classPathResources = Collections.emptySet();
    }

    @Override
    public Image loadImage(FilePointer file) {
        try {
            ResourceFile source = toResourceFile(file);
            BufferedImage original = Utils2D.loadImage(source.openStream());

            if (Platform.isWindows() || Platform.isMac()) {
                return new AWTImage(Utils2D.makeImageCompatible(original), file);
            } else {
                return new AWTImage(original, file);
            }
        } catch (IOException e) {
            throw new MediaException("Cannot load image from " + file.path(), e);
        }
    }

    @Override
    public Audio loadAudio(FilePointer file) {
        if (Platform.isMac()) {
            return new JavaSoundPlayer(toResourceFile(file));
        } else {
            LOGGER.warning("Java Sound not supported on platform " + Platform.getPlatformName());
            return new HeadlessAudio();
        }
    }

    @Override
    public OutlineFont loadFont(FilePointer file, FontStyle style) {
        ResourceFile source = toResourceFile(file);

        try (InputStream stream = source.openStream()) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, stream);

            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            env.registerFont(font);

            // Immediately derive a version of the font with the correct
            // style, since we have no idea what size the loaded font has.
            return new AWTFont(font, style).derive(style);
        } catch (IOException | FontFormatException e) {
            throw new MediaException("Cannot load font from " + file.path(), e);
        }
    }

    @Override
    public String loadText(FilePointer file) {
        return toResourceFile(file).read(Charsets.UTF_8);
    }

    @Override
    public PolygonModel loadModel(FilePointer file) {
        throw new UnsupportedGraphicsModeException();
    }

    @Override
    public GeometryBuilder getGeometryBuilder() {
        throw new UnsupportedGraphicsModeException();
    }

    @Override
    public boolean containsResourceFile(FilePointer file) {
        if (classPathResources.isEmpty()) {
            return toResourceFile(file).exists();
        } else {
            return classPathResources.contains(file.path());
        }
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
                return LoadUtils.loadProperties(dataFile, Charsets.UTF_8);
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
            LoadUtils.saveProperties(data, dataFile, Charsets.UTF_8);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to save application data", e);
        }
    }

    private ResourceFile toResourceFile(FilePointer file) {
        return locator.apply(file);
    }
}
