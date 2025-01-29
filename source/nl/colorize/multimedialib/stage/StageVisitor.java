//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.SegmentedLine;

/**
 * Visitor interface that can be used to traverse all nodes currently on the
 * stage. Nodes are visited in the order in which they should be drawn, with
 * parent nodes visited before their children. The renderer uses this interface
 * to draw the graphics on stage after each frame update.
 */
public interface StageVisitor {

    /**
     * Prepares visiting the stage. This method is called before any of the
     * stage's graphics are visited. It can be used to add initialization
     * logic that should be performed before any graphics can be drawn.
     */
    public void prepareStage(Stage stage);

    /**
     * Indicates whether this visitor should visit <em>all</em> graphics, or
     * only graphics that are currently visible.
     */
    public boolean shouldVisitAllNodes();

    //----------------------------------------
    // 2D graphics
    //----------------------------------------

    public void visitContainer(Container container, Transform globalTransform);

    public void drawBackground(ColorRGB color);

    public void drawSprite(Sprite sprite, Transform globalTransform);

    public void drawLine(Primitive graphic, Line line, Transform globalTransform);

    public void drawSegmentedLine(Primitive graphic, SegmentedLine line, Transform globalTransform);

    public void drawRect(Primitive graphic, Rect rect, Transform globalTransform);

    public void drawCircle(Primitive graphic, Circle circle, Transform globalTransform);

    public void drawPolygon(Primitive graphic, Polygon polygon, Transform globalTransform);

    public void drawText(Text text, Transform globalTransform);

    /**
     * Called after all 2D nodes on the stage have been visited. This is an
     * optional method, the default implementation is empty.
     */
    default void finalize2D(Stage stage) {
    }

    //----------------------------------------
    // 3D graphics
    //----------------------------------------

    public void visitGroup(Group group, Transform3D globalTransform);

    public void drawMesh(Mesh mesh, Transform3D globalTransform);

    public void drawLight(Light light, Transform3D globalTransform);

    /**
     * Called after all 3D nodes on the stage have been visited, but before the
     * 2D nodes are visited. This allows renders that support both 2D and 3D
     * graphics to switch from rendering 3D graphics to rendering 2D graphics.
     * This is an optional method, the default implementation is empty.
     */
    default void finalize3D(Stage stage) {
    }
}
