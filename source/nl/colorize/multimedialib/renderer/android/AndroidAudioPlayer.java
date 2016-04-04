//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.android;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

import nl.colorize.multimedialib.graphics.AudioData;
import nl.colorize.multimedialib.renderer.AudioQueue;
import nl.colorize.multimedialib.renderer.RendererException;
import nl.colorize.util.LogHelper;

/**
 * Plays audio clips using the Android standard libraries. Note that not all
 * Android devices have the same level of support for different audio formats.
 * MP3 is supported by nearly all devices, OGG is supported by most devices. 
 */
public class AndroidAudioPlayer implements AudioQueue, OnCompletionListener {
	
	private Queue<AudioData> playlist;
	
	private static final Logger LOGGER = LogHelper.getLogger(AndroidAudioPlayer.class);
	
	public AndroidAudioPlayer() {
		playlist = new ConcurrentLinkedQueue<AudioData>();
	}

	public void play(AudioData audioClip) {
		if (playlist.isEmpty()) {
			forcePlay(audioClip);
		} else {
			playlist.add(audioClip);
		}
	}
	
	public void forcePlay(AudioData audioClip) {
		try {
			FileInputStream audioStream = (FileInputStream) audioClip.openStream();
			FileDescriptor audioFileDescriptor = audioStream.getFD();
			
			MediaPlayer player = new MediaPlayer();
			player.setDataSource(audioFileDescriptor);
			player.setVolume(1f, 1f);
			player.setOnCompletionListener(this);
			player.prepare();
			player.start();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "I/O exception while playing audio clip", e);
		} catch (RendererException e) {
			LOGGER.log(Level.SEVERE, "Audio clip not supported: " + audioClip, e);
		} catch (ClassCastException e) {
			LOGGER.log(Level.SEVERE, "Audio clip must be loaded from Android assets: " + audioClip, e);
		}
	}
	
	public void onCompletion(MediaPlayer player) {
		AudioData upNext = playlist.poll();
		if (upNext != null) {
			forcePlay(upNext);
		}
	}
	
	public void stop(AudioData audioClip) {
		//TODO
	}

	public void stopAll() {
		playlist.clear();
	}
}
