//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Point3D;

/**
 * Transformation matrix that controls how a 3D polygon model is displayed on
 * the stage. This has a similar purpose {@link Transform}, though that class
 * focuses on transformations for 2D sprites.
 * <p>
 * Rotation is expressed in the number of degrees. Scale is represented
 * relative to the model's original scale, with 1.0 being the original scale.
 */
public class Transform3D {

    private Point3D position;
    private float[] rotation;
    private float[] scale;

    public Transform3D() {
        this.position = new Point3D(0f, 0f, 0f);
        this.rotation = new float[] {0f, 0f, 0f};
        this.scale = new float[] {1f, 1f, 1f};
    }

    public void setPosition(Point3D p) {
        position.set(p);
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
    }

    public Point3D getPosition() {
        return position;
    }

    public void setRotation(float x, float y, float z) {
        rotation[0] = x;
        rotation[1] = y;
        rotation[2] = z;
    }

    public float getRotationX() {
        return rotation[0];
    }

    public float getRotationY() {
        return rotation[1];
    }

    public float getRotationZ() {
        return rotation[2];
    }

    public void setScale(float scaleX, float scaleY, float scaleZ) {
        Preconditions.checkArgument(scaleX > 0f, "Invalid X scale: " + scaleX);
        Preconditions.checkArgument(scaleY > 0f, "Invalid Y scale: " + scaleY);
        Preconditions.checkArgument(scaleZ > 0f, "Invalid Z scale: " + scaleZ);

        scale[0] = scaleX;
        scale[1] = scaleY;
        scale[2] = scaleZ;
    }

    public void setScale(float scale) {
        setScale(scale, scale, scale);
    }

    public float getScaleX() {
        return scale[0];
    }

    public float getScaleY() {
        return scale[1];
    }

    public float getScaleZ() {
        return scale[2];
    }

    public Transform3D copy() {
        Transform3D copy = new Transform3D();
        copy.setPosition(position);
        copy.setRotation(rotation[0], rotation[1], rotation[2]);
        copy.setScale(scale[0], scale[1], scale[2]);
        return copy;
    }
}
