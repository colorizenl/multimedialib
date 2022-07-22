//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Graphic2D;
import nl.colorize.multimedialib.graphics.Primitive;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.Text;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;

/**
 * Visitor interface for rendering graphics based on the stage contents.
 * Graphics are visited in the order in which they should be drawn.
 */
public interface StageVisitor {

    default void preVisitStage(Stage stage) {
    }

    default void postVisitStage(Stage stage) {
    }

    default void preVisitLayer(Layer layer) {
    }

    default void postVisitLayer(Layer layer) {
    }

    default void preVisitGraphic(Graphic2D graphic, boolean visible) {
    }

    default void postVisitGraphic(Graphic2D graphic) {
    }

    public void drawBackground(ColorRGB color);

    public void drawSprite(Sprite sprite);

    public void drawLine(Primitive graphic, Line line);

    public void drawRect(Primitive graphic, Rect rect);

    public void drawCircle(Primitive graphic, Circle circle);

    public void drawPolygon(Primitive graphic, Polygon polygon);

    public void drawText(Text text);
}
