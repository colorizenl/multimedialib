//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.GraphicsLayer2D;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.PolygonModel;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.math.RandomGenerator;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.GeometryBuilder;
import nl.colorize.multimedialib.renderer.GraphicsContext2D;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple demo application for 3D graphics, that shows a checkerboard floor and
 * a number of models randomly walking around. Using this demo application
 * requires a renderer that supports 3D graphics.
 */
public class Demo3D implements Scene, GraphicsLayer2D {

    private SceneContext context;
    private TTFont font;
    private Image logo;
    private List<PolygonModel> models;
    private List<Point2D> walkVectors;
    private Point2D pointer;

    public static final int CANVAS_WIDTH = 800;
    public static final int CANVAS_HEIGHT = 600;

    private static final FilePointer MODEL_FILE = new FilePointer("colorize-logo.gltf");
    private static final FilePointer LOGO_FILE = new FilePointer("colorize-logo.png");
    private static final float AREA_SIZE = 50f;
    private static final float MAX_WALK_SPEED = 0.1f;

    public Demo3D() {
        this.pointer = new Point2D(0f, 0f);
    }

    @Override
    public void start(SceneContext context) {
        this.context = context;
        MediaLoader mediaLoader = context.getMediaLoader();
        font = mediaLoader.loadDefaultFont(12, ColorRGB.WHITE);
        logo = mediaLoader.loadImage(LOGO_FILE);

        Stage stage = context.getStage();
        createFloor(stage);
        createModels(stage, mediaLoader);
        stage.moveCamera(new Point3D(0f, 30f, AREA_SIZE * 0.6f), new Point3D(0f, 0f, 0f));
    }

    private void createFloor(Stage stage) {
        boolean white = true;

        for (int i = -5; i <= 5; i++) {
            for (int j = -5; j <= 5; j++) {
                float tileSize = AREA_SIZE / 11f;
                ColorRGB color = white ? ColorRGB.WHITE : new ColorRGB(50, 50, 50);
                PolygonModel tile = createTile(stage, tileSize, color, i == 0 && j == 0);
                tile.getTransform().setPosition(i * tileSize, 0f, j * tileSize);
                stage.add(tile);

                white = !white;
            }
        }
    }

    private PolygonModel createTile(Stage stage, float tileSize, ColorRGB color, boolean center) {
        GeometryBuilder geometryBuilder = context.getMediaLoader().getGeometryBuilder();

        if (center) {
            PolygonModel quad = geometryBuilder.createQuad(new Point2D(tileSize, tileSize), logo);
            quad.getTransform().setRotation(0f, 1f, 0f, 90f);
            return quad;
        } else {
            return geometryBuilder.createQuad(new Point2D(tileSize, tileSize), color);
        }
    }

    private void createModels(Stage stage, MediaLoader mediaLoader) {
        models = new ArrayList<>();
        walkVectors = new ArrayList<>();
        PolygonModel template = mediaLoader.loadModel(MODEL_FILE);

        for (int i = 0; i < 100; i++) {
            PolygonModel model = template.copy();
            model.getTransform().setPosition(generateRandomModelPosition());
            model.getTransform().setRotation(0f, 1f, 0f, -90f);
            stage.add(model);
            models.add(model);
            walkVectors.add(generateRandomWalkVector());
        }
    }

    private Point3D generateRandomModelPosition() {
        float x = RandomGenerator.getFloat(-AREA_SIZE / 2f, AREA_SIZE / 2f);
        float z = RandomGenerator.getFloat(-AREA_SIZE / 2f, AREA_SIZE / 2f);
        return new Point3D(x, 0.5f, z);
    }

    private Point2D generateRandomWalkVector() {
        float x = RandomGenerator.getFloat(-MAX_WALK_SPEED, MAX_WALK_SPEED);
        float z = RandomGenerator.getFloat(-MAX_WALK_SPEED, MAX_WALK_SPEED);
        return new Point2D(x, z);
    }

    @Override
    public void update(SceneContext context, float deltaTime) {
        InputDevice inputDevice = context.getInputDevice();

        if (inputDevice.isPointerReleased(context.getCanvas().getBounds())) {
            pointer = inputDevice.getPointers().get(0);
        }

        updateModels(context.getStage());
    }

    private void updateModels(Stage stage) {
        Rect areaBounds = new Rect(-AREA_SIZE / 2f, -AREA_SIZE / 2f, AREA_SIZE, AREA_SIZE);

        for (int i = 0; i < models.size(); i++) {
            PolygonModel model = models.get(i);
            Point2D walkVector = walkVectors.get(i);
            Point3D position = model.getTransform().getPosition();
            position.add(walkVector.getX(), 0f, walkVector.getY());

            if (!areaBounds.contains(position.getX(), position.getZ())) {
                Point2D walk = new Point2D(-walkVectors.get(i).getX(), -walkVectors.get(i).getY());
                walkVectors.set(i, walk);

                if (model.getAnimations().containsKey("Cube|Spin")) {
                    model.playAnimation("Cube|Spin", false);
                }
            }
        }
    }

    @Override
    public void render(GraphicsContext2D graphics) {
        Canvas canvas = graphics.getCanvas();

        graphics.drawText("Canvas:  " + canvas, font, 20, 20);
        graphics.drawText("Framerate:  " + Math.round(context.getAverageFramerate()), font, 20, 40);
        graphics.drawText("Frame time:  " + Math.round(context.getAverageFrameTime()) + "ms",
            font, 20, 60);
        graphics.drawText("Models:  " + models.size(), font, 20, 80);
        graphics.drawText("Pointer:  " + pointer, font, 20, 100);
    }
}
