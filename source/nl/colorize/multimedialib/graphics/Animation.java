//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Shows a number of images in sequence. Every frame in the animation is shown
 * for a certain amount of time, indicated in seconds. After all images have
 * been shown, the animation will either loop or keep showing the last image.
 * <p>
 * Note that this class *describes* the animation, it does not contain any
 * state that relates to *showing* the animation. This allows for using the
 * same animation data to display the animation multiple times simultaneously,
 * but it does mean the animation's playback state needs to be managed by the
 * user of this class.
 */
public class Animation {
    
    private List<FrameInfo> frames;
    private boolean loop;

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

    public float getFrameTime(int index) {
        return frames.get(index).frameTime;
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
