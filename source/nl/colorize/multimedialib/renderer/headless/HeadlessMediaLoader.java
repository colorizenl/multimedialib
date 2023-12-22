//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.headless;

import com.google.common.annotations.VisibleForTesting;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.renderer.java2d.AWTImage;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.FontStyle;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.OutlineFont;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.java2d.StandardMediaLoader;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.swing.Utils2D;

import java.awt.image.BufferedImage;
import java.util.UUID;

/**
 * Media loader implementation that can be used in headless environments,
 * primarily for testing and simulation purposes. By default, it will load all
 * media files using {@link StandardMediaLoader}. However, image loading can
 * be disabled for situations in which no graphics environment is available.
 */
@VisibleForTesting
public class HeadlessMediaLoader extends StandardMediaLoader {

    private boolean graphicsEnvironmentEnabled;

    public HeadlessMediaLoader(boolean graphicsEnvironmentEnabled) {
        this.graphicsEnvironmentEnabled = graphicsEnvironmentEnabled;
    }

    @Override
    public Image loadImage(FilePointer file) {
        if (graphicsEnvironmentEnabled) {
            // This avoids using Utils2D.makeImageCompatible, since
            // that relies on the graphics environment and does not
            // work headless.
            BufferedImage image = Utils2D.loadImage(new ResourceFile(file.path()));
            return new AWTImage(image, file);
        } else if (file == null) {
            return new HeadlessImage();
        } else {
            return new HeadlessImage(file.path());
        }
    }

    @Override
    public Audio loadAudio(FilePointer file) {
        return new HeadlessAudio();
    }

    @Override
    public OutlineFont loadFont(FilePointer file, String family, FontStyle style) {
        return super.loadFont(file, family, style);
    }

    /**
     * A "fake" image that is only used when the graphics environment is
     * completely disabled in the {@link HeadlessMediaLoader}.
     */
    private static class HeadlessImage implements Image {

        private String name;
        private int width;
        private int height;

        public HeadlessImage(String name, int width, int height) {
            this.name = name;
            this.width = width;
            this.height = height;
        }

        public HeadlessImage(String name) {
            this(name, 100, 100);
        }

        public HeadlessImage() {
            this("HeadlessImage-" + UUID.randomUUID(), 100, 100);
        }

        @Override
        public Region getRegion() {
            return new Region(0, 0, width, height);
        }

        @Override
        public Image extractRegion(Region subRegion) {
            String subImageName = name + "[" + subRegion + "]";
            return new HeadlessImage(subImageName, subRegion.width(), subRegion.height());
        }

        @Override
        public ColorRGB getColor(int x, int y) {
            return ColorRGB.BLACK;
        }

        @Override
        public int getAlpha(int x, int y) {
            return 100;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
