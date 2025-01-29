//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.jfx;

import com.google.common.base.Preconditions;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import nl.colorize.multimedialib.stage.Audio;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses the JavaFX media player to play audio. JavaFX is completely independent
 * of Java Sound, and therefore <em>does</em> support MP3 files out-of-the-box.
 */
public class JFXAudioPlayer implements Audio {

    private Media media;
    private List<MediaPlayer> currentlyPlaying;

    protected JFXAudioPlayer(Media media) {
        this.media = media;
        this.currentlyPlaying = new ArrayList<>();
    }

    @Override
    public void play(int volume, boolean loop) {
        Preconditions.checkArgument(volume >= 0 && volume <= 100, "Invalid volume: " + volume);

        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setVolume(volume / 100.0);
        mediaPlayer.setCycleCount(loop ? Integer.MAX_VALUE : 1);
        mediaPlayer.play();

        currentlyPlaying.add(mediaPlayer);
    }

    @Override
    public void stop() {
        for (MediaPlayer mediaPlayer : currentlyPlaying) {
            mediaPlayer.stop();
        }
        currentlyPlaying.clear();
    }
}
