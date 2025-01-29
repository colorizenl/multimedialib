//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.headless;

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
import nl.colorize.multimedialib.stage.StageNode2D;
import nl.colorize.multimedialib.stage.StageNode3D;
import nl.colorize.multimedialib.stage.StageVisitor;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.stage.Transform;
import nl.colorize.multimedialib.stage.Transform3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Does not draw any graphics, but collects the visited stage nodes in a list.
 * The list is cleared at the start of every frame update.
 */
@Getter
public class CollectingStageVisitor implements StageVisitor {

    private List<StageNode2D> nodes2D;
    private List<StageNode3D> nodes3D;

    public CollectingStageVisitor() {
        this.nodes2D = new ArrayList<>();
        this.nodes3D = new ArrayList<>();
    }

    @Override
    public void prepareStage(Stage stage) {
        nodes2D.clear();
        nodes3D.clear();
    }

    @Override
    public boolean shouldVisitAllNodes() {
        return false;
    }

    @Override
    public void visitContainer(Container container, Transform globalTransform) {
        nodes2D.add(container);
    }

    @Override
    public void drawBackground(ColorRGB color) {
    }

    @Override
    public void drawSprite(Sprite sprite, Transform globalTransform) {
        nodes2D.add(sprite);
    }

    @Override
    public void drawLine(Primitive graphic, Line line, Transform globalTransform) {
        nodes2D.add(graphic);
    }

    @Override
    public void drawSegmentedLine(Primitive graphic, SegmentedLine line, Transform globalTransform) {
        nodes2D.add(graphic);
    }

    @Override
    public void drawRect(Primitive graphic, Rect rect, Transform globalTransform) {
        nodes2D.add(graphic);
    }

    @Override
    public void drawCircle(Primitive graphic, Circle circle, Transform globalTransform) {
        nodes2D.add(graphic);
    }

    @Override
    public void drawPolygon(Primitive graphic, Polygon polygon, Transform globalTransform) {
        nodes2D.add(graphic);
    }

    @Override
    public void drawText(Text text, Transform globalTransform) {
        nodes2D.add(text);
    }

    @Override
    public void visitGroup(Group group, Transform3D globalTransform) {
        nodes3D.add(group);
    }

    @Override
    public void drawMesh(Mesh mesh, Transform3D globalTransform) {
        nodes3D.add(mesh);
    }

    @Override
    public void drawLight(Light light, Transform3D globalTransform) {
        nodes3D.add(light);
    }
}
