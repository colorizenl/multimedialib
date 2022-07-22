//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.PolygonModel;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GeometryBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Stage that contains 3D graphics in addition to a number of 2D graphics
 * layers. The 2D graphics layers are drawn on top of the 3D graphics.
 * <p>
 * The 3D graphics layer starts out with ambient lighting, a directional light,
 * and a camera. These can be modified, and models and geometry can be added to
 * the layer.
 */
public class Stage3D extends Stage {

    private Point3D cameraPosition;
    private Point3D cameraTarget;

    private ColorRGB ambientLight;
    private ColorRGB lightColor;
    private Point3D lightPosition;

    private List<PolygonModel> models;
    private GeometryBuilder geometryBuilder;

    protected Stage3D(Canvas canvas, GeometryBuilder geometryBuilder) {
        super(canvas);

        this.cameraPosition = new Point3D(10f, 10f, 10f);
        this.cameraTarget = new Point3D(0f, 0f, 0f);

        this.ambientLight = new ColorRGB(100, 100, 100);
        this.lightColor = new ColorRGB(200, 200, 200);
        this.lightPosition = new Point3D(-1f, -0.8f, -0.2f);

        this.models = new ArrayList<>();
        this.geometryBuilder = geometryBuilder;
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
    }

    public void remove(PolygonModel model) {
        models.remove(model);
    }

    public Iterable<PolygonModel> getModels() {
        return models;
    }

    public GeometryBuilder getGeometryBuilder() {
        return geometryBuilder;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        for (PolygonModel model : models) {
            model.update(deltaTime);
        }
    }

    @Override
    public void clear() {
        models.clear();
        super.clear();
    }
}
