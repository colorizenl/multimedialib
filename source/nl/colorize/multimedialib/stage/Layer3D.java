//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.scene.Updatable;

import java.util.ArrayList;
import java.util.List;

/**
 * Access to the solitary 3D graphics layer on the stage. This layer is only
 * available when using a renderer that supports 3D graphics. If both 2D and 3D
 * graphics are used, this layer will appear "below" all 2D graphics.
 */
public class Layer3D implements Updatable {

    private Point3D cameraPosition;
    private Point3D cameraTarget;

    private ColorRGB ambientLight;
    private ColorRGB lightColor;
    private Point3D lightPosition;

    private List<PolygonModel> models;

    private List<StageObserver> observers;

    protected Layer3D(List<StageObserver> observers) {
        this.models = new ArrayList<>();
        this.observers = observers;
        clear();
    }

    public void clear() {
        cameraPosition = new Point3D(10f, 10f, 10f);
        cameraTarget = new Point3D(0f, 0f, 0f);

        ambientLight = new ColorRGB(100, 100, 100);
        lightColor = new ColorRGB(200, 200, 200);
        lightPosition = new Point3D(-1f, -0.8f, -0.2f);

        models.clear();;
    }

    public void moveCamera(Point3D position, Point3D target) {
        this.cameraPosition = position;
        this.cameraTarget = target;
    }

    public Point3D getCameraPosition() {
        return cameraPosition;
    }

    public Point3D getCameraTarget() {
        return cameraTarget;
    }

    public void changeAmbientLight(ColorRGB color) {
        this.ambientLight = color;
    }

    public ColorRGB getAmbientLight() {
        return ambientLight;
    }

    public void changeLight(ColorRGB color, Point3D target) {
        this.lightColor = color;
        this.lightPosition = target;
    }

    public ColorRGB getLightColor() {
        return lightColor;
    }

    public Point3D getLightPosition() {
        return lightPosition;
    }

    public void add(PolygonModel model) {
        models.add(model);
        observers.forEach(observer -> observer.onModelAdded(model));
    }

    public void remove(PolygonModel model) {
        models.remove(model);
        observers.forEach(observer -> observer.onModelRemoved(model));
    }

    public Iterable<PolygonModel> getModels() {
        return models;
    }

    @Override
    public void update(float deltaTime) {
        for (PolygonModel model : models) {
            model.update(deltaTime);
        }
    }
}
