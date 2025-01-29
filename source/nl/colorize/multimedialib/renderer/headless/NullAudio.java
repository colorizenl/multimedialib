//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.headless;

import nl.colorize.multimedialib.stage.Audio;

/**
 * A no-op audio implementation for (headless) renderers that do not support
 * audio playback. It can also be used for situations where a renderer does
 * support audio on some platforms, but not others.
 */
public class NullAudio implements Audio {

    @Override
    public void play(int volume, boolean loop) {
    }

    @Override
    public void stop() {
    }
}
