//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.graphics.Audio;

public class TeaAudio extends Audio {

    private String id;

    protected TeaAudio(String id) {
        this.id = id;
    }

    @Override
    public void play() {
        Browser.playAudio(id);
    }

    @Override
    public void stop() {
        Browser.stopAudio(id);
    }
}
