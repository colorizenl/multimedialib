//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import nl.colorize.multimedialib.graphics.AnimationInfo;
import nl.colorize.multimedialib.graphics.PolygonModel;
import nl.colorize.multimedialib.graphics.Transform3D;
import nl.colorize.multimedialib.math.MathUtils;
import nl.colorize.multimedialib.math.Point3D;

import java.util.Map;
import java.util.UUID;

public class TeaModel implements PolygonModel {

    private UUID modelId;
    private UUID meshId;
    private Map<String, AnimationInfo> animations;
    private Transform3D transform;
    private boolean primitive;

    protected TeaModel(UUID modelId, UUID meshId, Map<String, AnimationInfo> animations, boolean primitive) {
        this.modelId = modelId;
        this.meshId = meshId;
        this.animations = ImmutableMap.copyOf(animations);
        this.transform = new Transform3D();
        this.primitive = primitive;
    }

    @Override
    public void attach() {
        Browser.addModel(modelId.toString(), meshId.toString());
    }

    @Override
    public void detach() {
        Browser.removeModel(modelId.toString());
    }

    @Override
    public void update(float deltaTime) {
        Point3D position = transform.getPosition();
        float rotationX = (float) Math.toRadians(transform.getRotationX() * transform.getRotationAmount());
        float rotationY = (float) Math.toRadians(transform.getRotationY() * transform.getRotationAmount())
            + getRotationOffset();
        float rotationZ = (float) Math.toRadians(transform.getRotationZ() * transform.getRotationAmount());

        Browser.syncModel(modelId.toString(),
            position.getX(), position.getY(), position.getZ(),
            rotationX, rotationY, rotationZ,
            transform.getScaleX(), transform.getScaleY(), transform.getScaleZ());
    }

    private float getRotationOffset() {
        return primitive ? -MathUtils.HALF_PI : 0f;
    }

    @Override
    public Transform3D getTransform() {
        return transform;
    }

    @Override
    public Map<String, AnimationInfo> getAnimations() {
        return animations;
    }

    @Override
    public void playAnimation(String animation, boolean loop) {
        Preconditions.checkArgument(getAnimations().containsKey(animation),
            "Unknown animation: " + animation);

        Browser.playAnimation(modelId.toString(), meshId.toString(), animation, loop);
    }

    @Override
    public PolygonModel copy() {
        return new TeaModel(UUID.randomUUID(), meshId, animations, primitive);
    }
}
