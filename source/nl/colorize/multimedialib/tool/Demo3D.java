//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import nl.colorize.multimedialib.math.Box;
import nl.colorize.multimedialib.math.Coordinate;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.math.RandomGenerator;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.renderer.ErrorHandler;
import nl.colorize.multimedialib.renderer.KeyCode;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.Pointer;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.effect.Effect;
import nl.colorize.multimedialib.stage.Align;
import nl.colorize.multimedialib.stage.Animation;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.Light;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.SpriteAtlas;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.stage.Transform3D;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.animation.Interpolation;
import nl.colorize.util.animation.Timeline;
import nl.colorize.util.stats.Tuple;
import nl.colorize.util.stats.TupleList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.colorize.multimedialib.math.Point3D.EPSILON;
import static nl.colorize.multimedialib.stage.ColorRGB.WHITE;
import static nl.colorize.multimedialib.tool.Demo2D.BLUE_BUTTON;
import static nl.colorize.multimedialib.tool.Demo2D.BUTTON_HEIGHT;
import static nl.colorize.multimedialib.tool.Demo2D.BUTTON_WIDTH;
import static nl.colorize.multimedialib.tool.Demo2D.GREEN_BUTTON;
import static nl.colorize.multimedialib.tool.Demo2D.MARIO_SPRITES_FILE;
import static nl.colorize.multimedialib.tool.Demo2D.ORANGE_BUTTON;
import static nl.colorize.multimedialib.tool.Demo2D.PINK_BUTTON;
import static nl.colorize.multimedialib.tool.Demo2D.RED_BUTTON;

/**
 * Simple demo application for 3D graphics that shows a checkerboard floor and
 * a number of models randomly walking around.
 */
public class Demo3D implements Scene, ErrorHandler {

    private SceneContext context;
    private FontFace font;
    private Map<Coordinate, Mesh> tiles;
    private List<Tuple<Mesh, String>> walkingModels;
    private List<Mesh> spinningModels;
    private Point2D pointerPosition;
    private Container hudContainer;

    private static final ResourceFile CRATE_MODEL_FILE = new ResourceFile("demo/crate.vox.obj");
    private static final ResourceFile LOGO_FILE = new ResourceFile("colorize-logo.png");
    private static final ColorRGB BLACK_TILE_COLOR = new ColorRGB(50, 50, 50);
    private static final int GRID_SIZE = 5;
    private static final int NUM_MODELS = 50;
    private static final float WALK_SPEED = 0.02f;
    private static final float SPIN_SPEED = 30f;
    private static final List<String> DIRECTIONS = List.of("north", "east", "south", "west");

    @Override
    public void start(SceneContext context) {
        this.context = context;
        this.pointerPosition = Point2D.ORIGIN;

        MediaLoader mediaLoader = context.getMediaLoader();
        font = mediaLoader.loadDefaultFont(12, WHITE);

        createHUD();
        createFloor();
        createWalkingModels();
        createSpinningModels();

        context.getStage().setCameraPosition(new Point3D(0f, 6f, 8f));
        context.getStage().setCameraFocus(Point3D.ORIGIN);
    }

    private void createFloor() {
        tiles = new HashMap<>();
        boolean white = false;
        Image logo = context.getMediaLoader().loadImage(LOGO_FILE);

        for (int i = -GRID_SIZE; i <= GRID_SIZE; i++) {
            for (int j = -GRID_SIZE; j <= GRID_SIZE; j++) {
                ColorRGB color = white ? WHITE : BLACK_TILE_COLOR;
                Mesh tile = context.createMesh(Box.around(Point3D.ORIGIN, 1f, EPSILON, 1f), color);
                if (i == 0 && j == 0) {
                    tile.applyTexture(logo);
                }
                tile.getTransform().setPosition(i, 0f, j);
                context.getStage().getRoot3D().addChild(tile);
                tiles.put(new Coordinate(i, j), tile);

                white = !white;
            }
        }
    }

    private void createWalkingModels() {
        walkingModels = new TupleList<>();
        Sprite sprite = createMarioSprite();

        for (int i = 0; i < NUM_MODELS; i++) {
            Mesh mesh = context.createMesh(Box.around(Point3D.ORIGIN, 0.96f, 1.28f, EPSILON), WHITE);
            mesh.getTransform().setPosition(generateRandomModelPosition());
            mesh.applyDynamicTexture(sprite.copy());
            context.getStage().getRoot3D().addChild(mesh);

            String direction = RandomGenerator.pick(DIRECTIONS);
            mesh.getDynamicTexture().changeGraphics(direction);
            walkingModels.add(Tuple.of(mesh, direction));
        }
    }

    private Sprite createMarioSprite() {
        Image image = context.getMediaLoader().loadImage(MARIO_SPRITES_FILE);
        SpriteAtlas atlas = new SpriteAtlas();
        Sprite marioSprite = new Sprite();

        int y = 0;
        for (String direction : DIRECTIONS) {
            for (int i = 0; i <= 4; i++) {
                atlas.add(direction + "_" + i, image, new Region(i * 48, y, 48, 64));
            }
            y += 64;
        }

        for (String direction : DIRECTIONS) {
            List<Image> frames = atlas.get(List.of(
                direction + "_0",
                direction + "_1",
                direction + "_2",
                direction + "_3",
                direction + "_4"
            ));

            marioSprite.addGraphics(direction, new Animation(frames, 0.1f, true));
        }

        return marioSprite;
    }

    private Point3D generateRandomModelPosition() {
        float x = RandomGenerator.getFloat(-GRID_SIZE / 2f, GRID_SIZE / 2f);
        float z = RandomGenerator.getFloat(-GRID_SIZE / 2f, GRID_SIZE / 2f);
        return new Point3D(x, 0.64f, z);
    }

    private void createSpinningModels() {
        spinningModels = new ArrayList<>();
        Mesh crateTemplate = context.getMediaLoader().loadModel(CRATE_MODEL_FILE);

        List<Point3D> positions = List.of(
            new Point3D(-GRID_SIZE, 1, -GRID_SIZE),
            new Point3D(GRID_SIZE, 1, -GRID_SIZE),
            new Point3D(-GRID_SIZE, 1, GRID_SIZE),
            new Point3D(GRID_SIZE, 1, GRID_SIZE)
        );

        for (Point3D position : positions) {
            Mesh model = crateTemplate.copy();
            model.getTransform().setPosition(position);
            model.getTransform().setScale(30f);
            context.getStage().getRoot3D().addChild(model);
            spinningModels.add(model);
            attachSpinningModelLabel(model);
        }
    }

    private void attachSpinningModelLabel(Mesh model) {
        Text label = new Text("", font, Align.CENTER);
        hudContainer.addChild(label);

        context.attach(Effect.forFrameHandler(() -> {
            Transform3D transform = model.getGlobalTransform();
            Point3D worldPosition = transform.getPosition();
            Point2D canvasPosition = context.project(worldPosition);
            String rotation = transform.getRotationX() + " " + transform.getRotationY() +
                " " + transform.getRotationZ();
            label.setText("3D: " + worldPosition + "\n2D: " + canvasPosition + "\n" + rotation);
            label.getTransform().setPosition(canvasPosition.move(0, -20));
        }));
    }

    private void createHUD() {
        Text hudText = new Text("", font);
        hudText.getTransform().setPosition(20, 30);
        hudText.setLineHeight(20);
        Effect.forFrameHandler(() -> updateHUD(hudText)).attach(context);

        hudContainer = new Container("hud");
        hudContainer.addChild(hudText);
        context.getStage().getRoot().addChild(hudContainer);

        createButton("Camera up", ORANGE_BUTTON, 0, () -> moveCamera(0, 1, 0));
        createButton("Camera down", ORANGE_BUTTON, 1, () -> moveCamera(0, -1, 0));
        createButton("Camera back", ORANGE_BUTTON, 2, () -> moveCamera(0, 0, 1));
        createButton("Camera foward", ORANGE_BUTTON, 3, () -> moveCamera(0, 0, -1));
        createButton("Camera left", ORANGE_BUTTON, 4, () -> moveCamera(-1, 0, 0));
        createButton("Camera right", ORANGE_BUTTON, 5, () -> moveCamera(1, 0, 0));
        createButton("Ambient up", BLUE_BUTTON, 6, () -> changeAmbientLight(1));
        createButton("Ambient down", BLUE_BUTTON, 7, () -> changeAmbientLight(-1));
        createButton("Add light", BLUE_BUTTON, 8, this::addLight);
    }

    private void updateHUD(Text hudText) {
        List<String> info = context.getDebugInformation();
        info.add("Pointer:  " + pointerPosition);
        hudText.setText(info);
    }

    private void createButton(String label, ColorRGB color, int yStep, Runnable click) {
        Primitive bounds = new Primitive(new Rect(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT), color);
        bounds.getTransform().setPosition(-BUTTON_WIDTH / 2f - 2, 2);

        Text text = new Text(label, font, Align.CENTER);
        text.getTransform().setY(19);

        Container button = new Container();
        button.addChild(bounds);
        button.addChild(text);
        hudContainer.addChild(button);

        // Need to keep the button in position for when the canvas
        // is resized.
        context.attach(deltaTime -> {
            int buttonX = context.getCanvas().getWidth() - BUTTON_WIDTH / 2;
            button.getTransform().setPosition(buttonX, yStep * 30f);
        });

        Effect.forClickHandler(bounds, click).attach(context);
    }

    private void moveCamera(int stepX, int stepY, int stepZ) {
        Point3D oldCameraPosition = context.getStage().getCameraPosition();
        Point3D newCameraPosition = oldCameraPosition.move(stepX, stepY, stepZ);
        context.getStage().setCameraPosition(newCameraPosition);
    }

    private void changeAmbientLight(int step) {
        ColorRGB oldAmbient = context.getStage().getAmbientLightColor();
        ColorRGB newAmbient = oldAmbient.alter(step * 40, step * 40, step * 40);
        context.getStage().setAmbientLightColor(newAmbient);
    }

    private void addLight() {
        List<ColorRGB> colors = List.of(GREEN_BUTTON, ORANGE_BUTTON, PINK_BUTTON, BLUE_BUTTON);
        ColorRGB color = RandomGenerator.pick(colors);

        int x = RandomGenerator.getInt(-GRID_SIZE, GRID_SIZE);
        int z = RandomGenerator.getInt(-GRID_SIZE, GRID_SIZE);

        Light light = new Light(color, 50f);
        light.getTransform().setPosition(x, 5, z);
        context.getStage().getRoot3D().addChild(light);
    }

    @Override
    public void update(SceneContext context, float deltaTime) {
        updatePointerControls();
        updateKeyboardControls();

        walkingModels = walkingModels.stream()
            .map(this::updateWalkingModel)
            .toList();

        spinningModels.get(0).getTransform().addRotation(deltaTime * SPIN_SPEED, 0, 0);
        spinningModels.get(1).getTransform().addRotation(0, deltaTime * SPIN_SPEED, 0);
        spinningModels.get(3).getTransform().addRotation(0, 0, deltaTime * SPIN_SPEED);
    }

    private void updatePointerControls() {
        for (Pointer pointer : context.getInput().getPointers()) {
            if (pointer.isReleased()) {
                pointerPosition = pointer.getPosition();
                checkPointerIntersection();
            }
        }
    }

    private void checkPointerIntersection() {
        for (int i = -GRID_SIZE; i <= GRID_SIZE; i++) {
            for (int j = -GRID_SIZE; j <= GRID_SIZE; j++) {
                Box tileBounds = Box.around(new Point3D(i, 0, j), 1, EPSILON, 1);
                Mesh tileModel = tiles.get(new Coordinate(i, j));

                if (context.castPickRay(pointerPosition, tileBounds)) {
                    tileModel.applyColor(RED_BUTTON);
                }
            }
        }
    }

    private void updateKeyboardControls() {
        if (context.getInput().isKeyReleased(KeyCode.N7)) {
            manuallySpin(90, 0, 0);
        } else if (context.getInput().isKeyReleased(KeyCode.N8)) {
            manuallySpin(0, 90, 0);
        } if (context.getInput().isKeyReleased(KeyCode.N9)) {
            manuallySpin(0, 0, 90);
        }
    }

    private void manuallySpin(float deltaX, float deltaY, float deltaZ) {
        Transform3D transform = spinningModels.get(2).getTransform();
        float startX = transform.getRotationX().degrees();
        float startY = transform.getRotationY().degrees();
        float startZ = transform.getRotationZ().degrees();
        float endX = startX + deltaX;
        float endY = startY + deltaY;
        float endZ = startZ + deltaZ;

        Timeline timeline = new Timeline(Interpolation.EASE);
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(0.4f, 1f);

        context.attach(Effect.forTimeline(timeline, delta -> {
            transform.setRotation(
                startX + delta * deltaX,
                startY + delta * deltaY,
                startZ + delta * deltaZ
            );
        }));
    }

    private Tuple<Mesh, String> updateWalkingModel(Tuple<Mesh, String> entry) {
        Mesh model = entry.left();
        String direction = entry.right();
        Point2D walkVector = getWalkVector(direction);
        Rect areaBounds = new Rect(-GRID_SIZE, -GRID_SIZE, GRID_SIZE * 2, GRID_SIZE * 2);

        model.getTransform().addPosition(walkVector.x(), 0f, walkVector.y());
        Point3D position = model.getTransform().getPosition();

        if (!areaBounds.contains(position.x(), position.z())) {
            direction = DIRECTIONS.get((DIRECTIONS.indexOf(direction) + 2) % 4);
            model.getDynamicTexture().changeGraphics(direction);
        }

        return Tuple.of(model, direction);
    }

    private Point2D getWalkVector(String direction) {
        return switch (direction) {
            case "north" -> new Point2D(0, -WALK_SPEED);
            case "east" -> new Point2D(WALK_SPEED, 0);
            case "south" -> new Point2D(0, WALK_SPEED);
            case "west" -> new Point2D(-WALK_SPEED, 0);
            default -> throw new AssertionError();
        };
    }

    @Override
    public void onError(SceneContext context, Exception cause) {
        Text errorText = new Text("Error:\n\n" + cause.getMessage(), font, Align.CENTER);
        errorText.getTransform().setPosition(context.getCanvas().getCenter());
        context.getStage().getRoot().addChild(errorText);
    }
}
