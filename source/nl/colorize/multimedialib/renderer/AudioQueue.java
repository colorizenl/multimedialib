//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.graphics.AudioData;

/**
 * Plays audio. If playback is requested when another audio clip is already
 * playing the audio clip will be added to a queue, and will be played as soon
 * as the other clip has finished playing. Audio clips can also be played
 * simultaneously, but the number of clips that can play at the same time is
 * platform-dependent.
 * <p>
 * Playing audio clips is done using a different thread than the one running the
 * animation loop. All methods in this class are non-blocking and will return 
 * immediately.
 */
public interface AudioQueue {

	/**
	 * Plays an audio clip. If another audio clip is already playing the new
	 * audio clip will be added to the queue.
	 */
	public void play(AudioData audioClip);
	
	/**
	 * Plays an audio clip. If another clip is already playing it will be stopped
	 * and the new audio clip will be played instead.
	 */
	public void forcePlay(AudioData audioClip);
	
	/**
	 * Stops playback of the specified audio clip, if it is currently playing.
	 */
	public void stop(AudioData audioClip);
	
	/**
	 * Stops playback of all audio clips that are playing.
	 */
	public void stopAll();
}
