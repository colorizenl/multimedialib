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

    /**
     * Changes this audio clip's volume to the specified value between 0
     * (silent) and 100 (normal volume). If the value of {@code volume} is
     * outside this range, it will be clamped.
     * <p>
     * If this method is called before the audio clip is played, the volume
     * change will take effect the next time it is played. If this audio clip
     * is currently playing, calling this method will dynamically change the
     * volume.
     */
    public void changeVolume(int volume);

    /**
     * Returns this audio clip's master volume, in the range between 0 (silent)
     * and 100 (normal volume). The master volume will be used the next time
     * this audio clip is played.
     */
    public int getMasterVolume();

    /**
     * Returns the duration of this audio clip, in seconds. If this audio
     * clip is still buffering or loading, this will return zero.
     */
    public float getDuration();
}
