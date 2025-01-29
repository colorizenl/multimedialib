//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
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
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.Group;
import nl.colorize.multimedialib.stage.Light;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.multimedialib.stage.Primitive;
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

    private List<String> rendered;

    public MockStageVisitor() {
        this.rendered = new ArrayList<>();
    }

    @Override
    public void prepareStage(Stage stage) {
        rendered.clear();
    }

    @Override
    public boolean shouldVisitAllNodes() {
        return false;
    }

    @Override
    public void visitContainer(Container container, Transform globalTransform) {
    }

    @Override
    public void drawBackground(ColorRGB color) {
        rendered.add("background");
    }

    @Override
    public void drawSprite(Sprite sprite, Transform globalTransform) {
        rendered.add("sprite");
    }

    @Override
    public void drawLine(Primitive graphic, Line line, Transform globalTransform) {
        rendered.add("line");
    }

    @Override
    public void drawSegmentedLine(Primitive graphic, SegmentedLine line, Transform globalTransform) {
        rendered.add("segmentedline");
    }

    @Override
    public void drawRect(Primitive graphic, Rect rect, Transform globalTransform) {
        rendered.add("rect");
    }

    @Override
    public void drawCircle(Primitive graphic, Circle circle, Transform globalTransform) {
        rendered.add("circle");
    }

    @Override
    public void drawPolygon(Primitive graphic, Polygon polygon, Transform globalTransform) {
        rendered.add("polygon");
    }

    @Override
    public void drawText(Text text, Transform globalTransform) {
        rendered.add("text");
    }
    @Override
    public void visitGroup(Group group, Transform3D globalTransform) {
    }

    @Override
    public void drawMesh(Mesh mesh, Transform3D globalTransform) {
        rendered.add("mesh");
    }

    @Override
    public void drawLight(Light light, Transform3D globalTransform) {
        rendered.add("light");
    }
}
