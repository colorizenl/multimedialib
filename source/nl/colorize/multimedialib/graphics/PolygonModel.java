//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Point3D;

import java.util.UUID;

/**
 * An instance of a polygon mesh that can added to the stage and displayed.
 * Multiple models can share the same underlying mesh.
 */
public final class PolygonModel {

    private UUID id;
    private PolygonMesh mesh;

    private Point3D position;
    private float rotationX;
    private float rotationY;
    private float rotationZ;
    private float rotationAmount;
    private float scaleX;
    private float scaleY;
    private float scaleZ;

    protected PolygonModel(UUID id, PolygonMesh mesh) {
        this.id = id;
        this.mesh = mesh;

        this.position = new Point3D(0f, 0f, 0f);
        this.rotationX = 0f;
        this.rotationY = 0f;
        this.rotationZ = 0f;
        this.rotationAmount = 0f;
        this.scaleX = 1f;
        this.scaleY = 1f;
        this.scaleZ = 1f;
    }

    protected PolygonModel(PolygonMesh mesh) {
        this(UUID.randomUUID(), mesh);
    }

    public UUID getId() {
        return id;
    }

    public PolygonMesh getMesh() {
        return mesh;
    }

    public AnimationInfo getAnimation(String name) {
        return mesh.getAnimation(name);
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

    public void setRotation(float x, float y, float z, float amount) {
        rotationX = x;
        rotationY = y;
        rotationZ = z;
        rotationAmount = amount;
    }

    public float getRotationX() {
        return rotationX;
    }

    public float getRotationY() {
        return rotationY;
    }

    public float getRotationZ() {
        return rotationZ;
    }

    public float getRotationAmount() {
        return rotationAmount;
    }

    public void setScale(float scaleX, float scaleY, float scaleZ) {
        Preconditions.checkArgument(scaleX > 0f, "Invalid X scale: " + scaleX);
        Preconditions.checkArgument(scaleY > 0f, "Invalid Y scale: " + scaleY);
        Preconditions.checkArgument(scaleZ > 0f, "Invalid Z scale: " + scaleZ);

        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
    }

    public void setScale(float scale) {
        setScale(scale, scale, scale);
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public float getScaleZ() {
        return scaleZ;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PolygonModel) {
            PolygonModel other = (PolygonModel) o;
            return id.equals(other.id);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return mesh.getName() + " (" + id + ")";
    }
}
