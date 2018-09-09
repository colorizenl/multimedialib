//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.RenderContext;
import nl.colorize.util.animation.Animatable;
import nl.colorize.util.animation.Animator;
import nl.colorize.util.animation.TimedAnimation;

import java.util.List;

/**
 * Standard implementation of a scene that contains multiple animated objects
 * that are updated every frame. This class can be extended by subclasses that
 * then render the frame based on the animations' current state.
 */
public abstract class AnimatedScene implements Scene {

    private SceneAnimator animator;

    public AnimatedScene() {
        animator = new SceneAnimator();
    }

    @Override
    public void onSceneStart(MediaLoader mediaLoader) {
        animator.start();
    }

    @Override
    public void onFrame(float deltaTime, InputDevice input) {
        animator.onFrame(deltaTime);
    }

    @Override
    public abstract void onRender(RenderContext context);

    @Override
    public void onSceneEnd() {
        animator.stop();
    }

    public void add(Animatable anim) {
        animator.play(anim);
    }

    public void add(Animatable anim, float duration) {
        if (anim instanceof TimedAnimation) {
            add(anim);
        } else {
            add(TimedAnimation.from(anim, duration));
        }
    }

    public void remove(Animatable anim) {
        animator.cancel(anim);
    }

    public void removeAll() {
        animator.cancelAll();
    }

    public List<Animatable> getContents() {
        return animator.getCurrentlyPlaying();
    }

    public boolean contains(Animatable anim) {
        return animator.getCurrentlyPlaying().contains(anim);
    }

    /**
     * Animated scene that allows registration of a number of animated objects, all
     * of which are updated during every frame update. The animator itself implements
     * the {@link Animatable} interface, and needs to be updated once a frame by the
     * currently active scene in order to actually play animations. T
     */
    public class SceneAnimator extends Animator implements Animatable {

        private boolean active;

        public SceneAnimator() {
            this.active = false;
        }

        @Override
        public void start() {
            active = true;
        }

        @Override
        public void stop() {
            active = false;
        }

        @Override
        public void onFrame(float deltaTime) {
            if (active) {
                performFrameUpdate(deltaTime);
            }
        }
    }
}
