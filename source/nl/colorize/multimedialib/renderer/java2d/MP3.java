//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.util.LogHelper;
import nl.colorize.util.ResourceFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Plays MP3 files using the JLayer library. Audio clips are played in a
 * separate thread.
 */
public class MP3 extends Audio {

    private ResourceFile audioClip;

    private static final Logger LOGGER = LogHelper.getLogger(MP3.class);

    public MP3(ResourceFile audioClip) {
        this.audioClip = audioClip;
    }

    @Override
    public void play() {
        Thread audioThread = new Thread(this::playAudioClip, "MultimediaLib-Audio");
        audioThread.start();
    }

    private void playAudioClip() {
        try (InputStream audioStream = audioClip.openStream()) {
            Player player = new Player(audioStream);
            player.play();

            if (isLoop()) {
                playAudioClip();
            }
        } catch (IOException | JavaLayerException e) {
            LOGGER.log(Level.WARNING, "Error during playback of " + audioClip, e);
        }
    }

    @Override
    public void pause() {
        //TODO
    }

    @Override
    public void stop() {
        //TODO
    }
}
