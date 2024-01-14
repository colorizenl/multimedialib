//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Point3D;

/**
 * Transformation matrix that controls how a 3D polygon model is displayed on
 * the stage. This has a similar purpose {@link Transform}, though that class
 * focuses on transformations for 2D sprites.
 * <p>
 * Rotation is expressed in the number of degrees. Scale is represented
 * relative to the model's original scale, with 1.0 being the original scale.
 */
@Getter
@Setter
public class Transform3D {

    private Point3D position;
    private float rotationX;
    private float rotationY;
    private float rotationZ;
    private float scaleX;
    private float scaleY;
    private float scaleZ;

    public Transform3D() {
        this.position = new Point3D(0f, 0f, 0f);
        this.rotationX = 0f;
        this.rotationY = 0f;
        this.rotationZ = 0f;
        this.scaleX = 1f;
        this.scaleY = 1f;
        this.scaleZ = 1f;
    }

    public void setPosition(float x, float y, float z) {
        position = new Point3D(x, y, z);
    }

    public void addPosition(float deltaX, float deltaY, float deltaZ) {
        position = new Point3D(
            position.getX() + deltaX,
            position.getY() + deltaY,
            position.getZ() + deltaZ
        );
    }

    public void setRotation(float x, float y, float z) {
        rotationX = x;
        rotationY = y;
        rotationZ = z;
    }

    public void addRotation(float x, float y, float z) {
        rotationX += x;
        rotationY += y;
        rotationZ += z;
    }

    public void setScale(float x, float y, float z) {
        this.scaleX = x;
        this.scaleY = y;
        this.scaleZ = z;
    }

    public void setScale(float scale) {
        setScale(scale, scale, scale);
    }
}
