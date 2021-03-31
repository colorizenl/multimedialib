//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.headless;

import com.google.common.annotations.VisibleForTesting;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.java2d.StandardMediaLoader;

/**
 * Media loader implementation that can be used in headless environments,
 * primarily for testing and simulation purposes. By default, it will load all
 * media files using {@link StandardMediaLoader}. However, image loading can
 * be disabled for situations in which no graphics environment is available.
 */
@VisibleForTesting
public class HeadlessMediaLoader extends StandardMediaLoader {

    private boolean graphicsEnvironmentEnabled;

    public static final int HEADLESS_IMAGE_SIZE = 128;

    public HeadlessMediaLoader(boolean graphicsEnvironmentEnabled) {
        this.graphicsEnvironmentEnabled = graphicsEnvironmentEnabled;
    }

    @Override
    public Image loadImage(FilePointer file) {
        if (graphicsEnvironmentEnabled) {
            return super.loadImage(file);
        } else {
            return new HeadlessImage();
        }
    }

    /**
     * Placeholder that is used when image loading has been disabled.
     */
    private static class HeadlessImage implements Image {

        @Override
        public Rect getRegion() {
            return new Rect(0, 0, HEADLESS_IMAGE_SIZE, HEADLESS_IMAGE_SIZE);
        }

        @Override
        public Image extractRegion(Rect region) {
            return this;
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
        public Image tint(ColorRGB color) {
            return this;
        }
    }
}
