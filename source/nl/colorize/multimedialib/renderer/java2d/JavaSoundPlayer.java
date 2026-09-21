//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import lombok.Getter;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.util.LogHelper;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.Subject;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.sound.sampled.FloatControl.Type.MASTER_GAIN;

/**
 * Plays audio clips using Java Sound. By default, Java Sound is unable to
 * play common audio formats such as MP3 or OGG, so support for additional
 * audio formats therefore relies on external service providers.
 * <p>
 * Java Sound does not support changing the pitch at runtime. To keep
 * compatibility with other renderers, {@link #changePitch(double)} will
 * not throw an exception, but it will not actually do anything either.
 */
public class JavaSoundPlayer implements Audio, LineListener {

    private ResourceFile origin;
    private byte[] audioData;
    private Clip playing;

    @Getter private double duration;
    @Getter private double volume;
    @Getter private double pitch;
    @Getter private Subject<Audio> audioQueue;

    private static final Logger LOGGER = LogHelper.getLogger(JavaSoundPlayer.class);

    public JavaSoundPlayer(ResourceFile origin, byte[] audioData, Subject<Audio> audioQueue) {
        this.origin = origin;
        this.audioData = audioData;

        this.duration = 0;
        this.volume = 100;
        this.pitch = 100;
        this.audioQueue = audioQueue;
    }

    @Override
    public void play(boolean loop) {
        if (playing != null) {
            return;
        }

        try (AudioInputStream rawStream = openStream()) {
            AudioFormat pcmFormat = convertAudioFormat(rawStream.getFormat());

            try (AudioInputStream pcmStream = AudioSystem.getAudioInputStream(pcmFormat, rawStream)) {
                playing = AudioSystem.getClip();
                playing.addLineListener(this);
                playing.open(pcmStream);
                changeVolume(volume);

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

    private AudioInputStream openStream() throws UnsupportedAudioFileException, IOException {
        return AudioSystem.getAudioInputStream(new ByteArrayInputStream(audioData));
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
        if (event.getType().equals(LineEvent.Type.STOP)) {
            stop();
            playing = null;
        }
    }

    @Override
    public boolean isPlaying() {
        return playing != null;
    }

    @Override
    public void changeVolume(double volume) {
        this.volume = Math.clamp(volume, 0.0, 100.0);

        if (playing != null) {
            FloatControl gainControl = (FloatControl) playing.getControl(MASTER_GAIN);
            gainControl.setValue(20f * (float) Math.log10(this.volume / 100f));
        }
    }

    @Override
    public void changePitch(double pitch) {
    }

    @Override
    public JavaSoundPlayer copy() {
        return new JavaSoundPlayer(origin, audioData, audioQueue);
    }
}
