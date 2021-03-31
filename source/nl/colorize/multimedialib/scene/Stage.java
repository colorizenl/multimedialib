//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.GraphicsLayer2D;
import nl.colorize.multimedialib.graphics.PolygonModel;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsContext2D;

import java.util.ArrayList;
import java.util.List;

/**
 * The stage contains all graphics that should be displayed as part of the
 * current scene. This includes a single layer of 3D graphics, plus a number
 * of 2D graphics layer that are drawn on top. If the renderer does not
 * support 3D graphics, only the 2D graphics will be displayed.
 * <p>
 * While the scene has full control over the stage, this control cannot outlive
 * the scene itself: at the end of the scene the contents of the stage are
 * cleared so that the next stage can take over.
 */
public final class Stage {

    private Canvas canvas;
    private List<GraphicsLayer2D> layers;
    private Point3D cameraPosition;
    private Point3D cameraTarget;
    private ColorRGB ambientLight;
    private ColorRGB lightColor;
    private Point3D lightPosition;
    private List<PolygonModel> models;

    public Stage(Canvas canvas) {
        this.canvas = canvas;
        this.layers = new ArrayList<>();
        this.cameraPosition = new Point3D(10f, 10f, 10f);
        this.cameraTarget = new Point3D(0f, 0f, 0f);
        this.ambientLight = new ColorRGB(100, 100, 100);
        this.lightColor = new ColorRGB(200, 200, 200);
        this.lightPosition = new Point3D(-1f, -0.8f, -0.2f);
        this.models = new ArrayList<>();
    }

    /**
     * Removes all 2D and 3D graphics from the stage. This is always called at
     * the end of a scene, but can also be used manually mid-scene.
     */
    public void clear() {
        for (PolygonModel model : models) {
            model.detach();
        }

        layers.clear();
        models.clear();
    }

    //---------------------------------
    // 2D graphics
    //---------------------------------

    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Attaches a graphics layer to the current scene, that will remain active
     * until the current scene has ended. The layer will be drawn on top of the
     * scene's 3D graphics (if any) plus any existing 2D graphics layers.
     */
    public void addLayer(GraphicsLayer2D layer) {
        if (!layers.contains(layer)) {
            layers.add(layer);
        }
    }

    /**
     * Same as {@link #addLayer(GraphicsLayer2D)}, but draws the layer *below*
     * any previously existing 2D graphics layers.
     */
    public void addBackgroundLayer(GraphicsLayer2D layer) {
        if (!layers.contains(layer)) {
            layers.add(0, layer);
        }
    }

    public void render2D(GraphicsContext2D graphicsContext) {
        for (GraphicsLayer2D layer : layers) {
            layer.render(graphicsContext);
        }
    }

    //---------------------------------
    // 3D graphics
    //---------------------------------

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
        model.attach();
        models.add(model);
    }

    public void remove(PolygonModel model) {
        model.detach();
        models.remove(model);
    }

    public Iterable<PolygonModel> getModels() {
        return models;
    }
}
