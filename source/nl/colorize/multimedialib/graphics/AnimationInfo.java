//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

/**
 * Shared interface for the various types of animations that can be displayed.
 */
public interface AnimationInfo {

    /**
     * Returns the duration of this animation, in seconds.
     */
    public float getDuration();

    public boolean isLoop();
}
