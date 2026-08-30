//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.headless;

import lombok.Getter;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.util.Subject;

/**
 * A no-op audio implementation of {@link Audio} for (headless) renderers that
 * do not support audio playback.
 */
@Getter
public class NullAudio implements Audio {

    private String name;
    private boolean playing;
    private double duration;
    private double volume;
    private double pitch;
    private Subject<Audio> audioQueue;

    public NullAudio(String name, Subject<Audio> audioQueue) {
        this.name = name;
        this.playing = false;
        this.duration = 0.0;
        this.volume = 100.0;
        this.pitch = 100.0;
        this.audioQueue = audioQueue;
    }

    @Override
    public void play(boolean loop) {
        playing = true;
        audioQueue.next(this);
    }

    @Override
    public void stop() {
        playing = false;
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
        return new NullAudio(name, audioQueue);
    }

    @Override
    public String toString() {
        return name;
    }
}
