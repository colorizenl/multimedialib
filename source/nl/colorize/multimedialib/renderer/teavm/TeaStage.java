//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.graphics.AnimationInfo;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.PolygonMesh;
import nl.colorize.multimedialib.graphics.PolygonModel;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.renderer.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TeaStage implements Stage {

    private Point3D lastCameraPosition;
    private List<PolygonModel> models;

    public TeaStage() {
        this.lastCameraPosition = new Point3D(0f, 0f, 0f);
        this.models = new ArrayList<>();
    }

    @Override
    public void update(float deltaTime) {
        for (PolygonModel model : models) {
            Point3D position = model.getPosition();
            float rotationX = (float) Math.toRadians(model.getRotationX() * model.getRotationAmount());
            float rotationY = (float) Math.toRadians(model.getRotationY() * model.getRotationAmount());
            float rotationZ = (float) Math.toRadians(model.getRotationZ() * model.getRotationAmount());

            Browser.syncModel(model.getId().toString(),
                position.getX(), position.getY(), position.getZ(),
                rotationX, rotationY, rotationZ,
                model.getScaleX(), model.getScaleY(), model.getScaleZ());
        }
    }

    @Override
    public void moveCamera(Point3D position, Point3D target) {
        Browser.moveCamera(position.getX(), position.getY(), position.getZ(),
            target.getX(), target.getY(), target.getZ());
        lastCameraPosition.set(position);
    }

    @Override
    public Point3D getCameraPosition() {
        return lastCameraPosition;
    }

    @Override
    public void changeAmbientLight(ColorRGB color) {
        Browser.changeAmbientLight(color.toHex());
    }

    @Override
    public void changeLight(ColorRGB color, Point3D target) {
        Browser.changeLight(color.toHex());
    }

    public void loadModel() {

    }

    @Override
    public void add(PolygonModel model) {
        models.add(model);
        Browser.addModel(model.getId().toString(), model.getMesh().getId().toString());
    }

    @Override
    public void remove(PolygonModel model) {
        models.remove(model);
        Browser.removeModel(model.getId().toString());
    }

    @Override
    public void clear() {
        models.clear();
        Browser.clearModels();
    }

    @Override
    public void playAnimation(PolygonModel model, AnimationInfo animation, boolean loop) {
        Browser.playAnimation(model.getId().toString(), model.getMesh().getId().toString(),
            animation.getName(), loop);
    }

    @Override
    public PolygonMesh createQuad(Point2D size, ColorRGB color) {
        return createBox(new Point3D(size.getX(), 0.001f, size.getY()), color);
    }

    @Override
    public PolygonMesh createQuad(Point2D size, Image image) {
        return createBox(new Point3D(size.getX(), 0.001f, size.getY()), image);
    }

    @Override
    public PolygonMesh createBox(Point3D size, ColorRGB color) {
        UUID id = UUID.randomUUID();
        Browser.createBox(id.toString(), size.getX(), size.getY(), size.getZ(), color.toHex(), null);
        return registerMeshData(id, "box");
    }

    @Override
    public PolygonMesh createBox(Point3D size, Image image) {
        UUID id = UUID.randomUUID();
        TeaImage texture = (TeaImage) image;
        Browser.createBox(id.toString(), size.getX(), size.getY(), size.getZ(),
            null, texture.getOrigin().getPath());
        return registerMeshData(id, "box");
    }

    @Override
    public PolygonMesh createSphere(float diameter, ColorRGB color) {
        UUID id = UUID.randomUUID();
        Browser.createSphere(id.toString(), diameter, color.toHex(), null);
        return registerMeshData(id, "sphere");
    }

    @Override
    public PolygonMesh createSphere(float diameter, Image image) {
        UUID id = UUID.randomUUID();
        TeaImage texture = (TeaImage) image;
        Browser.createSphere(id.toString(), diameter, null, texture.getOrigin().getPath());
        return registerMeshData(id, "sphere");
    }

    private PolygonMesh registerMeshData(UUID id, String name) {
        return new PolygonMesh(id, name, Collections.emptyList());
    }
}
