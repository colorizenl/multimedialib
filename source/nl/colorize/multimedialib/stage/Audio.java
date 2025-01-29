//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

/**
 * Describes an audio clip that has been loaded by the renderer. There is no
 * difference between the audio data and audio playback, an instance of this
 * class represents both. This is done because some renderers do not allow
 * audio data to be reused.
 */
public interface Audio {

    /**
     * Starts playing this audio clip. {@code volume} is in the range 0-100,
     * where 100 indicates the audio clip's normal volume and 0 indicates the
     * audio clip is muted. Calling this method does nothing if playback of
     * this audio clip is already in progress.
     */
    public void play(int volume, boolean loop);

    /**
     * Plays this audio clip once, at its normal volume. This method is a
     * shorthand version of {@code play(100, false)}.
     */
    default void play() {
        play(100, false);
    }

    /**
     * Stops playback of this audio clip and resets the playhead to the start
     * of the audio clip. Calling this method does nothing if this audio clip
     * is not currently playing.
     */
    public void stop();
}
