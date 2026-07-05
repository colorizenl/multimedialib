//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.Preconditions;
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

    protected TeaAudio(Subject<HTMLAudioElement> audioPromise) {
        audioPromise.subscribe(event -> audioElement = event);
    }

    @Override
    public void play(boolean loop) {
        stop();
        if (audioElement != null) {
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
    public boolean isPlaying() {
        if (audioElement == null) {
            return false;
        }

        return audioElement.getCurrentTime() > 0.0 &&
            !audioElement.isPaused() &&
            !audioElement.isEnded();
    }

    @Override
    public double getDuration() {
        if (audioElement == null) {
            return 0f;
        }
        return audioElement.getDuration();
    }

    @Override
    public void changeVolume(double volume) {
        if (audioElement != null) {
            float audioVolume = Math.clamp((float) volume / 100f, 0f, 1f);
            audioElement.setVolume(audioVolume);
        }
    }

    @Override
    public void changePitch(double pitch) {
        if (audioElement != null) {
            double audioPitch = Math.clamp(pitch / 100.0, 0.5, 2.0);
            audioElement.setPlaybackRate(audioPitch);
        }
    }

    @Override
    public Audio copy() {
        Preconditions.checkState(audioElement != null,
            "Audio element has not been preloaded and is not yet available");

        HTMLAudioElement copyElement = (HTMLAudioElement) audioElement.cloneNode(true);
        audioElement.getParentNode().appendChild(copyElement);
        return new TeaAudio(Subject.of(copyElement));
    }

    @Override
    public String toString() {
        if (audioElement == null) {
            return "<loading>";
        }
        return audioElement.getSrc();
    }
}
