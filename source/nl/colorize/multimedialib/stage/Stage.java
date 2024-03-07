//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
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
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.scene.Updatable;
import nl.colorize.util.LogHelper;

import java.util.logging.Logger;

/**
 * The stage contains all graphics that are part of the current scene.
 * Depending on the renderer and current platform, the stage can contain
 * 2D and/or 3D graphics. The stage is structured as a
 * <a href="https://en.wikipedia.org/wiki/Scene_graph">scene graph</a>,
 * and can be traversed using a {@link StageVisitor}. This is used by the
 * renderer at the end of each frame update, so the stage can be rendered.
 * <p>
 * While the scene has full control over the stage, this control cannot
 * outlive the scene itself: at the end of the scene the contents of the
 * stage are cleared so the next scene can take over.
 */
@Getter
public class Stage implements Updatable {

    private Canvas canvas;
    private FrameStats frameStats;

    private Container root;
    private World3D world;
    @Setter private ColorRGB backgroundColor;

    private static final String ROOT_CONTAINER_NAME = "$$root";
    private static final Logger LOGGER = LogHelper.getLogger(Stage.class);

    public Stage(GraphicsMode graphicsMode, Canvas canvas, FrameStats frameStats) {
        this.canvas = canvas;
        this.frameStats = frameStats;

        this.root = new Container(ROOT_CONTAINER_NAME);
        this.world = graphicsMode == GraphicsMode.MODE_3D ? new World3D() : null;
        this.backgroundColor = ColorRGB.BLACK;
    }

    @Override
    public void update(float deltaTime) {
        root.update(deltaTime);
        if (world != null) {
            world.update(deltaTime);
        }
    }

    /**
     * Creates a new container, then adds it to the root of the scene graph.
     * Returns the container that was just created. This is a convenience
     * method to easily create top-level containers.
     */
    public Container addContainer() {
        Container container = new Container();
        root.addChild(container);
        return container;
    }

    /**
     * Creates a new container with the specified name, then adds it to the
     * root of the scene graph. Returns the container that was just created.
     * This is a convenience method to easily create top-level containers.
     */
    public Container addContainer(String name) {
        Container container = new Container(name);
        root.addChild(container);
        return container;
    }

    /**
     * Visits all graphics currently on the stage, in the order in which they
     * should be drawn. This method is called by the renderer following each
     * frame update.
     */
    public void visit(StageVisitor visitor) {
        visitor.prepareStage(this);
        visitor.drawBackground(backgroundColor);
        visitGraphic(root, visitor);
    }

    /**
     * Visits all graphics on the stage, recursively. This method first draws
     * the graphic itself, then visits the graphic's children. To improve
     * performance, this method will only recurse if the specified graphic is
     * currently visible.
     */
    private void visitGraphic(Graphic2D graphic, StageVisitor visitor) {
        if (!shouldVisitGraphic(graphic, visitor)) {
            return;
        }

        switch (graphic) {
            case Container container -> visitContainer(container, visitor);
            case Sprite sprite -> visitor.drawSprite(sprite);
            case Primitive primitive -> visitPrimitive(primitive, visitor);
            case Text text -> visitor.drawText(text);
            default -> LOGGER.warning("Unknown graphics type: " + graphic.getClass());
        }

        frameStats.countGraphics(graphic);
    }

    /**
     * Returns whether the specified graphic should be visited. Stateful
     * renderers will need to track the status of every single graphic.
     * Stateless renderers only care about what needs to be drawn this
     * frame, so visiting non-visible graphics would result in needless
     * overhead.
     * <p>
     * This method will <em>always</em> visit containers. This is done for
     * performance reasons: calculating the stage bounds for containers is
     * an expensive operation, so this negates the performance benefit of
     * not visiting non-visible containers. The actual graphics within the
     * container will still perform the visibility check.
     */
    private boolean shouldVisitGraphic(Graphic2D graphic, StageVisitor visitor) {
        return visitor.shouldVisitAllGraphics() ||
            graphic instanceof Container ||
            isVisible(graphic);
    }

    /**
     * Returns true if the specified graphic is entirely or partially visible
     * within the canvas, based on its current transform and the value
     * returned by {@link Transform#isVisible()}.
     */
    private boolean isVisible(Graphic2D graphic) {
        if (!graphic.getTransform().isVisible() || !graphic.getGlobalTransform().isVisible()) {
            return false;
        }

        Rect bounds = graphic.getStageBounds();
        return canvas.getBounds().intersects(bounds);
    }

    private void visitContainer(Container container, StageVisitor visitor) {
        visitor.visitContainer(container);

        for (Graphic2D child : container.getChildren()) {
            visitGraphic(child, visitor);
        }
    }

    private void visitPrimitive(Primitive graphic, StageVisitor visitor) {
        Transformable globalTransform = graphic.getGlobalTransform();
        Shape displayedShape = graphic.getShape().reposition(globalTransform.getPosition());

        switch (displayedShape) {
            case Line line -> visitor.drawLine(graphic, line);
            case SegmentedLine sLine -> visitor.drawSegmentedLine(graphic, sLine);
            case Rect rect -> visitor.drawRect(graphic, rect);
            case Circle circle -> visitor.drawCircle(graphic, circle);
            case Polygon polygon -> visitor.drawPolygon(graphic, polygon);
            default -> LOGGER.warning("Unknown primitive: " + displayedShape.getClass());
        }
    }

    /**
     * Removes all 2D and 3D graphics from the stage. This is always called at
     * the end of a scene, but can also be used manually mid-scene.
     */
    public void clear() {
        root.clearChildren();
        if (world != null) {
            world.clear();
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
        if (world != null) {
            buffer.append("World\n");
            for (PolygonModel model : world.getChildren()) {
                append(buffer, model, 1);
            }
        }
        return buffer.toString();
    }

    private void append(StringBuilder buffer, Graphic2D graphic, int depth) {
        buffer.append("    ".repeat(depth));
        buffer.append(graphic.toString());
        buffer.append("\n");

        if (graphic instanceof Container container) {
            for (Graphic2D child : container.getChildren()) {
                append(buffer, child, depth + 1);
            }
        }
    }

    private void append(StringBuilder buffer, PolygonModel model, int depth) {
        buffer.append("    ".repeat(depth));
        buffer.append(model.toString());
        buffer.append("\n");
    }
}
