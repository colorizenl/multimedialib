//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl3.audio.OpenALSound;
import lombok.Getter;
import nl.colorize.multimedialib.stage.Audio;

/**
 * Uses libGDX's sound system for audio playback. Concurrent playback of the
 * same audio clip is not supported, though different audio clips can be played
 * concurrently.
 */
public class GDXAudio implements Audio {

    private Sound sound;
    private long playbackId;
    @Getter private int masterVolume;
    @Getter private float duration;

    public GDXAudio(Sound sound) {
        this.sound = sound;
        this.playbackId = -1;
        this.masterVolume = 100;
        this.duration = 0f;

        if (sound instanceof OpenALSound openAL) {
            duration = openAL.duration();
        }
    }

    @Override
    public void play(boolean loop) {
        stop();

        if (loop) {
            playbackId = sound.loop(masterVolume / 100f);
        } else {
            playbackId = sound.play(masterVolume / 100f);
        }
    }

    @Override
    public void stop() {
        if (playbackId != -1) {
            sound.stop();
            playbackId = -1;
        }
    }

    @Override
    public void changeVolume(int volume) {
        masterVolume = Math.clamp(volume, 0, 100);
        if (playbackId != -1) {
            sound.setVolume(playbackId, masterVolume / 100f);
        }
    }
}
