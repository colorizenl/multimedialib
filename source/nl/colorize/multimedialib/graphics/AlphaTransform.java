//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
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

    public float getAlpha();

    /**
     * Converts this {@code AlphaTransform} into a regular {@code Transform}.
     * All non-alpha values will be set to default values.
     */
    default Transform toTransform() {
        return Transform.withAlpha(getAlpha());
    }

    /**
     * Creates a new alpha transform that uses a fixed alpha value.
     */
    public static AlphaTransform create(float value) {
        Preconditions.checkArgument(value >= 0f && value <= 100f,
            "Invalid alpha value: " + value);

        return () -> value;
    }
}
