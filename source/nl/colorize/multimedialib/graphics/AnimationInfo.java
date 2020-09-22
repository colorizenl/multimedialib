//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.base.Preconditions;

/**
 * Describes the properties for an animation that was loaded from a mesh.
 */
public class AnimationInfo {

    private String name;
    private float duration;

    public AnimationInfo(String name, float duration) {
        Preconditions.checkArgument(duration > 0f, "Invalid duration: " + duration);

        this.name = name;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public float getDuration() {
        return duration;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AnimationInfo) {
            AnimationInfo other = (AnimationInfo) o;
            return name.equals(other.name);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
