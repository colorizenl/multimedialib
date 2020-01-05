//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.base.Preconditions;

/**
 * Describes how to draw graphics with a modified alpha channel. The alpha
 * value is represented by a value between 0 and 100, where 0 indicates fully
 * transparent and 100 indicates the graphics' "normal" alpha.
 */
@FunctionalInterface
public interface AlphaTransform {

    public int getAlpha();

    public static AlphaTransform create(int value) {
        Preconditions.checkArgument(value >= 0 && value <= 100,
            "Invalid alpha value: " + value);

        return () -> value;
    }
}
