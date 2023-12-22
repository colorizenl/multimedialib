//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.util.LogHelper;
import nl.colorize.util.ResourceFile;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.sound.sampled.FloatControl.Type.MASTER_GAIN;

/**
 * Plays audio clips using Java Sound. By default, Java Sound is unable to play
 * common audio formats such as MP3, so support for additional audio formats
 * therefore relies on external service providers.
 */
public class JavaSoundPlayer implements Audio, LineListener {

    private ResourceFile file;
    private Clip playing;

    private static final Logger LOGGER = LogHelper.getLogger(JavaSoundPlayer.class);

    public JavaSoundPlayer(ResourceFile file) {
        this.file = file;
    }

    @Override
    public void play(int volume, boolean loop) {
        Preconditions.checkArgument(volume >= 0 && volume <= 100, "Invalid volume: " + volume);

        if (playing != null) {
            return;
        }

        try (AudioInputStream mp3Stream = AudioSystem.getAudioInputStream(file.openStream())) {
            AudioFormat pcmFormat = convertAudioFormat(mp3Stream.getFormat());

            try (AudioInputStream pcmStream = AudioSystem.getAudioInputStream(pcmFormat, mp3Stream)) {
                playing = AudioSystem.getClip();
                playing.addLineListener(this);
                playing.open(pcmStream);

                FloatControl gainControl = (FloatControl) playing.getControl(MASTER_GAIN);
                gainControl.setValue(20f * (float) Math.log10(volume / 100f));

                if (loop) {
                    playing.loop(Clip.LOOP_CONTINUOUSLY);
                } else {
                    playing.start();
                }
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            LOGGER.log(Level.WARNING, "Exception during audio playback", e);
        }
    }

    private AudioFormat convertAudioFormat(AudioFormat original) {
        return new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            original.getSampleRate(), 16,
            original.getChannels(),
            original.getChannels() * 2,
            original.getSampleRate(),
            false
        );
    }

    @Override
    public void stop() {
        if (playing != null) {
            try {
                playing.close();
                playing = null;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error while closing audio clip", e);
            }
        }
    }

    @Override
    public void update(LineEvent event) {
        if (event.getType() == LineEvent.Type.STOP) {
            stop();
        }
    }
}
