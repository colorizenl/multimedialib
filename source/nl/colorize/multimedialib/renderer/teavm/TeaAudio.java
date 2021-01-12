//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.renderer.Audio;

/**
 * Plays audio clips using the HTML5 media API that is supported by all modern
 * browsers.
 * <p>
 * Supported audio formats depend on the browser. All browsers support OGG,
 * all browsers except Firefox support MP3.
 */
public class TeaAudio extends Audio {

    private String id;

    protected TeaAudio(String id) {
        this.id = id;
    }

    @Override
    public void play() {
        Browser.playAudio(id, getVolume() / 100f, isLoop());
    }

    @Override
    public void pause() {
        Browser.stopAudio(id, false);
    }

    @Override
    public void stop() {
        Browser.stopAudio(id, true);
    }
}
