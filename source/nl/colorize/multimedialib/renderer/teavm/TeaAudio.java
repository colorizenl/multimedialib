//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import lombok.Getter;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.util.Subject;
import org.teavm.jso.dom.html.HTMLAudioElement;

/**
 * Plays audio clips using the HTML5 media API that is supported by all modern
 * browsers. Since the {@code <audio>} element is loaded asynchronously,
 * playing the audio clip is only possible after the browser has loaded the
 * audio clip.<p>
 * Supported audio formats depend on the browser, although MP3 and OGG are
 * now supported by all modern browsers.
 */
public class TeaAudio implements Audio {

    private HTMLAudioElement audioElement;
    @Getter private int masterVolume;

    protected TeaAudio(Subject<HTMLAudioElement> audioPromise) {
        this.masterVolume = 100;
        audioPromise.subscribe(event -> audioElement = event);
    }

    @Override
    public void play(boolean loop) {
        stop();
        if (audioElement != null) {
            audioElement.setVolume(masterVolume / 100f);
            audioElement.play();
        }
    }

    @Override
    public void stop() {
        if (audioElement != null) {
            audioElement.pause();
            audioElement.setCurrentTime(0.0);
        }
    }

    @Override
    public void changeVolume(int volume) {
        masterVolume = Math.clamp(volume, 0, 100);
        if (audioElement != null) {
            audioElement.setVolume(masterVolume / 100f);
        }
    }

    @Override
    public float getDuration() {
        if (audioElement == null) {
            return 0f;
        }
        return (float) audioElement.getDuration();
    }

    @Override
    public String toString() {
        if (audioElement == null) {
            return "<loading>";
        }
        return audioElement.getSrc();
    }
}
