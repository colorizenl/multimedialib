//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Shows a number of images in sequence. After all images have been shown the
 * animation will either loop or keep showing the last image. 
 */
public class Animation {
    
    private List<Image> frames;
    private float frameDuration;
    private boolean loop;
    
    public Animation(List<Image> frames, float frameDuration, boolean loop) {
        Preconditions.checkArgument(!frames.isEmpty(), "Animation must consist of at least 1 frame");
        Preconditions.checkArgument(frames.size() == 1 || frameDuration > 0f,
                "Invalid frame duration: " + frameDuration);

        this.frames = ImmutableList.copyOf(frames);
        this.frameDuration = frameDuration;
        this.loop = loop;
    }

    public Animation(Image frame) {
        this(ImmutableList.of(frame), 0f, false); 
    }
    
    public Image getFrameAtIndex(int index) {
        return frames.get(index);
    }
    
    public Image getFrameAtTime(float time) {
        if (frames.size() == 1) {
            return frames.get(0);
        }

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

    public List<Image> getFrames() {
        return frames;
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
