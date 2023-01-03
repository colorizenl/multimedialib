//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.three;

import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.stage.Layer2D;
import nl.colorize.multimedialib.stage.StageVisitor;

/**
 * Renders 2D and 3D graphics using the <a href="https://threejs.org">three.js</a>
 * JavaScript library. Using this renderer requires the browser to support WebGL.
 */
public class ThreeGraphics implements StageVisitor {

    private Canvas canvas;

    public ThreeGraphics(Canvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void prepareLayer(Layer2D layer) {
    }

    @Override
    public void drawBackground(ColorRGB color) {
        //TODO
    }

    @Override
    public void drawSprite(Sprite sprite) {
        //TODO
    }

    @Override
    public void drawLine(Primitive graphic, Line line) {
        //TODO
    }

    @Override
    public void drawRect(Primitive graphic, Rect rect) {
        //TODO
    }

    @Override
    public void drawCircle(Primitive graphic, Circle circle) {
        //TODO
    }

    @Override
    public void drawPolygon(Primitive graphic, Polygon polygon) {
        //TODO
    }

    @Override
    public void drawText(Text text) {
        //TODO
    }
}
