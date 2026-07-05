//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.headless;

import lombok.Getter;
import nl.colorize.multimedialib.stage.Audio;

/**
 * A no-op audio implementation of {@link Audio} for (headless) renderers that
 * do not support audio playback.
 */
@Getter
public class NullAudio implements Audio {

    private double duration;
    private double volume;
    private double pitch;

    public NullAudio() {
        this.duration = 0.0;
        this.volume = 100.0;
        this.pitch = 100.0;
    }

    @Override
    public void play(boolean loop) {
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public void changeVolume(double volume) {
        this.volume = Math.clamp(volume, 0, 100);
    }

    @Override
    public void changePitch(double pitch) {
        this.pitch = Math.clamp(pitch, 50, 200);
    }

    @Override
    public Audio copy() {
        return new NullAudio();
    }
}
