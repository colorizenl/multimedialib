//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Graphic2D;
import nl.colorize.multimedialib.graphics.Primitive;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.Text;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Shape;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.util.LogHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * The stage contains all graphics that should be displayed as part of the
 * current scene. The stage consists of a number of 2D graphics layers.
 * Renderers that also support 3D graphics will use {@link Stage3D}, an
 * extension of the stage that supports both 2D and 3D graphics.
 * <p>
 * The graphics layers which are drawn in the order in which they were created.
 * Similarly, the contents of the layer are also drawn in the order in which
 * they were created. Visitors can be used to ensure a predictable drawing
 * order across different renderers.
 * <p>
 * While the scene has full control over the stage, this control cannot outlive
 * the scene itself: at the end of the scene the contents of the stage are
 * cleared so that the next stage can take over.
 */
public class Stage implements Updatable {

    private Canvas canvas;

    private ColorRGB backgroundColor;
    private Map<String, Layer> layers;
    private Multimap<String, Graphic2D> layerGraphics;
    private Set<Graphic2D> allGraphics;
    private List<StageObserver> observers;

    public static final Layer DEFAULT_LAYER = Layer.DEFAULT;
    private static final Logger LOGGER = LogHelper.getLogger(Stage.class);

    protected Stage(Canvas canvas) {
        this.canvas = canvas;

        this.backgroundColor = ColorRGB.BLACK;
        this.layers = new LinkedHashMap<>();
        this.layerGraphics = ArrayListMultimap.create();
        this.allGraphics = new HashSet<>();
        this.observers = new ArrayList<>();
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
     * Creates a new 2D graphics layer with the specified name. The new layer
     * will be drawn on top of all existing layers. Returns the created
     * {@link Layer}, which can be used when adding graphics to the layer.
     */
    public Layer addLayer(String name) {
        Preconditions.checkArgument(!layers.containsKey(name), "Layer already exists: " + name);

        Layer layer = new Layer(name);
        layers.put(name, layer);
        return layer;
    }

    /**
     * Adds graphics to the specified layer. The graphics will be drawn on top
     * of all existing graphics within the same layer.
     */
    public void add(Layer layer, Graphic2D graphic) {
        add(layer.getName(), graphic);
    }

    /**
     * Adds graphics to the layer with the specified name. The graphics will be
     * drawn on top of all existing graphics within the same layer.
     */
    public void add(String layerName, Graphic2D graphic) {
        ensureDefaultLayerExists();
        Layer layer = layers.get(layerName);

        Preconditions.checkArgument(layer != null,
            "Cannot add graphics to nonexistent layer: " + layerName);
        Preconditions.checkState(!contains(graphic), "Stage already contains " + graphic);

        layerGraphics.put(layerName, graphic);
        allGraphics.add(graphic);
        observers.forEach(observer -> observer.onGraphicAdded(layer, graphic));
    }

    /**
     * Adds graphics to the default layer. The default layer will automatically
     * be created if it doesn't already exist.
     */
    public void add(Graphic2D graphic) {
        ensureDefaultLayerExists();
        add(DEFAULT_LAYER, graphic);
    }

    private void ensureDefaultLayerExists() {
        if (!layers.containsKey(DEFAULT_LAYER.getName())) {
            layers.put(DEFAULT_LAYER.getName(), DEFAULT_LAYER);
        }
    }

    /**
     * Removes graphics from the specified layer.
     */
    public void remove(Layer layer, Graphic2D graphic) {
        remove(layer.getName(), graphic);
    }

    /**
     * Removes graphics from the layer with the specified name.
     */
    public void remove(String layerName, Graphic2D graphic) {
        if (layerGraphics.remove(layerName, graphic)) {
            allGraphics.remove(graphic);
            Layer layer = layers.get(layerName);
            observers.forEach(observer -> observer.onGraphicRemoved(layer, graphic));
        }
    }

    /**
     * Removes graphics, automatically removing it from all layers where it
     * might be used. If the graphic was not part of the stage this method
     * does nothing.
     */
    public void remove(Graphic2D graphic) {
        for (Map.Entry<String, Layer> entry : layers.entrySet()) {
            if (layerGraphics.remove(entry.getKey(), graphic)) {
                Layer layer = entry.getValue();
                observers.forEach(observer -> observer.onGraphicRemoved(layer, graphic));
            }
        }

        allGraphics.remove(graphic);
    }

    /**
     * Iterates over all layers in the order in which they should be drawn,
     * back to front.
     */
    public Iterable<Layer> getLayers() {
        return layers.values();
    }

    /**
     * Returns the layer with the specified name. Throws an exception if no
     * such layer exists.
     */
    public Layer getLayer(String name) {
        Preconditions.checkArgument(layers.containsKey(name), "No such layer: " + name);
        return layers.get(name);
    }

    public boolean hasLayer(String layer) {
        return layers.containsKey(layer);
    }

    public boolean contains(Layer layer, Graphic2D graphic) {
        return layerGraphics.containsEntry(layer.getName(), graphic);
    }

    public boolean contains(String layerName, Graphic2D graphic) {
        return layerGraphics.containsEntry(layerName, graphic);
    }

    public boolean contains(Graphic2D graphic) {
        return allGraphics.contains(graphic);
    }

    /**
     * Visits all graphics on this stage, in the back-to-front order in which
     * they should be drawn.
     */
    public void visit(StageVisitor visitor) {
        visitor.preVisitStage(this);
        visitor.drawBackground(backgroundColor);

        for (Layer layer : getLayers()) {
            visitor.preVisitLayer(layer);

            for (Graphic2D graphic : layerGraphics.get(layer.getName())) {
                boolean visible = graphic.isVisible();
                visitor.preVisitGraphic(graphic, visible);
                if (visible) {
                    visitGraphic(graphic, visitor);
                    visitor.postVisitGraphic(graphic);
                }
            }

            visitor.postVisitLayer(layer);
        }

        visitor.postVisitStage(this);
    }

    private void visitGraphic(Graphic2D graphic, StageVisitor visitor) {
        if (graphic instanceof Sprite) {
            visitor.drawSprite((Sprite) graphic);
        } else if (graphic instanceof Primitive) {
            visitPrimitive((Primitive) graphic, visitor);
        } else if (graphic instanceof Text) {
            visitor.drawText((Text) graphic);
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
        for (Graphic2D graphic : layerGraphics.values()) {
            graphic.update(deltaTime);
        }
    }

    /**
     * Removes all 2D and 3D graphics from the stage. This is always called at
     * the end of a scene, but can also be used manually mid-scene.
     */
    public void clear() {
        layers.clear();
        layerGraphics.clear();
        observers.forEach(observer -> observer.onStageCleared());
    }

    public void addObserver(StageObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(StageObserver observer) {
        observers.remove(observer);
    }
}
