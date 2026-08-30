//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import lombok.Getter;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.SegmentedLine;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Group;
import nl.colorize.multimedialib.stage.ImageTransform;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Spatial2D;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.StageVisitor;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.stage.Transform;
import nl.colorize.multimedialib.stage.Transform3D;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MockStageVisitor implements StageVisitor {

    private List<Spatial2D> graphics;
    private List<String> rendered;

    public MockStageVisitor() {
        this.graphics = new ArrayList<>();
        this.rendered = new ArrayList<>();
    }

    @Override
    public void prepareStage(Stage stage) {
        rendered.clear();
    }

    @Override
    public void drawBackground(ColorRGB color) {
        rendered.add("background");
    }

    @Override
    public void drawSprite(Sprite sprite, ImageTransform globalTransform) {
        graphics.add(sprite);
        rendered.add("sprite");
    }

    @Override
    public void drawLine(Primitive graphic, Line line, Transform globalTransform) {
        graphics.add(graphic);
        rendered.add("line");
    }

    @Override
    public void drawSegmentedLine(Primitive graphic, SegmentedLine line, Transform globalTransform) {
        graphics.add(graphic);
        rendered.add("segmentedline");
    }

    @Override
    public void drawRect(Primitive graphic, Rect rect, Transform globalTransform) {
        graphics.add(graphic);
        rendered.add("rect");
    }

    @Override
    public void drawCircle(Primitive graphic, Circle circle, Transform globalTransform) {
        graphics.add(graphic);
        rendered.add("circle");
    }

    @Override
    public void drawPolygon(Primitive graphic, Polygon polygon, Transform globalTransform) {
        graphics.add(graphic);
        rendered.add("polygon");
    }

    @Override
    public void drawText(Text text, Transform globalTransform) {
        graphics.add(text);
        rendered.add("text");
    }
    @Override
    public void visitGroup(Group group, Transform3D globalTransform) {
    }

    @Override
    public void drawMesh(Mesh mesh, Transform3D globalTransform) {
        rendered.add("mesh");
    }
}
