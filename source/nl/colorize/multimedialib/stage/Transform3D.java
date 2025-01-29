//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Angle;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.util.stats.Aggregate;

/**
 * Transformation matrix that controls how polygonal 3D graphics are displayed.
 * This class is the equivalent of {@link Transform} but for 3D graphics.
 * The following transform properties are available:
 * <p>
 * <ul>
 *   <li>Position</li>
 *   <li>Rotation (in degrees)</li>
 *   <li>Scale (as a percentage of the original)</li>
 *   <li>Alpha (as a percentage, with 0% being fully transparent)</li>
 * </ul>>
 */
@Setter
@Getter
public class Transform3D {

    private boolean visible;
    private Point3D position;
    private Angle rotationX;
    private Angle rotationY;
    private Angle rotationZ;
    private float scaleX;
    private float scaleY;
    private float scaleZ;

    public Transform3D() {
        this.visible = true;
        this.position = Point3D.ORIGIN;
        this.rotationX = Angle.ORIGIN;
        this.rotationY = Angle.ORIGIN;
        this.rotationZ = Angle.ORIGIN;
        this.scaleX = 100f;
        this.scaleY = 100f;
        this.scaleZ = 100f;
    }

    public void set(Transform3D source) {
        this.visible = source.isVisible();
        this.position = source.getPosition();
        this.rotationX = source.getRotationX();
        this.rotationY = source.getRotationY();
        this.rotationZ = source.getRotationZ();
        this.scaleX = source.getScaleX();
        this.scaleY = source.getScaleY();
        this.scaleZ = source.getScaleZ();
    }

    public void setPosition(float x, float y, float z) {
        position = new Point3D(x, y, z);
    }

    public void addPosition(float deltaX, float deltaY, float deltaZ) {
        position = position.move(deltaX, deltaY, deltaZ);
    }

    public void setX(float x) {
        position = new Point3D(x, position.y(), position.z());
    }

    public void setY(float y) {
        position = new Point3D(position.x(), y, position.z());
    }

    public void setZ(float z) {
        position = new Point3D(position.x(), position.y(), z);
    }

    public void setRotation(float rotationX, float rotationY, float rotationZ) {
        this.rotationX = new Angle(rotationX);
        this.rotationY = new Angle(rotationY);
        this.rotationZ = new Angle(rotationZ);
    }

    public void addRotation(float degreesX, float degreesY, float degreesZ) {
        this.rotationX = rotationX.move(degreesX);
        this.rotationY = rotationY.move(degreesY);
        this.rotationZ = rotationZ.move(degreesZ);
    }

    public void setScale(float scaleX, float scaleY, float scaleZ) {
        setScaleX(scaleX);
        setScaleY(scaleY);
        setScaleZ(scaleZ);
    }

    public void setScale(float scale) {
        setScale(scale, scale, scale);
    }

    /**
     * Returns a new {@link Transform3D} instance that is the result of
     * combining this transform with the specified other transform.
     */
    public Transform3D combine(Transform3D other) {
        Transform3D combined = new Transform3D();
        combined.setVisible(visible && other.visible);
        combined.setPosition(position.move(other.position));
        combined.setRotation(
            rotationX.degrees() + other.rotationX.degrees(),
            rotationY.degrees() + other.rotationY.degrees(),
            rotationZ.degrees() + other.rotationZ.degrees()
        );
        combined.setScale(
            Aggregate.multiplyPercentage(scaleX, other.scaleX),
            Aggregate.multiplyPercentage(scaleY, other.scaleY),
            Aggregate.multiplyPercentage(scaleZ, other.scaleZ)
        );
        return combined;
    }
}
