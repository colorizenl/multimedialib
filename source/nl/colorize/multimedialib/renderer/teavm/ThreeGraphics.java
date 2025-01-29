//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.annotations.Beta;
import nl.colorize.multimedialib.math.Box;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.SegmentedLine;
import nl.colorize.multimedialib.math.Shape3D;
import nl.colorize.multimedialib.math.Sphere;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.teavm.ThreeBridge.ThreeObject;
import nl.colorize.multimedialib.renderer.teavm.ThreeBridge.ThreeLight;
import nl.colorize.multimedialib.renderer.teavm.ThreeBridge.ThreeVector;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.Group;
import nl.colorize.multimedialib.stage.Light;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.StageNode3D;
import nl.colorize.multimedialib.stage.StageSubscriber;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.stage.Transform;
import nl.colorize.multimedialib.stage.Transform3D;
import nl.colorize.util.Subject;

import java.util.HashMap;
import java.util.Map;

/**
 * Uses the <a href="https://threejs.org">Three.js</a> JavaScript library to
 * render 3D graphics. This requires the browser to support WebGL.
 * <p>
 * Three.js does not support 2D graphics very well, so when using both 2D and
 * 3D graphics, the 2D part is delegated to a separate canvas overlay that is
 * displayed on top of the 3D graphics. Updating the 2D graphics overlay is
 * then delegated to {@link HtmlCanvasGraphics}.
 */
@Beta
public class ThreeGraphics implements TeaGraphics, StageSubscriber {

    private Canvas canvas;
    private TeaMediaLoader mediaLoader;
    private ThreeBridge three;
    private HtmlCanvasGraphics overlay;
    private Map<StageNode3D, ThreeObject> threeObjects;
    private Map<Light, ThreeLight> lights;

    private static final float PI = (float) Math.PI;

    @Override
    public void init(SceneContext context) {
        this.canvas = context.getCanvas();
        this.mediaLoader = (TeaMediaLoader) context.getMediaLoader();
        this.threeObjects = new HashMap<>();
        this.lights = new HashMap<>();

        three = Browser.getThreeBridge();
        three.init();

        overlay = new HtmlCanvasGraphics();
        overlay.init(context);
        overlay.getHtmlCanvas().getStyle().setProperty("position", "absolute");
        overlay.getHtmlCanvas().getStyle().setProperty("left", "0");
        overlay.getHtmlCanvas().getStyle().setProperty("top", "0");
        overlay.getHtmlCanvas().getStyle().setProperty("z-index", "2");

        context.getStage().subscribe(this);
    }

    @Override
    public int getDisplayWidth() {
        return overlay.getDisplayWidth();
    }

    @Override
    public int getDisplayHeight() {
        return overlay.getDisplayHeight();
    }

    @Override
    public void onNodeAdded(Group parent, StageNode3D node) {
        if (node instanceof Light light) {
            ThreeLight threeLight = three.createLight(light.getColor().toHex(), light.getIntensity());
            three.getScene().add(threeLight);
            lights.put(light, threeLight);
        }
    }

    @Override
    public void onNodeRemoved(Group parent, StageNode3D node) {
        ThreeObject threeObject = threeObjects.get(node);
        if (threeObject != null) {
            threeObject.removeFromParent();
        }

        if (node instanceof Light light) {
            ThreeLight threeLight = lights.get(light);
            threeLight.removeFromParent();
            lights.remove(light);
        }
    }

    @Override
    public void prepareStage(Stage stage) {
        Point3D camera = stage.getCameraPosition();
        Point3D focus = stage.getCameraFocus();
        three.moveCamera(camera.x(), camera.y(), camera.z(), focus.x(), focus.y(), focus.z());

        ThreeLight ambientLight = three.getAmbientLight();
        ambientLight.getColor().set(stage.getAmbientLightColor().toHex());

        overlay.prepareStage(stage);
    }

    @Override
    public boolean shouldVisitAllNodes() {
        return true;
    }

    @Override
    public void visitContainer(Container container, Transform globalTransform) {
        if (globalTransform.isVisible()) {
            overlay.visitContainer(container, globalTransform);
        }
    }

    @Override
    public void drawBackground(ColorRGB color) {
        three.changeBackgroundColor(color.toHex());

        // Do not use the background color for the 2D canvas overlay,
        // since we want that to be transparent.
    }

    @Override
    public void drawSprite(Sprite sprite, Transform globalTransform) {
        if (globalTransform.isVisible()) {
            overlay.drawSprite(sprite, globalTransform);
        }
    }

    @Override
    public void drawLine(Primitive graphic, Line line, Transform globalTransform) {
        if (globalTransform.isVisible()) {
            overlay.drawLine(graphic, line, globalTransform);
        }
    }

    @Override
    public void drawSegmentedLine(Primitive graphic, SegmentedLine line, Transform globalTransform) {
        if (globalTransform.isVisible()) {
            overlay.drawSegmentedLine(graphic, line, globalTransform);
        }
    }

    @Override
    public void drawRect(Primitive graphic, Rect rect, Transform globalTransform) {
        if (globalTransform.isVisible()) {
            overlay.drawRect(graphic, rect, globalTransform);
        }
    }

    @Override
    public void drawCircle(Primitive graphic, Circle circle, Transform globalTransform) {
        if (globalTransform.isVisible()) {
            overlay.drawCircle(graphic, circle, globalTransform);
        }
    }

    @Override
    public void drawPolygon(Primitive graphic, Polygon polygon, Transform globalTransform) {
        if (globalTransform.isVisible()) {
            overlay.drawPolygon(graphic, polygon, globalTransform);
        }
    }

    @Override
    public void drawText(Text text, Transform globalTransform) {
        if (globalTransform.isVisible()) {
            overlay.drawText(text, globalTransform);
        }
    }

    @Override
    public void visitGroup(Group group, Transform3D globalTransform) {
    }

    @Override
    public void drawMesh(Mesh mesh, Transform3D globalTransform) {
        ThreeMeshWrapper meshWrapper = (ThreeMeshWrapper) mesh;
        ThreeObject threeObject = meshWrapper.getThreeObject();

        if (threeObject == null) {
            // Object is still being loaded.
            return;
        }

        // This check is intentionally done here, because models
        // can be loaded asynchronously.
        if (!threeObjects.containsKey(meshWrapper)) {
            threeObjects.put(meshWrapper, threeObject);
            three.getScene().add(threeObject);
        }

        syncTransform(threeObject, globalTransform);
    }

    @Override
    public void drawLight(Light light, Transform3D globalTransform) {
        Point3D position = globalTransform.getPosition();

        ThreeLight threeLight = lights.get(light);
        syncTransform(threeLight, globalTransform);
        threeLight.getPosition().set(position.x(), position.y(), position.z());
        threeLight.getColor().set(light.getColor().toHex());
        threeLight.setIntensity(globalTransform.isVisible() ? light.getIntensity() : 0f);
    }

    private void syncTransform(ThreeObject threeObject, Transform3D globalTransform) {
        Point3D position = globalTransform.getPosition();

        threeObject.setVisible(globalTransform.isVisible());
        threeObject.getPosition().set(position.x(), position.y(), position.z());
        threeObject.getRotation().set(
            globalTransform.getRotationX().getRadians(),
            globalTransform.getRotationY().getRadians(),
            globalTransform.getRotationZ().getRadians()
        );
        threeObject.getScale().set(
            globalTransform.getScaleX() / 100f,
            globalTransform.getScaleY() / 100f,
            globalTransform.getScaleZ() / 100f
        );
    }

    @Override
    public Mesh createMesh(Shape3D shape, ColorRGB color) {
        if (shape instanceof Box box) {
            ThreeObject model = three.createBox(box.width(), box.height(), box.depth(),
                color.toHex());
            return new ThreeMeshWrapper(Subject.of(model));
        } else if (shape instanceof Sphere sphere) {
            ThreeObject model = three.createSphere(sphere.radius(), color.toHex());
            return new ThreeMeshWrapper(Subject.of(model));
        } else {
            throw new IllegalArgumentException("Unknown shape: " + shape);
        }
    }

    @Override
    public Point2D project(Point3D position) {
        ThreeVector ndcPosition = three.project(position.x(), position.y(), position.z());
        float canvasX = (ndcPosition.getX() + 1f) / 2f * canvas.getWidth();
        float canvasY = (-ndcPosition.getY() + 1f) / 2f * canvas.getHeight();
        return new Point2D(canvasX, canvasY);
    }

    @Override
    public boolean castPickRay(Point2D canvasPosition, Box area) {
        float ndcX = (canvasPosition.x() / canvas.getWidth()) * 2f - 1f;
        float ndcY = -(canvasPosition.y() / canvas.getHeight()) * 2f + 1f;

        return three.castPickRay(ndcX, ndcY,
            area.x(), area.y(), area.z(), area.getEndX(), area.getEndY(), area.getEndZ());
    }

    @Override
    public void finalize3D(Stage stage) {
        three.render();
    }

    @Override
    public GraphicsMode getGraphicsMode() {
        return GraphicsMode.MODE_3D;
    }
}
