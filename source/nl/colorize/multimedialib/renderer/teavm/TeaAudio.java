//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.stage.Audio;
import org.teavm.jso.dom.html.HTMLAudioElement;

/**
 * Plays audio clips using the HTML5 media API that is supported by all modern
 * browsers.
 * <p>
 * Supported audio formats depend on the browser. All browsers support OGG,
 * all browsers except Firefox support MP3.
 */
public class TeaAudio extends Audio {

    private HTMLAudioElement audioElement;

    protected TeaAudio(HTMLAudioElement audioElement) {
        this.audioElement = audioElement;
    }

    @Override
    public void play() {
        audioElement.setVolume(getVolume() / 100f);
        audioElement.setLoop(isLoop());
        audioElement.play();
    }

    @Override
    public void pause() {
        audioElement.pause();
    }

    @Override
    public void stop() {
        audioElement.pause();
        audioElement.setCurrentTime(0.0);
    }
}
