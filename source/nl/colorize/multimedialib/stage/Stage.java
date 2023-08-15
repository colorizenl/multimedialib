//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.SegmentedLine;
import nl.colorize.multimedialib.math.Shape;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.scene.Updatable;
import nl.colorize.util.LogHelper;

import java.util.logging.Logger;

import static nl.colorize.multimedialib.stage.Primitive.TYPE_CIRCLE;
import static nl.colorize.multimedialib.stage.Primitive.TYPE_LINE;
import static nl.colorize.multimedialib.stage.Primitive.TYPE_POLYGON;
import static nl.colorize.multimedialib.stage.Primitive.TYPE_RECT;
import static nl.colorize.multimedialib.stage.Primitive.TYPE_SEGMENTED_LINE;

/**
 * The stage contains all graphics that are part of the current scene.
 * Depending on the renderer and current platform, the stage can contain
 * 2D and/or 3D graphics. The stage is structured as a
 * <a href="https://en.wikipedia.org/wiki/Scene_graph">scene graph</a>,
 * and can be traversed using a {@link StageVisitor}.
 * <p>
 * While the scene has full control over the stage, this control cannot
 * outlive the scene itself: at the end of the scene the contents of the
 * stage are cleared so that the next stage can take over.
 */
@Getter
@Setter
public class Stage implements Updatable {

    private final Canvas canvas;
    private ColorRGB backgroundColor;
    private final Container root;
    private final World3D world;

    private static final Logger LOGGER = LogHelper.getLogger(Stage.class);

    public Stage(GraphicsMode graphicsMode, Canvas canvas) {
        this.canvas = canvas;
        this.backgroundColor = ColorRGB.BLACK;
        this.root = new Container();
        this.world = graphicsMode == GraphicsMode.MODE_3D ? new World3D() : null;
    }

    @Override
    public void update(float deltaTime) {
        root.update(deltaTime);
        if (world != null) {
            world.update(deltaTime);
        }
    }

    /**
     * Creates a new container, adds it to the root of the scene graph, then
     * returns the created container. This is a convenience method to easily
     * create top-level containers.
     */
    public Container addContainer() {
        Container container = new Container();
        root.addChild(container);
        return container;
    }

    /**
     * Visits all layers and graphics on this stage, in the back-to-front order
     * in which they should be drawn.
     */
    public void visit(StageVisitor visitor) {
        visitor.prepareStage(this);
        visitor.drawBackground(backgroundColor);
        visitGraphic(root, visitor);
        //TODO visitor for 3D graphics
    }

    private void visitGraphic(Graphic2D graphic, StageVisitor visitor) {
        if (visitor.visitGraphic(graphic)) {
            if (graphic instanceof Container container) {
                visitContainer(container, visitor);
            } else if (graphic instanceof Sprite sprite) {
                visitor.drawSprite(sprite);
            } else if (graphic instanceof Primitive primitive) {
                visitPrimitive(primitive, visitor);
            } else if (graphic instanceof Text text) {
                visitor.drawText(text);
            } else {
                LOGGER.warning("Stage contains unknown graphics type: " + graphic);
            }
        }
    }

    private void visitContainer(Container container, StageVisitor visitor) {
        for (Graphic2D added : container.getAddedChildren().flush()) {
            visitor.onGraphicAdded(container, added);
        }

        for (Graphic2D removed : container.getRemovedChildren().flush()) {
            visitor.onGraphicRemoved(container, removed);
        }

        for (Graphic2D child : container.getChildren()) {
            visitGraphic(child, visitor);
        }
    }

    private void visitPrimitive(Primitive graphic, StageVisitor visitor) {
        Transform globalTransform = graphic.getGlobalTransform();
        Shape displayedShape = graphic.getShape().reposition(globalTransform.getPosition());

        switch (graphic.getShapeType()) {
            case TYPE_LINE -> visitor.drawLine(graphic, (Line) displayedShape);
            case TYPE_RECT -> visitor.drawRect(graphic, (Rect) displayedShape);
            case TYPE_CIRCLE -> visitor.drawCircle(graphic, (Circle) displayedShape);
            case TYPE_POLYGON -> visitor.drawPolygon(graphic, (Polygon) displayedShape);
            case TYPE_SEGMENTED_LINE -> visitor.drawSegmentedLine(graphic, (SegmentedLine) displayedShape);
            default -> LOGGER.warning("Stage contains unknown primitive type: " + graphic);
        }
    }

    /**
     * Removes all 2D and 3D graphics from the stage. This is always called at
     * the end of a scene, but can also be used manually mid-scene.
     */
    public void clear() {
        root.clearChildren();
        if (world != null) {
            //TODO
            world.getChildren().clear();
        }
    }

    /**
     * Returns a textual representation of the stage's current contents, which
     * can be used for testing and debugging purposes.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Stage\n");
        append(buffer, root, 1);
        //TODO append 3D graphics
        return buffer.toString();
    }

    private void append(StringBuilder buffer, Graphic2D graphic, int depth) {
        for (int i = 0; i < depth; i++) {
            buffer.append("    ");
        }

        buffer.append(graphic.toString());
        buffer.append("\n");

        if (graphic instanceof Container container) {
            for (Graphic2D child : container.getChildren()) {
                if (child.getTransform().isVisible()) {
                    append(buffer, child, depth + 1);
                }
            }
        }
    }
}
