//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import lombok.AllArgsConstructor;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.SegmentedLine;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.util.LogHelper;
import nl.colorize.util.ResourceFile;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import static nl.colorize.multimedialib.stage.Align.CENTER;
import static nl.colorize.multimedialib.stage.ColorRGB.WHITE;

/**
 * Simple demo application that does not use any animation, interactivity, or
 * randomness. Suitable for regression tests that visually compare the
 * renderer output.
 */
@AllArgsConstructor
public class RegressionDemo implements Scene {

    private GraphicsMode graphicsMode;
    private File screenshotFile;

    private static final ResourceFile IMAGE_FILE = new ResourceFile("colorize-emblem-64.png");
    private static final ResourceFile MODEL_FILE = new ResourceFile("demo/crate.vox.obj");
    private static final ColorRGB RED = ColorRGB.parseHex("#e45d61");
    private static final ColorRGB GRAY = ColorRGB.parseHex("#adadad");
    private static final Logger LOGGER = LogHelper.getLogger(RegressionDemo.class);

    @Override
    public void start(SceneContext context) {
        Rect bounds = context.getCanvas().getBounds();

        if (graphicsMode == GraphicsMode.MODE_3D) {
            addPolygonModels(context);
        }

        addBackground(context, bounds);
        addSprites(context);
        addShapes(context, bounds);
    }

    private void addBackground(SceneContext context, Rect bounds) {
        Primitive outline = new Primitive(SegmentedLine.fromOutline(bounds.expand(-20)), WHITE);
        outline.setStroke(2);
        context.getStage().getRoot().addChild(outline);

        FontFace font = FontFace.DEFAULT_FONT.derive(30, RED);
        Text title = new Text("MultimediaLib", font, CENTER);
        title.getTransform().setPosition(bounds.getCenterX(), 100);
        context.getStage().getRoot().addChild(title);

        Text subTitle = new Text(context.getConfig().getRendererName(), font.derive(20, GRAY), CENTER);
        subTitle.getTransform().setPosition(bounds.getCenterX(), 150);
        context.getStage().getRoot().addChild(subTitle);
    }

    private void addSprites(SceneContext context) {
        Sprite normal = new Sprite(context.getMediaLoader().loadImage(IMAGE_FILE));

        Sprite rotated = normal.copy();
        rotated.getTransform().setRotation(90);

        Sprite scaled = normal.copy();
        scaled.getTransform().setScale(50);

        Sprite flipped = normal.copy();
        flipped.getTransform().setFlipHorizontal(true);
        flipped.getTransform().setFlipVertical(true);

        Sprite alpha = normal.copy();
        alpha.getTransform().setAlpha(50);

        Sprite tint = normal.copy();
        tint.getTransform().setMaskColor(WHITE);

        List<Sprite> sprites = List.of(normal, rotated, scaled, flipped, alpha, tint);
        for (int i = 0; i < sprites.size(); i++) {
            sprites.get(i).getTransform().setPosition(70, 200 + i * 70);
            context.getStage().getRoot().addChild(sprites.get(i));
        }
    }

    private void addShapes(SceneContext context, Rect bounds) {
        Primitive rect = new Primitive(Rect.aroundOrigin(64, 64), RED);
        rect.getTransform().setPosition(bounds.getEndX() - 70, 200);
        context.getStage().getRoot().addChild(rect);

        Primitive circle = new Primitive(new Circle(32), RED);
        circle.getTransform().setPosition(bounds.getEndX() - 70, 270);
        context.getStage().getRoot().addChild(circle);

        Primitive polygon = new Primitive(Polygon.createCircle(32, 8), RED, 50);
        polygon.getTransform().setPosition(bounds.getEndX() - 70, 340);
        context.getStage().getRoot().addChild(polygon);
    }

    private void addPolygonModels(SceneContext context) {
        context.getStage().setCameraPosition(new Point3D(0, 6, 8));
        context.getStage().setCameraFocus(Point3D.ORIGIN);

        Mesh crateTemplate = context.getMediaLoader().loadModel(MODEL_FILE);
        Mesh model = crateTemplate.copy();
        model.getTransform().setPosition(new Point3D(0, 1, 0));
        model.getTransform().setScale(60);
        model.getTransform().setRotation(0, 45, 0);
        context.getStage().getRoot3D().addChild(model);
    }

    @Override
    public void update(SceneContext context, double deltaTime) {
        if (screenshotFile != null) {
            context.attachTimer(1.0, () -> {
                context.captureScreenshot(screenshotFile);
                LOGGER.info("Saved screenshot to " + screenshotFile.getAbsolutePath());
                context.terminate();
            });
        }
    }

    @Override
    public boolean shouldRestartOnResize() {
        return true;
    }
}
