//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.math.RandomGenerator;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.ErrorHandler;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.GeometryBuilder;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.Pointer;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.stage.Align;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.PolygonModel;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.stage.World3D;

import java.util.ArrayList;
import java.util.List;

import static nl.colorize.multimedialib.stage.ColorRGB.WHITE;

/**
 * Simple demo application for 3D graphics, that shows a checkerboard floor and
 * a number of models randomly walking around. Using this demo application
 * requires a renderer that supports 3D graphics.
 */
public class Demo3D implements Scene, ErrorHandler {

    private SceneContext context;
    private FontFace font;
    private Image logo;
    private List<PolygonModel> models;
    private List<Point2D> walkVectors;
    private Point2D pointer;
    private Text hud;

    public static final int CANVAS_WIDTH = 800;
    public static final int CANVAS_HEIGHT = 600;

    private static final FilePointer MODEL_FILE = new FilePointer("demo/colorize-logo.gltf");
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
        font = mediaLoader.loadDefaultFont(12, WHITE);
        logo = mediaLoader.loadImage(LOGO_FILE);

        World3D stage = context.getStage().getWorld();
        createFloor(stage);
        createModels(stage, mediaLoader);
        stage.setCameraPosition(new Point3D(0f, 30f, AREA_SIZE * 0.6f));
        stage.setCameraTarget(new Point3D(0f, 0f, 0f));

        hud = new Text("", font);
        hud.setPosition(20, 20);
        hud.setLineHeight(20);
        context.getStage().getRoot().addChild(hud);
    }

    private void createFloor(World3D stage) {
        boolean white = true;

        for (int i = -5; i <= 5; i++) {
            for (int j = -5; j <= 5; j++) {
                float tileSize = AREA_SIZE / 11f;
                ColorRGB color = white ? WHITE : new ColorRGB(50, 50, 50);
                PolygonModel tile = createTile(tileSize, color, i == 0 && j == 0);
                tile.getTransform().setPosition(i * tileSize, 0f, j * tileSize);
                stage.getChildren().add(tile);

                white = !white;
            }
        }
    }

    private PolygonModel createTile(float tileSize, ColorRGB color, boolean center) {
        GeometryBuilder geometryBuilder = context.getMediaLoader().getGeometryBuilder();

        if (center) {
            PolygonModel quad = geometryBuilder.createQuad(new Point2D(tileSize, tileSize), logo);
            quad.getTransform().setRotation(0f, 90f, 0f);
            return quad;
        } else {
            return geometryBuilder.createQuad(new Point2D(tileSize, tileSize), color);
        }
    }

    private void createModels(World3D stage, MediaLoader mediaLoader) {
        models = new ArrayList<>();
        walkVectors = new ArrayList<>();
        PolygonModel template = mediaLoader.loadModel(MODEL_FILE);

        for (int i = 0; i < 100; i++) {
            PolygonModel model = template.copy();
            model.getTransform().setPosition(generateRandomModelPosition());
            model.getTransform().setRotation(0f, -90f, 0f);
            stage.getChildren().add(model);
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
        for (Pointer pointer : context.getInput().getPointers()) {
            if (pointer.isReleased()) {
                this.pointer = pointer.getPosition();
            }
        }

        updateModels(context.getStage());
        updateHUD(context.getStage());
    }

    private void updateModels(Stage stage) {
        Rect areaBounds = new Rect(-AREA_SIZE / 2f, -AREA_SIZE / 2f, AREA_SIZE, AREA_SIZE);

        for (int i = 0; i < models.size(); i++) {
            PolygonModel model = models.get(i);
            Point2D walkVector = walkVectors.get(i);
            model.getTransform().addPosition(walkVector.x(), 0f, walkVector.y());

            Point3D position = model.getTransform().getPosition();

            if (!areaBounds.contains(position.x(), position.z())) {
                Point2D walk = new Point2D(-walkVectors.get(i).x(), -walkVectors.get(i).y());
                walkVectors.set(i, walk);

                model.playAnimation("Cube|Spin");
            }
        }
    }

    private void updateHUD(Stage stage) {
        List<String> info = context.getDebugInformation();
        info.add("Models:  " + models.size());
        info.add("Pointer:  " + pointer);
        hud.setText(info);
    }

    @Override
    public void onError(SceneContext context, Exception cause) {
        Text errorText = new Text("Error:\n\n" + cause.getMessage(), font, Align.CENTER);
        errorText.getTransform().setPosition(context.getCanvas().getCenter());
        context.getStage().getRoot().addChild(errorText);
    }
}
