//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
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
     * Plays this audio clip once. If this method is called while the audio
     * clip is already playing, it will be replayed from the beginning.
     * Calling this method is equivelant to calling {@code play(false)}.
     */
    default void play() {
        play(false);
    }

    /**
     * Plays this audio clip. If {@code loop} is true, this will keep playing
     * the audio clip in a loop until it is stopped. If {@code loop} is false,
     * it will only play it once. If this method is called while the audio
     * clip is already playing, it will be replayed from the beginning.
     */
    public void play(boolean loop);

    /**
     * Stops playback of this audio clip and resets the playhead to the start
     * of the audio clip. Does nothing if this audio clip is not currently
     * playing.
     */
    public void stop();

    public boolean isPlaying();

    /**
     * Returns the duration of this audio clip, in seconds.
     */
    public double getDuration();

    /**
     * Changes this audio clip's volume to the specified value between 0%
     * (silent) and 100% (normal volume). Values outside of this range will
     * automatically be clamped. This method can be used both before playback
     * and during playback.
     */
    public void changeVolume(double volume);

    /**
     * Changes this audio clip's pitch to a percentage of the original, within
     * the range between 50% and 200%. Values outside of this range will
     * automatically be clamped. This method can be used both before playback
     * and during playback.
     */
    public void changePitch(double pitch);

    /**
     * Returns a new {@link Audio} instance that is based on the same audio
     * clip as this instance. This allows multiple instances to be played
     * simultaneously.
     */
    public Audio copy();
}
