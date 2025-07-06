//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import nl.colorize.multimedialib.math.Coordinate;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;
import nl.colorize.multimedialib.renderer.Pointer;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.Timer;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.stage.Transform;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Demo application that displays isometric "pseudo-3D" or "2.5D" graphics
 * using the 2D renderer.
 */
public class DemoIsometric implements Scene {

    private Container world;
    private Container camera;
    private Map<Coordinate, Container> blocks;
    private Text hud;

    private static final int BLOCK_SIZE = 70;
    private static final ColorRGB RED_BLOCK = new ColorRGB(228, 93, 97);
    private static final ColorRGB LIGHT_BLOCK = new ColorRGB(235, 235, 235);
    private static final ColorRGB HIGHLIGHT = ColorRGB.parseHex("#adadad");
    private static final int DARK_LEFT = -30;
    private static final int DARK_RIGHT = -60;
    private static final float MOVE = 50f;
    private static final float ZOOM = 20f;
    private static final float SINE_WAVE_SPEED = 2f;
    private static final float SINE_WAVE_AMPLITUDE = 10f;

    @Override
    public void start(SceneContext context) {
        initWorld(context);
        initHUD(context);
    }

    private void initWorld(SceneContext context) {
        world = context.getStage().addContainer("world");
        camera = world.addChildContainer();
        blocks = new LinkedHashMap<>();

        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                Container block = createBlock(x, y);
                camera.addChild(block);
                blocks.put(new Coordinate(x, y), block);
            }
        }
    }

    private Container createBlock(int x, int y) {
        ColorRGB color = (Math.abs(x) % 2) == (Math.abs(y) % 2) ? RED_BLOCK : LIGHT_BLOCK;

        Polygon top = Polygon.fromPoints(
            0f, -BLOCK_SIZE / 4f,
            BLOCK_SIZE / 2f, 0f,
            0f, BLOCK_SIZE / 4f,
            -BLOCK_SIZE / 2f, 0f
        );

        Polygon left = Polygon.fromPoints(
            -BLOCK_SIZE / 2f, 0f,
            0f, BLOCK_SIZE / 4f,
            0f, BLOCK_SIZE * 0.75f,
            -BLOCK_SIZE / 2f, BLOCK_SIZE * 0.5f
        );

        Polygon right = Polygon.fromPoints(
            0f, BLOCK_SIZE / 4f,
            BLOCK_SIZE / 2f, 0f,
            BLOCK_SIZE / 2f, BLOCK_SIZE * 0.5f,
            0f, BLOCK_SIZE * 0.75f
        );

        Container block = new Container();
        block.addChild(new Primitive(top, color));
        block.addChild(new Primitive(left, color.alter(DARK_LEFT, DARK_LEFT, DARK_LEFT)));
        block.addChild(new Primitive(right, color.alter(DARK_RIGHT, DARK_RIGHT, DARK_RIGHT)));
        block.getTransform().setX(x * 0.5f * BLOCK_SIZE + y * -0.5f * BLOCK_SIZE);
        block.getTransform().setY(x * 0.25f * BLOCK_SIZE + y * 0.25f * BLOCK_SIZE);
        return block;
    }

    private void initHUD(SceneContext context) {
        FontFace hudFont = context.getMediaLoader().loadDefaultFont(12, ColorRGB.WHITE);

        hud = new Text("", hudFont);
        hud.getTransform().setPosition(20, 30);
        hud.setLineHeight(20);

        Container hudContainer = context.getStage().addContainer("hud");
        hudContainer.addChild(hud);
    }

    @Override
    public void update(SceneContext context, float deltaTime) {
        world.getTransform().setPosition(context.getCanvas().getCenter());
        hud.setText(context.getDebugInformation());
        updateCameraControls(context);
        updatePointerControls(context.getInput());
    }

    private void updateCameraControls(SceneContext context) {
        InputDevice input = context.getInput();
        Transform cameraTransform = camera.getTransform();

        if (input.isKeyReleased(KeyCode.LEFT)) {
            cameraTransform.addPosition(MOVE, 0f);
        } else if (input.isKeyReleased(KeyCode.RIGHT)) {
            cameraTransform.addPosition(-MOVE, 0f);
        } else if (input.isKeyReleased(KeyCode.UP)) {
            cameraTransform.addPosition(0f, MOVE);
        } else if (input.isKeyReleased(KeyCode.DOWN)) {
            cameraTransform.addPosition(0f, -MOVE);
        } else if (input.isKeyReleased(KeyCode.MINUS)) {
            cameraTransform.setScale(Math.max(cameraTransform.getScaleX() - ZOOM, 20f));
        } else if (input.isKeyReleased(KeyCode.PLUS)) {
            cameraTransform.setScale(cameraTransform.getScaleX() + ZOOM);
        } else if (input.isKeyReleased(KeyCode.SPACEBAR)) {
            startSineWave(context, cameraTransform);
        }
    }

    private void updatePointerControls(InputDevice input) {
        Point2D cameraWorldPosition = camera.getGlobalTransform().getPosition();

        for (Pointer pointer : input.getPointers()) {
            Point2D worldPosition = pointer.getPosition().move(cameraWorldPosition.negate());
            Coordinate highlighted = toBlockCoordinate(worldPosition);

            for (Map.Entry<Coordinate, Container> entry : blocks.entrySet()) {
                ColorRGB mask = entry.getKey().equals(highlighted) ? HIGHLIGHT : null;
                entry.getValue().getTransform().setMaskColor(mask);
            }
        }
    }

    private Coordinate toBlockCoordinate(Point2D position) {
        float a = 0.5f * BLOCK_SIZE;
        float b = -0.5f * BLOCK_SIZE;
        float c = 0.25f * BLOCK_SIZE;
        float d = 0.25f * BLOCK_SIZE;

        float det = (1f / (a * d - b * c));
        float[] inv = {det * d, det * -b, det * -c, det * a};

        return new Coordinate(
            Math.round(position.x() * inv[0] + position.y() * inv[1]),
            Math.round(position.x() * inv[2] + position.y() * inv[3])
        );
    }

    private void startSineWave(SceneContext context, Transform cameraTransform) {
        Timer waveTimer = Timer.infinite();

        Map<Container, Float> orginalBlockY = new HashMap<>();
        for (Container block : blocks.values()) {
            orginalBlockY.put(block, block.getTransform().getY());
        }

        context.attach(deltaTime -> {
            waveTimer.update(deltaTime * SINE_WAVE_SPEED);

            for (Map.Entry<Coordinate, Container> entry : blocks.entrySet()) {
                float offset = entry.getKey().x();
                float value = SINE_WAVE_AMPLITUDE * (float) Math.sin(waveTimer.getTime() - offset);
                Container block = entry.getValue();
                block.getTransform().setY(orginalBlockY.get(block) + value);
            }
        });
    }
}
