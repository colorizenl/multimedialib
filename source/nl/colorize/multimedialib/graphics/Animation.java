//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 2D sprite animation that shows a number of images in sequence. Every frame
 * in the animation is shown for a certain amount of time, indicated in seconds.
 * After all images have been shown, the animation will either loop or keep
 * showing the last image.
 * <p>
 * Note that this class *describes* the animation, it does not contain any
 * state that relates to *showing* the animation. This allows for using the
 * same animation data to display the animation multiple times simultaneously,
 * but it does mean the animation's playback state needs to be managed by the
 * user of this class.
 */
public class Animation implements AnimationInfo {
    
    private List<FrameInfo> frames;
    private boolean loop;
    
    private Animation(List<FrameInfo> frames, boolean loop) {
        this.frames = ImmutableList.copyOf(frames);
        this.loop = loop;
    }

    public Animation(boolean loop) {
        this.frames = new ArrayList<>();
        this.loop = loop;
    }
    
    public Animation(List<Image> frames, float frameTime, boolean loop) {
        this(loop);
        for (Image frame : frames) {
            addFrame(frame, frameTime);
        }
    }

    public Animation(Image frame) {
        this(false);
        addFrame(frame, 0f);
    }

    public void addFrame(Image frame, float frameTime) {
        Preconditions.checkArgument(frameTime >= 0f, "Invalid frame time: " + frameTime);

        frames.add(new FrameInfo(frame, frameTime));
    }

    public int getFrameCount() {
        return frames.size();
    }

    public List<Image> getFrameImages() {
        return frames.stream()
            .map(frame -> frame.image)
            .collect(Collectors.toList());
    }
    
    public Image getFrameAtIndex(int index) {
        return frames.get(index).image;
    }

    public Image getFrameAtTime(float time) {
        Preconditions.checkState(!frames.isEmpty(), "Animation does not contain any frames");

        if (frames.size() == 1) {
            return frames.get(0).image;
        }

        if (loop) {
            time = time % getDuration();
        }

        for (FrameInfo frame : frames) {
            time -= frame.frameTime;
            if (time < 0f) {
                return frame.image;
            }
        }

        return frames.get(frames.size() - 1).image;
    }

    @Override
    public float getDuration() {
        if (frames.size() <= 1) {
            return 0f;
        }

        float duration = 0f;
        for (FrameInfo frame : frames) {
            duration += frame.frameTime;
        }
        return duration;
    }

    public void setFrameTime(int index, float frameTime) {
        Preconditions.checkArgument(frameTime >= 0f, "Invalid frame time: " + frameTime);

        frames.get(index).frameTime = frameTime;
    }
    
    public void setFrameTimes(List<Float> frameTimes) {
        Preconditions.checkArgument(frameTimes.size() == frames.size(), 
            "Animation has " + frames.size() + " frame, but provided " + frameTimes.size());
        
        for (int i = 0; i < frameTimes.size(); i++) {
            frames.get(i).frameTime = frameTimes.get(i);
        }
    }

    public float getFrameTime(int index) {
        return frames.get(index).frameTime;
    }

    @Override
    public boolean isLoop() {
        return loop;
    }
    
    private List<FrameInfo> copyFrames() {
        return frames.stream()
            .map(frame -> new FrameInfo(frame.image, frame.frameTime))
            .collect(Collectors.toList());
    }
    
    /**
     * Returns a new animation with the same frames as this one, but with all
     * frames and corresponding frame times in reverse compared to the original.
     */
    public Animation reverse() {
        List<FrameInfo> reverseFrames = copyFrames();
        Collections.reverse(reverseFrames);
        
        return new Animation(reverseFrames, loop);
    }
    
    /**
     * Returns a new animation that contains all frames of this animation
     * followed by all frames of the specified other animation.
     */
    public Animation append(Animation other) {
        List<FrameInfo> combinedFrames = new ArrayList<>();
        combinedFrames.addAll(copyFrames());
        combinedFrames.addAll(other.copyFrames());
        
        return new Animation(combinedFrames, loop);
    }
    
    /**
     * Returns a new animation with the same frames as this one, but repeated
     * by the specified number of times.
     * 
     * @throws IllegalArgumentException if the new animation does not repeat
     *         at least twice (since repeating once would be identical to the
     *         original animation.
     */
    public Animation repeat(int times) {
        Preconditions.checkArgument(times >= 2, "Must repeat at least twice");
        
        List<FrameInfo> repeatingFrames = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            repeatingFrames.addAll(copyFrames());
        }
        
        return new Animation(repeatingFrames, loop);
    }
    
    /**
     * Returns a new animation with the same frames as this one, but appends the
     * reversed version of the animation. For example, if the original animation
     * consists of frames 1-2-3, the result would be 1-2-3-3-2-1.
     */
    public Animation mirror() {
        return append(reverse());
    }

    /**
     * Data structure for all information related to showing one of the frames
     * within the animation.
     */
    private static class FrameInfo {

        private Image image;
        private float frameTime;

        public FrameInfo(Image image, float frameTime) {
            this.image = image;
            this.frameTime = frameTime;
        }
    }
}
