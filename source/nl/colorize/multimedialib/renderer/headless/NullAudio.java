//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.headless;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.stage.Audio;

/**
 * A no-op audio implementation of {@link Audio} for (headless) renderers that
 * do not support audio playback.
 */
@Getter
@Setter
public class NullAudio implements Audio {

    private int masterVolume;
    private float duration;

    public NullAudio() {
        this.masterVolume = 100;
        this.duration = 0f;
    }

    @Override
    public void play(boolean loop) {
    }

    @Override
    public void stop() {
    }

    @Override
    public void changeVolume(int volume) {
        masterVolume = Math.clamp(volume, 0, 100);
    }
}
