//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import nl.colorize.multimedialib.graphics.AudioData;
import nl.colorize.multimedialib.renderer.AudioQueue;
import nl.colorize.util.LogHelper;

/**
 * Uses Java Sound and the JLayer library to play MP3 audio clips. 
 */
public class MP3Player implements AudioQueue {

	private AtomicBoolean running;
	private Queue<AudioData> playlist;
	
	private static final long POLL_TIME = 50;
	private static final long PAUSE = 100;
	private static final Logger LOGGER = LogHelper.getLogger(MP3Player.class);
	
	public MP3Player() {
		running = new AtomicBoolean(false);
		playlist = new ConcurrentLinkedQueue<AudioData>();
	}

	private void startAudioThread() {
		Runnable audioTask = new Runnable() {
			public void run() {
				while (running.get()) {
					AudioData toPlay = playlist.poll();
					if (toPlay != null) {
						playAudioClip(toPlay);
					}
					
					try {
						Thread.sleep(PAUSE);
					} catch (InterruptedException e) {
						LOGGER.warning("Audio thread interrupted");
					}
				}
			}
		};
		
		Thread audioThread = new Thread(audioTask, "MultimediaLib-AudioThread");
		audioThread.setDaemon(true);
		audioThread.start();
	}
	
	private void playAudioClip(AudioData toPlay) {
		try {
			Player player = new Player(toPlay.openStream());
			player.play();
			
			while (!player.isComplete()) {
				try {
					Thread.sleep(POLL_TIME);
				} catch (InterruptedException e) {
					LOGGER.warning("Audio thread interrupted");
				}
			}
				
			player.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "I/O exception while playing audio clip", e);
		} catch (JavaLayerException e) {
			LOGGER.log(Level.WARNING, "Exception while playing audio clip", e);
		}
	}
	
	public void play(AudioData audioClip) {
		if (!running.get()) {
			running.set(true);
			startAudioThread();
		}
		
		playlist.add(audioClip);
	}
	
	public void forcePlay(AudioData audioClip) {
		play(audioClip);
		//TODO
	}
	
	public void stop(AudioData audioClip) {
		//TODO
	}

	public void stopAll() {
		playlist.clear();
	}
}
