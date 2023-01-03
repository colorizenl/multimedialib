//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.stage.ModelAnimation;
import nl.colorize.multimedialib.stage.PolygonModel;
import nl.colorize.multimedialib.stage.Transform3D;

import java.util.HashMap;
import java.util.Map;

public class GDXModel implements PolygonModel {

    private ModelInstance instance;
    private Transform3D transform;
    private Map<String, Animation> animations;
    private AnimationController animationController;

    protected GDXModel(ModelInstance instance) {
        this.instance = instance;
        this.transform = new Transform3D();
        this.animations = new HashMap<>();

        for (Animation anim : instance.model.animations) {
            animations.put(anim.id, anim);
        }
    }

    protected ModelInstance getInstance() {
        return instance;
    }

    @Override
    public void update(float deltaTime) {
        instance.transform.setToTranslation(transform.getPosition().getX(),
            transform.getPosition().getY(), transform.getPosition().getZ());

        instance.transform.rotate(1f, 0f, 0f, transform.getRotationX());
        instance.transform.rotate(0f, 1f, 0f, transform.getRotationY());
        instance.transform.rotate(0f, 0f, 1f, transform.getRotationZ());

        instance.transform.scale(transform.getScaleX(), transform.getScaleY(), transform.getScaleZ());

        if (animationController != null) {
            animationController.update(deltaTime);

            if (isAnimationCompleted()) {
                animationController = null;
            }
        }
    }

    @Override
    public ModelAnimation getAnimation(String name) {
        Animation animation = animations.get(name);
        Preconditions.checkArgument(animation != null, "No such animation: " + name);
        return new ModelAnimation(name, animation.duration, false);
    }

    @Override
    public void playAnimation(ModelAnimation animation) {
        int loopCount = animation.loop() ? Integer.MAX_VALUE : 1;
        animationController = new AnimationController(instance);
        animationController.animate(animation.name(), loopCount, 1f, null, 0f);
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
}
