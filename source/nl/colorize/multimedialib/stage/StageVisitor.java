//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.SegmentedLine;

/**
 * Visitor interface that visits all graphics currently on the stage, visiting
 * them in the order in which they should be drawn.
 * <p>
 * For the various {@code drawX} methods that operate on {@link Primitive}s,
 * the provided shape is already normalized to the position and size how it
 * should be drawn on stage. In other words, the shape is defined using the
 * coordinate system of the stage, not the coordinate system of the graphic
 * or of its parent.
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
    public boolean shouldVisitAllGraphics();

    /**
     * Visits a container. This method does not actually need to <em>draw</em>
     * the container's graphics, since the corresponding {@code drawX} methods
     * will be called for all the container's children. This method will be
     * called <em>before</em> the container's children are visited. It can be
     * used to handle logic related to the container itself, for example to
     * process children that were added or removed since the last frame
     * update.
     */
    public void visitContainer(Container container);

    public void drawBackground(ColorRGB color);

    public void drawSprite(Sprite sprite);

    public void drawLine(Primitive graphic, Line line);

    public void drawSegmentedLine(Primitive graphic, SegmentedLine line);

    public void drawRect(Primitive graphic, Rect rect);

    public void drawCircle(Primitive graphic, Circle circle);

    public void drawPolygon(Primitive graphic, Polygon polygon);

    public void drawText(Text text);
}
