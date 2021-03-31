//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import nl.colorize.multimedialib.graphics.AnimationInfo;
import nl.colorize.multimedialib.graphics.PolygonModel;
import nl.colorize.multimedialib.graphics.Transform3D;

import java.util.HashMap;
import java.util.Map;

public class GDXModel implements PolygonModel {

    private ModelInstance instance;
    private Transform3D transform;
    private Map<String, AnimationInfo> animations;
    private AnimationController animationController;

    protected GDXModel(ModelInstance instance) {
        this.instance = instance;
        this.transform = new Transform3D();
        this.animations = new HashMap<>();

        for (Animation anim : instance.model.animations) {
            animations.put(anim.id, new GDXModelAnimation(anim));
        }
    }

    protected ModelInstance getInstance() {
        return instance;
    }

    @Override
    public void attach() {
    }

    @Override
    public void detach() {
    }

    @Override
    public void update(float deltaTime) {
        instance.transform.setToTranslation(transform.getPosition().getX(),
            transform.getPosition().getY(), transform.getPosition().getZ());
        instance.transform.rotate(transform.getRotationX(), transform.getRotationY(),
            transform.getRotationZ(), transform.getRotationAmount());
        instance.transform.scale(transform.getScaleX(), transform.getScaleY(), transform.getScaleZ());

        if (animationController != null) {
            animationController.update(deltaTime);

            if (isAnimationCompleted()) {
                animationController = null;
            }
        }
    }

    @Override
    public Map<String, AnimationInfo> getAnimations() {
        return animations;
    }

    @Override
    public void playAnimation(String animation, boolean loop) {
        int loopCount = loop ? Integer.MAX_VALUE : 1;
        animationController = new AnimationController(instance);
        animationController.animate(animation, loopCount, 1f, null, 0f);
    }

    private boolean isAnimationCompleted() {
        return animationController == null ||
            animationController.current == null ||
            animationController.current.loopCount == 0;
    }

    @Override
    public Transform3D getTransform() {
        return transform;
    }

    @Override
    public PolygonModel copy() {
        ModelInstance instanceCopy = new ModelInstance(instance.model);
        return new GDXModel(instanceCopy);
    }

    private static class GDXModelAnimation implements AnimationInfo {

        private Animation animation;

        public GDXModelAnimation(Animation animation) {
            this.animation = animation;
        }

        @Override
        public float getDuration() {
            return animation.duration;
        }

        @Override
        public boolean isLoop() {
            return false;
        }
    }
}
