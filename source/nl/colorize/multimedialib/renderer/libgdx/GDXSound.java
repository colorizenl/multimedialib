//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.audio.Sound;
import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.renderer.Audio;

/**
 * Refers to an audio clip that is managed by libGDX.
 */
public class GDXSound extends Audio {

    private Sound sound;
    private boolean disposed;

    protected GDXSound(Sound sound) {
        this.sound = sound;
        this.disposed = false;
    }

    @Override
    public void play() {
        Preconditions.checkState(!disposed, "Sound has already been disposed");

        if (isLoop()) {
            sound.loop(getVolume() / 100f);
        } else {
            sound.play(getVolume() / 100f);
        }
    }

    @Override
    public void pause() {
        sound.pause();
    }

    @Override
    public void stop() {
        sound.stop();
    }

    public void dispose() {
        sound.dispose();
    }
}
