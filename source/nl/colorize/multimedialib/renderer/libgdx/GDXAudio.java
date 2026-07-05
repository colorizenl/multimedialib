//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl3.audio.OpenALSound;
import nl.colorize.multimedialib.stage.Audio;

/**
 * Uses libGDX's sound system for audio playback. Concurrent playback of the
 * same audio clip is not supported, though different audio clips can be played
 * concurrently.
 */
public class GDXAudio implements Audio {

    private Sound sound;
    private long playbackId;
    private double volume;
    private double pitch;

    public GDXAudio(Sound sound) {
        this.sound = sound;
        this.playbackId = -1;
        this.volume = 100;
        this.pitch = 100;
    }

    @Override
    public void play(boolean loop) {
        stop();

        if (loop) {
            playbackId = sound.loop((float) volume / 100f, (float) pitch / 100f, 0f);
        } else {
            playbackId = sound.play((float) volume / 100f, (float) pitch / 100f, 0f);
        }
    }

    @Override
    public void stop() {
        if (playbackId != -1) {
            sound.stop(playbackId);
            playbackId = -1;
        }
    }

    @Override
    public boolean isPlaying() {
        return playbackId != -1;
    }

    public double getDuration() {
        if (sound instanceof OpenALSound openAL) {
            return openAL.duration();
        } else {
            return 0.0;
        }
    }

    @Override
    public void changeVolume(double volume) {
        this.volume = Math.clamp(volume, 0, 100);
        if (playbackId != -1) {
            sound.setVolume(playbackId, (float) this.volume / 100f);
        }
    }

    @Override
    public void changePitch(double pitch) {
        this.pitch = Math.clamp(pitch, 50, 200);
        if (playbackId != -1) {
            sound.setPitch(playbackId, (float) this.pitch / 100f);
        }
    }

    @Override
    public Audio copy() {
        return new GDXAudio(sound);
    }
}
