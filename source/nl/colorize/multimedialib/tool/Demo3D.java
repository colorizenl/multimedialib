//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.PolygonMesh;
import nl.colorize.multimedialib.graphics.PolygonModel;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.math.RandomGenerator;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.GraphicsContext2D;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.Stage;
import nl.colorize.multimedialib.scene.Application;
import nl.colorize.multimedialib.scene.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple demo application for 3D graphics, that shows a checkerboard floor and
 * a number of models randomly walking around. Using this demo application
 * requires a renderer that supports 3D graphics.
 */
public class Demo3D implements Scene {

    private TTFont font;
    private Image logo;
    private List<PolygonModel> models;
    private List<Point2D> walkVectors;
    private Point2D pointer;

    public static final int CANVAS_WIDTH = 800;
    public static final int CANVAS_HEIGHT = 600;

    private static final FilePointer MODEL_FILE = new FilePointer("model.fbx");
    private static final FilePointer LOGO_FILE = new FilePointer("colorize-logo.png");
    private static final float AREA_SIZE = 50f;
    private static final float MAX_WALK_SPEED = 0.1f;

    public Demo3D() {
        this.pointer = new Point2D(0f, 0f);
    }

    @Override
    public void start(Application app) {
        MediaLoader mediaLoader = app.getMediaLoader();
        font = mediaLoader.loadDefaultFont(12, ColorRGB.WHITE);
        logo = mediaLoader.loadImage(LOGO_FILE);

        createFloor(app.getStage());
        createModels(app.getStage(), mediaLoader);

        Stage stage = app.getStage();
        stage.moveCamera(new Point3D(0f, 30f, AREA_SIZE * 0.6f), new Point3D(0f, 0f, 0f));
    }

    private void createFloor(Stage stage) {
        boolean white = true;

        for (int i = -5; i <= 5; i++) {
            for (int j = -5; j <= 5; j++) {
                float tileSize = AREA_SIZE / 11f;
                ColorRGB color = white ? ColorRGB.WHITE : new ColorRGB(50, 50, 50);
                PolygonModel tile = createTile(stage, tileSize, color, i == 0 && j == 0);
                tile.setPosition(i * tileSize, 0f, j * tileSize);
                stage.add(tile);

                white = !white;
            }
        }
    }

    private PolygonModel createTile(Stage stage, float tileSize, ColorRGB color, boolean center) {
        if (center) {
            PolygonMesh tileMesh = stage.createQuad(new Point2D(tileSize, tileSize), logo);
            PolygonModel model = tileMesh.createModel();
            model.setRotation(0f, 1f, 0f, 90f);
            return model;
        } else {
            PolygonMesh tileMesh = stage.createQuad(new Point2D(tileSize, tileSize), color);
            return tileMesh.createModel();
        }
    }

    private void createModels(Stage stage, MediaLoader mediaLoader) {
        models = new ArrayList<>();
        walkVectors = new ArrayList<>();
        PolygonMesh mesh = mediaLoader.loadMesh(MODEL_FILE);

        for (int i = 0; i < 100; i++) {
            PolygonModel model = mesh.createModel();
            model.setPosition(generateRandomModelPosition());
            model.setRotation(0f, 1f, 0f, -90f);
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
    public void update(Application app, float deltaTime) {
        InputDevice inputDevice = app.getInputDevice();
        if (inputDevice.isPointerReleased(app.getCanvas().getBounds())) {
            pointer = inputDevice.getPointers().get(0);
        }

        updateModels(app.getStage());
    }

    private void updateModels(Stage stage) {
        Rect areaBounds = new Rect(-AREA_SIZE / 2f, -AREA_SIZE / 2f, AREA_SIZE, AREA_SIZE);

        for (int i = 0; i < models.size(); i++) {
            PolygonModel model = models.get(i);
            Point2D walkVector = walkVectors.get(i);
            Point3D position = model.getPosition();
            position.add(walkVector.getX(), 0f, walkVector.getY());

            if (!areaBounds.contains(position.getX(), position.getZ())) {
                Point2D walk = new Point2D(-walkVectors.get(i).getX(), -walkVectors.get(i).getY());
                walkVectors.set(i, walk);
                stage.playAnimation(model, model.getAnimation("Cube|Spin"), false);
            }
        }
    }

    @Override
    public void render(Application app, GraphicsContext2D graphics) {
        Canvas canvas = graphics.getCanvas();

        graphics.drawText("Canvas:  " + canvas, font, 20, 20);
        graphics.drawText("Framerate:  " + Math.round(app.getAverageFramerate()), font, 20, 40);
        graphics.drawText("Frame time:  " + Math.round(app.getAverageFrameTime()) + "ms",
            font, 20, 60);
        graphics.drawText("Models:  " + models.size(), font, 20, 80);
        graphics.drawText("Pointer:  " + pointer, font, 20, 100);
    }
}
