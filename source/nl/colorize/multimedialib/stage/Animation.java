//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
public class Animation {
    
    private List<Frame> frames;
    private boolean loop;
    
    private Animation(List<Frame> frames, boolean loop) {
        this.frames = new ArrayList<>(frames);
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
        Preconditions.checkNotNull(frame, "Missing frame graphics");
        Preconditions.checkArgument(frameTime >= 0f, "Invalid frame time: " + frameTime);

        frames.add(new Frame(frame, frameTime));
    }

    public int getFrameCount() {
        return frames.size();
    }

    public List<Image> getFrameImages() {
        return frames.stream()
            .map(frame -> frame.image)
            .toList();
    }
    
    public Image getFrameAtIndex(int index) {
        return frames.get(index).image;
    }

    public Image getFrameAtTime(float time) {
        Preconditions.checkState(!frames.isEmpty(), "Animation does not contain any frames");

        if (frames.size() == 1) {
            return frames.getFirst().image;
        }

        if (loop) {
            time = time % getDuration();
        }

        for (Frame frame : frames) {
            time -= frame.frameTime;
            if (time < 0f) {
                return frame.image;
            }
        }

        return frames.getLast().image;
    }

    public float getDuration() {
        if (frames.size() <= 1) {
            return 0f;
        }

        float duration = 0f;
        for (Frame frame : frames) {
            duration += frame.frameTime;
        }
        return duration;
    }

    /**
     * Changes the frame time for the frame located at the specified index.
     */
    public void setFrameTime(int index, float frameTime) {
        Frame frame = frames.get(index);
        frames.set(index, new Frame(frame.image, frameTime));
    }

    /**
     * Changes the frame time for all frames within this animation. The number
     * of elements in the list needs to match the number of frames within this
     * animation.
     */
    public void setFrameTimes(List<Float> frameTimes) {
        Preconditions.checkArgument(frameTimes.size() == 1 || frameTimes.size() == frames.size(),
            "Animation has " + frames.size() + " frame, but provided " + frameTimes.size());
        
        for (int i = 0; i < frames.size(); i++) {
            Frame frame = frames.get(i);
            float time = frameTimes.get(frameTimes.size() == 1 ? 0 : i);
            frames.set(i, new Frame(frame.image, time));
        }
    }

    /**
     * Changes the frame time for all frames within this animation.
     */
    public void setFrameTime(float frameTime) {
        for (int i = 0; i < frames.size(); i++) {
            Frame frame = frames.get(i);
            frames.set(i, new Frame(frame.image, frameTime));
        }
    }

    public float getFrameTime(int index) {
        return frames.get(index).frameTime;
    }

    public boolean isLoop() {
        return loop;
    }

    public Animation copy() {
        return new Animation(copyFrames(), loop);
    }
    
    private List<Frame> copyFrames() {
        return frames.stream()
            .map(frame -> new Frame(frame.image, frame.frameTime))
            .toList();
    }
    
    /**
     * Returns a new animation with the same frames as this one, but with all
     * frames and corresponding frame times in reverse compared to the original.
     */
    public Animation reverse() {
        List<Frame> reverseFrames = new ArrayList<>();
        reverseFrames.addAll(copyFrames());
        Collections.reverse(reverseFrames);

        return new Animation(reverseFrames, loop);
    }
    
    /**
     * Returns a new animation that contains all frames of this animation
     * followed by all frames of the specified other animation.
     */
    public Animation append(Animation other) {
        List<Frame> combinedFrames = new ArrayList<>();
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
        
        List<Frame> repeatingFrames = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            repeatingFrames.addAll(copyFrames());
        }
        
        return new Animation(repeatingFrames, loop);
    }
    
    /**
     * Returns a new animation with the same frames as this one, but appends
     * the reversed version of the animation. For example, if the original
     * animation consists of frames {@code 1-2-3}, the result would be
     * {@code 1-2-3-3-2-1}.
     */
    public Animation mirror() {
        return append(reverse());
    }

    /**
     * Describes how one of the animation frames should be displayed. Frames
     * are immutable, modifying the animation requires swapping out the frame.
     */
    private record Frame(Image image, float frameTime) {

        public Frame {
            Preconditions.checkNotNull(image, "Missing frame image");
            Preconditions.checkArgument(frameTime >= 0f, "Invalid frame time: " + frameTime);
        }
    }
}
