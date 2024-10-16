//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.three;

import nl.colorize.multimedialib.math.*;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.teavm.Browser;
import nl.colorize.multimedialib.renderer.teavm.TeaGraphics;
import nl.colorize.multimedialib.renderer.teavm.TeaMediaLoader;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.Graphic2D;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.stage.Transform;

/**
 * Renders 2D and 3D graphics using the <a href="https://threejs.org">three.js</a>
 * JavaScript library. Using this renderer requires the browser to support WebGL.
 */
public class ThreeGraphics implements TeaGraphics {

    private Canvas canvas;
    private ThreeBridge three;

    public ThreeGraphics(Canvas canvas) {
        this.canvas = canvas;
        this.three = Browser.getThreeBridge();
    }

    @Override
    public void init(TeaMediaLoader mediaLoader) {
        three.init();
    }

    @Override
    public int getDisplayWidth() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDisplayHeight() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void prepareStage(Stage stage) {
        //TODO
    }

    @Override
    public boolean shouldVisitAllGraphics() {
        return true;
    }

    @Override
    public void visitContainer(Container container, Transform globalTransform) {
        //TODO
    }

    @Override
    public void drawBackground(ColorRGB color) {
        //TODO
    }

    @Override
    public void drawSprite(Sprite sprite, Transform globalTransform) {
        //TODO
    }

    @Override
    public void drawLine(Primitive graphic, Line line, Transform globalTransform) {
        //TODO
    }

    @Override
    public void drawSegmentedLine(Primitive graphic, SegmentedLine line, Transform globalTransform) {
        //TODO
    }

    @Override
    public void drawRect(Primitive graphic, Rect rect, Transform globalTransform) {
        //TODO
    }

    @Override
    public void drawCircle(Primitive graphic, Circle circle, Transform globalTransform) {
        //TODO
    }

    @Override
    public void drawPolygon(Primitive graphic, Polygon polygon, Transform globalTransform) {
        //TODO
    }

    @Override
    public void drawText(Text text, Transform globalTransform) {
        //TODO
    }

    @Override
    public GraphicsMode getGraphicsMode() {
        return GraphicsMode.MODE_3D;
    }
}
