//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;
import lombok.Getter;
import nl.colorize.util.TupleList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 2D sprite animation that shows a number of images in sequence. Every frame
 * in the animation is shown for a certain amount of time, indicated in seconds.
 * After all images have been shown, the animation will either loop or keep
 * showing the last image.
 * <p>
 * Note that this class is immutable and describes the animation, it does not
 * contain any state that relates to <em>showing</em> the animation. This
 * allows multiple sprites to use the same animation data.
 */
public class Animation {
    
    private List<Frame> frames;
    @Getter private boolean loop;
    @Getter private double duration;

    /**
     * Internal constructor that creates an animation from frames that have
     * been previously parsed by another {@link Animation} instance.
     */
    private Animation(List<Frame> frames, boolean loop) {
        this.frames = new ArrayList<>(frames);
        this.loop = loop;
    }

    /**
     * Creates an animation from the specified frame, with each frame being
     * represented by an image and its frame time.
     *
     * @throws IllegalArgumentException if no frames have been provided, or
     *         if one of the frames has an invalid frame time.
     */
    public Animation(TupleList<Image, Double> frameImages, boolean loop) {
        Preconditions.checkArgument(!frameImages.isEmpty(), "Empty animation");

        this.frames = frameImages.stream()
            .map(frameImage -> new Frame(frameImage.left(), frameImage.right()))
            .toList();
        this.loop = loop;
        this.duration = frames.stream()
            .mapToDouble(Frame::frameTime)
            .sum();
    }

    /**
     * Creates an animation from the specified frame, with each frame being
     * represented by an image and its frame time.
     *
     * @throws IllegalArgumentException if no frames have been provided, or
     *         if one of the frames has an invalid frame time, or if the
     *         number of frame images and frame times do not match.
     */
    public Animation(List<Image> frameImages, List<Double> frameTimes, boolean loop) {
        this(TupleList.combine(frameImages, frameTimes), loop);
    }

    /**
     * Creates an animation from the specified frame, with each frame having
     * the same frame time.
     *
     * @throws IllegalArgumentException if no frames have been provided, or
     *         if the frame time is invalid.
     */
    public Animation(List<Image> frameImages, double frameTime, boolean loop) {
        this(TupleList.combine(frameImages, Collections.nCopies(frameImages.size(), frameTime)), loop);
    }

    public Animation(boolean loop) {
        this.frames = new ArrayList<>();
        this.loop = loop;
    }

    public Animation(Image frame) {
        this(TupleList.of(frame, 0.0), false);
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

    public double getFrameTime(int index) {
        return frames.get(index).frameTime;
    }

    public Image getFrameAtTime(double time) {
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

    /**
     * Returns a new animation with the same frames as this one, but with all
     * frames and corresponding frame times in reverse compared to the original.
     */
    public Animation reverse() {
        List<Frame> reverseFrames = new ArrayList<>(frames);
        Collections.reverse(reverseFrames);

        return new Animation(reverseFrames, loop);
    }
    
    /**
     * Returns a new animation that contains all frames of this animation
     * followed by all frames of the specified other animation.
     */
    public Animation append(Animation other) {
        List<Frame> combinedFrames = new ArrayList<>();
        combinedFrames.addAll(frames);
        combinedFrames.addAll(other.frames);

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
            repeatingFrames.addAll(frames);
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
    private record Frame(Image image, double frameTime) {

        public Frame {
            Preconditions.checkNotNull(image, "Missing frame image");
            Preconditions.checkArgument(frameTime >= 0.0, "Invalid frame time: " + frameTime);
        }
    }
}
