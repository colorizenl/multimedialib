//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Shape;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.UnsupportedGraphicsModeException;
import nl.colorize.multimedialib.scene.Updatable;
import nl.colorize.util.LogHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The stage contains all graphics that should be displayed as part of the
 * current scene. The stage consists multiple 2D graphics layers, plus a 3D
 * graphics layer if the renderer supports 3D graphics.
 * <p>
 * The graphics layers which are drawn in the order in which they were created.
 * Similarly, the contents of the layer are also drawn in the order in which
 * they were created.
 * <p>
 * While the scene has full control over the stage, this control cannot outlive
 * the scene itself: at the end of the scene the contents of the stage are
 * cleared so that the next stage can take over.
 */
public class Stage implements Updatable {

    private Canvas canvas;
    private FrameStats frameStats;
    private ColorRGB backgroundColor;
    private Map<String, Layer2D> layers2D;
    private Layer3D layer3D;
    private List<StageObserver> observers;

    public static final String DEFAULT_LAYER = Layer2D.DEFAULT_LAYER;
    private static final Logger LOGGER = LogHelper.getLogger(Stage.class);

    public Stage(GraphicsMode graphicsMode, Canvas canvas, FrameStats frameStats) {
        this.canvas = canvas;
        this.frameStats = frameStats;
        this.backgroundColor = ColorRGB.BLACK;
        this.layers2D = new LinkedHashMap<>();
        this.observers = new ArrayList<>();

        // Add default layers for 2D and (when supported) 3D graphics.
        addLayer(DEFAULT_LAYER);
        if (graphicsMode == GraphicsMode.MODE_3D) {
            layer3D = new Layer3D(observers);
        }
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setBackgroundColor(ColorRGB backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public ColorRGB getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Creates a new 2D graphics layer with the specified name, and adds it to
     * the stage. The new layer will be drawn on top of all existing layers.
     *
     * @throws IllegalArgumentException if the stage already contains a layer
     *         with the same name.
     */
    public Layer2D addLayer(String name) {
        Preconditions.checkArgument(!name.isEmpty(), "Invalid layer name");
        Preconditions.checkArgument(!layers2D.containsKey(name),
            "Layer with same name already exists: " + name);

        Layer2D layer = new Layer2D(name, observers);
        layers2D.put(name, layer);
        return layer;
    }

    /**
     * Returns the layer with the specified name. This assumes the layer
     * already exists, use {@link #retrieveLayer(String)} if you want to
     * simultaneously create the layer.
     *
     * @throws IllegalArgumentException if no layer with the specified
     *         name currently exists.
     */
    public Layer2D getLayer(String name) {
        Layer2D layer = layers2D.get(name);
        Preconditions.checkArgument(layer != null, "No such layer: " + name);
        return layer;
    }

    public Layer2D getDefaultLayer() {
        return getLayer(DEFAULT_LAYER);
    }

    public boolean hasLayer(String name) {
        return layers2D.containsKey(name);
    }

    /**
     * Returns the 2D graphics layer with the specified name, or creates the
     * layer if it does not yet exist. If the layer needs to be created, it is
     * done using {@link #addLayer(String)}.
     */
    public Layer2D retrieveLayer(String name) {
        if (hasLayer(name)) {
            return getLayer(name);
        } else {
            return addLayer(name);
        }
    }

    /**
     * Returns the solitary 3D graphics layer on the stage. This method is only
     * available if the renderer supports 3D graphics. If not, calling this
     * method will result in an {@link UnsupportedGraphicsModeException}.
     */
    public Layer3D getLayer3D() {
        if (layer3D == null) {
            throw new UnsupportedGraphicsModeException("3D graphics not supported");
        }
        return layer3D;
    }

    /**
     * Adds graphics to the layer with the specified name. This method is a
     * shorthand for {@code getLayer(layerName).add(graphic)}.
     *
     * @throws IllegalArgumentException if no layer with the specified name
     *         currently exists.
     */
    public void add(String layerName, Graphic2D graphic) {
        getLayer(layerName).add(graphic);
    }

    /**
     * Removes graphics from any layer on the stage where it might exist.
     *
     * @deprecated Use {@code getLayer(...).remove(graphic}} to remove the
     *             graphic from the correct layer instead. This method has
     *             poor performance behavior for large and complex stages.
     */
    @Deprecated
    public void remove(Graphic2D graphic) {
        layers2D.values().forEach(layer -> layer.remove(graphic));
    }

    /**
     * Visits all layers and graphics on this stage, in the back-to-front order
     * in which they should be drawn.
     */
    public void visit(StageVisitor visitor) {
        Rect canvasBounds = canvas.getBounds();

        frameStats.resetDrawOperations();
        visitor.preVisitStage(this);
        visitor.drawBackground(backgroundColor);

        for (Layer2D layer : layers2D.values()) {
            visitor.prepareLayer(layer);

            for (Graphic2D graphic : layer.getGraphics()) {
                boolean visible = determineGraphicVisible(graphic, canvasBounds);
                visitor.preVisitGraphic(graphic, visible);
                if (visible) {
                    visitGraphic(graphic, visitor);
                    visitor.postVisitGraphic(graphic);
                    frameStats.markDrawOperation(graphic);
                }
            }
        }

        visitor.postVisitStage(this);
    }

    private boolean determineGraphicVisible(Graphic2D graphic, Rect canvasBounds) {
        return graphic.isVisible() && graphic.getBounds().intersects(canvasBounds);
    }

    private void visitGraphic(Graphic2D graphic, StageVisitor visitor) {
        if (graphic instanceof Sprite sprite) {
            visitor.drawSprite(sprite);
        } else if (graphic instanceof Primitive primitive) {
            visitPrimitive(primitive, visitor);
        } else if (graphic instanceof Text text) {
            visitor.drawText(text);
        } else {
            LOGGER.warning("Stage contains unknown graphics type: " + graphic);
        }
    }

    private void visitPrimitive(Primitive graphic, StageVisitor visitor) {
        Shape displayedShape = graphic.getShape().reposition(graphic.getPosition());

        switch (graphic.getShapeType()) {
            case Primitive.TYPE_LINE -> visitor.drawLine(graphic, (Line) displayedShape);
            case Primitive.TYPE_RECT -> visitor.drawRect(graphic, (Rect) displayedShape);
            case Primitive.TYPE_CIRCLE -> visitor.drawCircle(graphic, (Circle) displayedShape);
            case Primitive.TYPE_POLYGON -> visitor.drawPolygon(graphic, (Polygon) displayedShape);
            default -> LOGGER.warning("Stage contains unknown primitive type: " + graphic);
        }
    }

    @Override
    public void update(float deltaTime) {
        for (Layer2D layer : layers2D.values()) {
            layer.update(deltaTime);
        }

        if (layer3D != null) {
            layer3D.update(deltaTime);
        }
    }

    /**
     * Removes all 2D and 3D graphics from the stage. This is always called at
     * the end of a scene, but can also be used manually mid-scene.
     */
    public void clear() {
        layers2D.clear();
        addLayer(DEFAULT_LAYER);
        if (layer3D != null) {
            layer3D.clear();
        }

        observers.forEach(observer -> observer.onStageCleared());
    }

    public List<StageObserver> getObservers() {
        return observers;
    }

    /**
     * Returns a textual representation of the stage's current contents, which
     * can be used for testing and debugging purposes. The stage is depicted as
     * it is drawn, so layers and graphics "higher" in the textual representation
     * are drawn on top.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Stage\n");

        for (Layer2D layer : ImmutableList.copyOf(layers2D.values()).reverse()) {
            buffer.append("    2D graphics layer [" + layer.getName() + "]\n");
            for (Graphic2D graphic : ImmutableList.copyOf(layer.getGraphics()).reverse()) {
                buffer.append("        " + graphic + "\n");
            }
        }

        if (layer3D != null) {
            buffer.append("    3D graphics layer\n");
            for (PolygonModel model : layer3D.getModels()) {
                buffer.append("        PolygonModel\n");
            }
        }

        return buffer.toString();
    }
}
