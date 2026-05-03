//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.SegmentedLine;
import nl.colorize.multimedialib.math.Shape;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.World3D;
import nl.colorize.multimedialib.scene.Timer;
import nl.colorize.util.LogHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * The stage contains the graphics and audio for the currently active scene.
 * The stage can contain both 2D and 3D graphics. It is structured as a
 * <a href="https://en.wikipedia.org/wiki/Scene_graph">scene graph</a>,
 * where transforming a parent node propagates to its children.
 * <p>
 * While the scene has full control over the stage, this control cannot
 * outlive the scene itself: at the end of the scene the contents of the
 * stage are cleared so the next scene can take over.
 */
@Getter
@Setter
public final class Stage {

    private final Canvas canvas;
    private final Timer animationTimer;

    private final Container root;
    private ColorRGB backgroundColor;

    private World3D world3D;
    private final Group root3D;
    private List<Light> lights;
    private Point3D cameraPosition;
    private Point3D cameraFocus;
    private ColorRGB ambientLightColor;

    private static final String ROOT_CONTAINER_2D = "$$root";
    private static final String ROOT_CONTAINER_3D = "$$root3D";
    private static final ColorRGB DEFAULT_AMBIENT_LIGHT_COLOR = new ColorRGB(220, 220, 220);
    private static final float SAFE_ZONE_PADDING = 64f;
    private static final Logger LOGGER = LogHelper.getLogger(Stage.class);

    public Stage(Canvas canvas) {
        this.canvas = canvas;
        this.animationTimer = Timer.infinite();

        this.root = new Container(ROOT_CONTAINER_2D);
        this.backgroundColor = ColorRGB.BLACK;

        this.root3D = new Group(ROOT_CONTAINER_3D);
        this.lights = new ArrayList<>();
        this.cameraPosition = new Point3D(0, 20, 10);
        this.cameraFocus = Point3D.ORIGIN;
        this.ambientLightColor = DEFAULT_AMBIENT_LIGHT_COLOR;
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
     * Creates a new group with the specified name, then adds it to the root
     * container for 3D graphics to make it part of the stage.
     */
    public Group addGroup(String name) {
        Group group = new Group(name);
        root3D.addChild(group);
        return group;
    }

    /**
     * Detaches the specified node from the scene graph. If the node is not
     * currently part of the stage, calling this method does nothing.
     *
     * @deprecated Prefer using {@link StageNode2D#detach()} instead. This
     *             method remains only for backward compatibility.
     */
    @Deprecated
    public void detach(StageNode2D node) {
        node.detach();
    }

    /**
     * Detaches the specified node from the scene graph. If the node is not
     * currently part of the stage, calling this method does nothing.
     *
     * @deprecated Prefer using {@link StageNode2D#detach()} instead. This
     *             method remains only for backward compatibility.
     */
    @Deprecated
    public void detach(StageNode3D node) {
        node.detach();
    }

    /**
     * Removes all 2D and 3D graphics from the stage. This is always called at
     * the end of a scene, but can also be used manually mid-scene.
     */
    public void clear() {
        root.getChildren().clear();
        root3D.getChildren().clear();
    }

    /**
     * Visits all nodes that are currently part of the stage. Nodes will be
     * visited in the order in which they should be drawn. Parent nodes will
     * be visited before their children.
     */
    public void visit(StageVisitor visitor) {
        visitor.prepareStage(this);
        visitor.drawBackground(backgroundColor);
        if (world3D != null) {
            visitNode3D(root3D, root3D.getTransform(), visitor);
            visitor.finalize3D(this);
        }
        visitNode2D(root, root.getTransform(), visitor);
        visitor.finalize2D(this);
    }

    private void visitNode2D(StageNode2D node, Transform globalTransform, StageVisitor visitor) {
        node.getGlobalTransform().set(globalTransform);

        if (!globalTransform.isVisible() || !shouldDraw(node)) {
            return;
        }

        node.animate(animationTimer);

        switch (node) {
            case Container container -> visitContainer(container, globalTransform, visitor);
            case Sprite sprite -> visitor.drawSprite(sprite, (ImageTransform) globalTransform);
            case Primitive primitive -> visitPrimitive(primitive, globalTransform, visitor);
            case Text text -> visitor.drawText(text, globalTransform);
            default -> LOGGER.warning("Unknown 2D graphics type: " + node.getClass());
        }
    }

    private boolean shouldDraw(StageNode2D node) {
        if (node instanceof Container) {
            return true;
        }

        if (canvas.getBounds().contains(node.getGlobalTransform().getPosition())) {
            return true;
        }

        if (node instanceof Text) {
            return false;
        }

        return canvas.getBounds().expand(SAFE_ZONE_PADDING).intersects(node.getStageBounds());
    }

    private void visitContainer(Container container, Transform globalTransform, StageVisitor visitor) {
        visitor.visitContainer(container, globalTransform);

        for (StageNode2D child : container) {
            Transform childLocalTransform = child.getTransform();
            Transform childGlobalTransform = globalTransform.combine(childLocalTransform);
            visitNode2D(child, childGlobalTransform, visitor);
        }
    }

    private void visitPrimitive(Primitive graphic, Transform globalTransform, StageVisitor visitor) {
        Shape displayedShape = graphic.getRawShape().reposition(globalTransform.getPosition());

        switch (displayedShape) {
            case Line line -> visitor.drawLine(graphic, line, globalTransform);
            case SegmentedLine sLine -> visitor.drawSegmentedLine(graphic, sLine, globalTransform);
            case Rect rect -> visitor.drawRect(graphic, rect, globalTransform);
            case Circle circle -> visitor.drawCircle(graphic, circle, globalTransform);
            case Polygon polygon -> visitor.drawPolygon(graphic, polygon, globalTransform);
            default -> LOGGER.warning("Unknown primitive: " + displayedShape.getClass());
        }
    }

    private void visitNode3D(StageNode3D node, Transform3D globalTransform, StageVisitor visitor) {
        node.getGlobalTransform().set(globalTransform);

        if (!globalTransform.isVisible()) {
            return;
        }

        node.animate(animationTimer);

        switch (node) {
            case Group group -> visitGroup(group, globalTransform, visitor);
            case Mesh mesh -> visitor.drawMesh(mesh, globalTransform);
            default -> LOGGER.warning("Unknown 3D graphics type: " + node.getClass());
        }
    }

    private void visitGroup(Group group, Transform3D globalTransform, StageVisitor visitor) {
        visitor.visitGroup(group, globalTransform);

        for (StageNode3D child : group) {
            Transform3D childLocalTransform = child.getTransform();
            Transform3D childGlobalTransform = globalTransform.combine(childLocalTransform);
            visitNode3D(child, childGlobalTransform, visitor);
        }
    }

    /**
     * Returns the path towards the specified node. The first element in the
     * list is the root node, the last element is the node itself. If the
     * node is not currently part of the stage, this returns a list containing
     * only the node itself.
     */
    public List<StageNode2D> findNodePath(StageNode2D node) {
        List<StageNode2D> nodePath = new ArrayList<>();
        StageNode2D currentNode = node;
        while (currentNode != null) {
            nodePath.addFirst(currentNode);
            currentNode = currentNode.getParent();
        }
        return nodePath;
    }

    /**
     * Returns the path towards the specified node. The first element in the
     * list is the root node, the last element is the node itself. If the
     * node is not currently part of the stage, this returns a list containing
     * only the node itself.
     */
    public List<StageNode3D> findNodePath(StageNode3D node) {
        List<StageNode3D> nodePath = new ArrayList<>();
        StageNode3D currentNode = node;
        while (currentNode != null) {
            nodePath.addFirst(currentNode);
            currentNode = currentNode.getParent();
        }
        return nodePath;
    }

    /**
     * Recalculates the global transform for the specified node. The global
     * transform is normally updated at the end of each frame update when the
     * node is drawn. This method can be used to force-recalculate the global
     * transform for a single node.
     * <p>
     * Since the global transform is relative to the node's parent, this will
     * also recalculate the global transform for its parents, recursively.
     * This method therefore has a performance impact, and should only be used
     * on/for a limited number of nodes per frame update.
     */
    public void recalculateGlobalTransform(StageNode2D node) {
        List<StageNode2D> nodePath = findNodePath(node);
        nodePath.getFirst().getGlobalTransform().set(nodePath.getFirst().getTransform());

        for (int i = 1; i < nodePath.size(); i++) {
            Transform parentGlobalTransform = nodePath.get(i - 1).getGlobalTransform();
            Transform localTransform = nodePath.get(i).getTransform();
            Transform globalTransform = parentGlobalTransform.combine(localTransform);
            nodePath.get(i).getGlobalTransform().set(globalTransform);
        }
    }

    /**
     * Recalculates the global transform for the specified node. The global
     * transform is normally updated at the end of each frame update when the
     * node is drawn. This method can be used to force-recalculate the global
     * transform for a single node.
     * <p>
     * Since the global transform is relative to the node's parent, this will
     * also recalculate the global transform for its parents, recursively.
     * This method therefore has a performance impact, and should only be used
     * on/for a limited number of nodes per frame update.
     */
    public void recalculateGlobalTransform(StageNode3D node) {
        List<StageNode3D> nodePath = findNodePath(node);
        nodePath.getFirst().getGlobalTransform().set(nodePath.getFirst().getTransform());

        for (int i = 1; i < nodePath.size(); i++) {
            Transform3D parentGlobalTransform = nodePath.get(i - 1).getGlobalTransform();
            Transform3D localTransform = nodePath.get(i).getTransform();
            Transform3D globalTransform = parentGlobalTransform.combine(localTransform);
            nodePath.get(i).getGlobalTransform().set(globalTransform);
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
        if (world3D != null) {
            append(buffer, root3D, 1);
        }
        return buffer.toString();
    }

    private void append(StringBuilder buffer, StageNode2D node, int depth) {
        buffer.append("    ".repeat(depth));
        buffer.append(node.toString());
        buffer.append("\n");

        if (node instanceof Container container) {
            for (StageNode2D child : container) {
                append(buffer, child, depth + 1);
            }
        }
    }

    private void append(StringBuilder buffer, StageNode3D node, int depth) {
        buffer.append("    ".repeat(depth));
        buffer.append(node.toString());
        buffer.append("\n");

        if (node instanceof Group group) {
            for (StageNode3D child : group) {
                append(buffer, child, depth + 1);
            }
        }
    }
}
