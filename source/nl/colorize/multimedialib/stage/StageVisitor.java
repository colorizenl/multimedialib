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
 * Visitor interface for rendering graphics based on the stage contents.
 * Graphics are visited in the order in which they should be drawn.
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
     * stage's graphics are visited.
     */
    public void prepareStage(Stage stage);

    public void onGraphicAdded(Container parent, Graphic2D graphic);

    public void onGraphicRemoved(Container parent, Graphic2D graphic);

    /**
     * Visits the specified graphic in the scene graph. If the graphic is a
     * {@link Container}, the container itself will be visited <em>before</em>
     * any of its children.
     * <p>
     * Returns a boolean that indicates whether this graphic should be drawn
     * by the renderer. If this returns false, the corresponding {@code drawX}
     * method will <em>not</em> be called for this graphic (or any of its
     * children).
     */
    public boolean visitGraphic(Stage stage, Graphic2D graphic);

    public void drawBackground(ColorRGB color);

    public void drawSprite(Sprite sprite);

    public void drawLine(Primitive graphic, Line line);

    public void drawSegmentedLine(Primitive graphic, SegmentedLine line);

    public void drawRect(Primitive graphic, Rect rect);

    public void drawCircle(Primitive graphic, Circle circle);

    public void drawPolygon(Primitive graphic, Polygon polygon);

    public void drawText(Text text);
}
