//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.util.Promise;
import org.teavm.jso.dom.html.HTMLAudioElement;

/**
 * Plays audio clips using the HTML5 media API that is supported by all modern
 * browsers. Since the {@code <audio>} element is loaded asynchronously,
 * playing the audio clip is only possible after the browser has loaded the
 * audio clip.
 * <p>
 * Supported audio formats depend on the browser. All browsers support OGG.
 * Older Firefox versions used to not support MP3, but this is now changes and
 * all browsers now also support MP3.
 */
public class TeaAudio implements Audio {

    private Promise<HTMLAudioElement> audioPromise;

    protected TeaAudio(Promise<HTMLAudioElement> audioPromise) {
        this.audioPromise = audioPromise;
    }

    @Override
    public void play(int volume, boolean loop) {
        Preconditions.checkArgument(volume >= 0 && volume <= 100, "Invalid volume: " + volume);

        audioPromise.getValue().ifPresent(audioElement -> {
            audioElement.setVolume(volume / 100f);
            audioElement.setLoop(loop);
            audioElement.play();
        });
    }

    @Override
    public void stop() {
        audioPromise.getValue().ifPresent(audioElement -> {
            audioElement.pause();
            audioElement.setCurrentTime(0.0);
        });
    }

    @Override
    public String toString() {
        return audioPromise.getValue()
            .map(HTMLAudioElement::getSrc)
            .orElse("<loading>");
    }
}
