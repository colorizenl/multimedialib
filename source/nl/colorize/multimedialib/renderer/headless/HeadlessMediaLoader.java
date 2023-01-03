//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.headless;

import com.google.common.annotations.VisibleForTesting;
import nl.colorize.multimedialib.stage.FontStyle;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.OutlineFont;
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

    @Override
    public OutlineFont loadFont(FilePointer file, FontStyle style) {
        if (graphicsEnvironmentEnabled) {
            return super.loadFont(file, style);
        } else {
            return new HeadlessFont(style);
        }
    }
}
