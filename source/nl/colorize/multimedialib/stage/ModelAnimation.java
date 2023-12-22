//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;

/**
 * Properties for one of the animations defined in a {@link PolygonModel}. This
 * class should not be confused with {@link Animation}, which is used for 2D
 * animations in sprites, while this class is used for animating 3D models.
 * <p>
 * Like all time units in MultimediaLib, the animation's duration is defined
 * in seconds.
 */
public record ModelAnimation(String name, float duration, boolean loop) {

    public ModelAnimation {
        Preconditions.checkArgument(!name.isEmpty(), "Missing name");
        Preconditions.checkArgument(duration > 0f, "Invalid duration: " + duration);
    }
}
