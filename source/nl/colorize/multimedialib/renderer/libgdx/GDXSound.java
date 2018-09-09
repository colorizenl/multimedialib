//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.audio.Sound;
import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.Audio;
import nl.colorize.util.ResourceFile;

/**
 * Refers to an audio clip that is managed by libGDX.
 */
public class GDXSound extends Audio {

    private Sound sound;
    private boolean disposed;

    protected GDXSound(Sound sound, ResourceFile source) {
        super(source);
        this.sound = sound;
        this.disposed = false;
    }

    @Override
    public void play() {
        Preconditions.checkState(!disposed, "Sound has already been disposed");
        sound.play();
    }

    @Override
    public void stop() {
        sound.stop();
    }

    public void dispose() {
        sound.dispose();
    }
}
