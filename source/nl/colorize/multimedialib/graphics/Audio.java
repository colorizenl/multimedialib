//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.renderer.MediaLoader;

/**
 * Represents an audio clip. Audio can be loaded from one of the common file
 * formats (for example MP3 or OGG). The volume of audio clips can be set to
 * a value between 0 and 100, where 100 indicates the audio clip's original
 * volume. Audio clips stored in files are loaded using a {@link MediaLoader}.
 */
public abstract class Audio {

    private int volume;
    private boolean loop;

    public Audio() {
        this.volume = 100;
        this.loop = false;
    }

    public abstract void play();

    public abstract void stop();

    public void setVolume(int volume) {
        Preconditions.checkArgument(volume >= 0 && volume <= 100,
            "Volume out of range 0 - 100: " + volume);
        this.volume = volume;
    }

    public int getVolume() {
        return volume;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public boolean isLoop() {
        return loop;
    }
}
