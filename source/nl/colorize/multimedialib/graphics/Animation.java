//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Shows a number of images in sequence. After all images have been shown the
 * animation will either loop or keep showing the last image. 
 */
public class Animation {
	
	private List<ImageRegion> frames;
	private float frameDuration;
	private boolean loop;
	
	/**
	 * Creates an animation that will consist of the specified images.
	 * @param frameDuration Time each image should be shown, in seconds.
	 * @throws IllegalArgumentException if there are no images.
	 */
	public Animation(List<ImageRegion> frames, float frameDuration, boolean loop) {
		if (frames.isEmpty()) {
			throw new IllegalArgumentException("Animation must consist of at least 1 frame");
		}
		
		if (frames.size() >= 2 && frameDuration <= 0f) {
			throw new IllegalArgumentException("Invalid frame duration: " + frameDuration);
		}
		
		this.frames = new ArrayList<ImageRegion>();
		this.frames.addAll(frames);
		this.frameDuration = frameDuration;
		this.loop = loop;
	}

	/**
	 * Creates an animation that will display a single, static image.
	 */
	public Animation(ImageRegion frame) {
		this(ImmutableList.of(frame), 0f, false); 
	}
	
	public ImageRegion getFrameAtIndex(int index) {
		return frames.get(index);
	}
	
	public ImageRegion getFrameAtTime(float time) {
		if (loop && time > getTotalDuration()) {
			time = time % getTotalDuration();  
		}
		
		for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
			float frameStartTime = frameIndex * frameDuration;
			float frameEndTime = frameStartTime + frameDuration;
			if (time >= frameStartTime && time < frameEndTime) {
				return frames.get(frameIndex);
			}
		}
		
		return frames.get(frames.size() - 1);
	}
	
	public int getNumFrames() {
		return frames.size();
	}
	
	public float getFrameDuration() {
		return frameDuration;
	}
	
	public float getTotalDuration() {
		if (frames.size() == 1) {
			return 0f;
		}
		return frames.size() * frameDuration;
	}
	
	public boolean isLoop() {
		return loop;
	}
}
