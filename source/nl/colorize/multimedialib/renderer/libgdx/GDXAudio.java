//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.audio.Sound;
import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.stage.Audio;

/**
 * Uses libGDX's sound system for audio playback. Concurrent playback of the
 * same audio clip is not supported, though different audio clips can be played
 * concurrently.
 */
public class GDXAudio implements Audio {

    private Sound sound;

    public GDXAudio(Sound sound) {
        this.sound = sound;
    }

    @Override
    public void play(int volume, boolean loop) {
        Preconditions.checkArgument(volume >= 0 && volume <= 100, "Invalid volume: " + volume);

        sound.stop();
        if (loop) {
            sound.loop(volume / 100f);
        } else {
            sound.play(volume / 100f);
        }
    }

    @Override
    public void stop() {
        sound.stop();
    }
}
