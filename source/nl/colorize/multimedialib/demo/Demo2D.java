//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.demo;

import com.google.common.collect.ImmutableList;
import com.google.common.net.HttpHeaders;
import nl.colorize.multimedialib.graphics.Align;
import nl.colorize.multimedialib.graphics.Animation;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.OutlineFont;
import nl.colorize.multimedialib.graphics.Primitive;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.SpriteSheet;
import nl.colorize.multimedialib.graphics.Text;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.RandomGenerator;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.NetworkAccess;
import nl.colorize.multimedialib.scene.DisplayObject;
import nl.colorize.multimedialib.scene.Layer;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.Stage;
import nl.colorize.multimedialib.scene.Updatable;
import nl.colorize.multimedialib.scene.effect.Effect;
import nl.colorize.multimedialib.scene.effect.TransitionEffect;
import nl.colorize.util.Callback;
import nl.colorize.util.LogHelper;
import nl.colorize.util.animation.Interpolation;
import nl.colorize.util.animation.Timeline;
import nl.colorize.util.http.Headers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Simple demo application that displays a number of animated Mario sprites on
 * top of a black background.
 * <p>
 * application when using the framework to implement an application. Second, it
 * can be used for verification purposes to determine if a new platform is fully
 * supported.
 * <p>
 * The demo application can be started from the command line using the
 * {@link DemoLauncher}. It can also be embedded in applications by creating an
 * instance of this class from the application code.
 */
public class Demo2D implements Scene {

    private SceneContext context;

    private SpriteSheet marioSpriteSheet;
    private OutlineFont font;
    private List<Mario> marios;
    private Effect colorizeLogo;
    private Audio audioClip;
    private Text hud;

    public static final int DEFAULT_CANVAS_WIDTH = 800;
    public static final int DEFAULT_CANVAS_HEIGHT = 600;
    public static final int DEFAULT_FRAMERATE = 60;

    private static final FilePointer MARIO_SPRITES_FILE = new FilePointer("mario.png");
    private static final FilePointer AUDIO_FILE = new FilePointer("test.mp3");
    private static final FilePointer COLORIZE_LOGO = new FilePointer("colorize-logo.png");
    private static final int INITIAL_MARIOS = 20;
    private static final List<String> DIRECTIONS = ImmutableList.of("north", "east", "south", "west");
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 25;
    private static final ColorRGB RED_BUTTON = new ColorRGB(228, 93, 97);
    private static final ColorRGB GREEN_BUTTON = ColorRGB.parseHex("#72A725");
    private static final ColorRGB BACKGROUND_COLOR = ColorRGB.parseHex("#343434");
    private static final Transform MASK_TRANSFORM = Transform.withMask(ColorRGB.WHITE);
    private static final String TEST_URL = "http://www.colorize.nl";
    private static final ColorRGB COLORIZE_COLOR = ColorRGB.parseHex("#e45d61");
    private static final Logger LOGGER = LogHelper.getLogger(Demo2D.class);

    @Override
    public void start(SceneContext context) {
        this.context = context;
        MediaLoader mediaLoader = context.getMediaLoader();

        context.getStage().setBackgroundColor(BACKGROUND_COLOR);

        initMarioSprites(mediaLoader);
        marios = new ArrayList<>();
        addMarios(context.getStage(), INITIAL_MARIOS);

        font = mediaLoader.loadDefaultFont(12, ColorRGB.WHITE);
        audioClip = mediaLoader.loadAudio(AUDIO_FILE);

        initHUD(context);
        initEffects();
    }

    private void initMarioSprites(MediaLoader mediaLoader) {
        Image image = mediaLoader.loadImage(MARIO_SPRITES_FILE);
        marioSpriteSheet = new SpriteSheet(image);

        int y = 0;
        for (String direction : ImmutableList.of("north", "east", "south", "west")) {
            for (int i = 0; i <= 4; i++) {
                marioSpriteSheet.markRegion(direction + "_" + i, new Rect(i * 48, y, 48, 64));
            }
            y += 64;
        }
    }

    private void initHUD(SceneContext context) {
        Stage stage = context.getStage();
        Layer hudLayer = stage.addLayer("hud");

        hud = new Text("", font);
        hud.getPosition().set(20, 20);
        hud.setLineHeight(20);
        stage.add("hud", hud);

        createButton(context, "Add sprites", RED_BUTTON, 0, () -> addMarios(stage, 10));
        createButton(context, "Remove sprites", RED_BUTTON, 30, () -> removeMarios(10));
        createButton(context, "Play sound", GREEN_BUTTON, 60, audioClip::play);
        createButton(context, "Background", GREEN_BUTTON, 90, () -> toggleBackgroundColor(stage));

        Polygon hexagon = new Polygon(80, 70, 120, 70, 135, 100, 120, 130, 80, 130, 65, 100);
        Primitive hexagonPrimitive = Primitive.of(hexagon, COLORIZE_COLOR);
        hexagonPrimitive.setAlpha(50f);

        new DisplayObject()
            .withGraphics(hexagonPrimitive)
            .withLayer(hudLayer)
            .withFrameHandler(deltaTime -> {
                hexagonPrimitive.setPosition(50f, stage.getCanvas().getHeight() - 150f);
            })
            .attachTo(context);
    }

    private void createButton(SceneContext context, String label, ColorRGB color, int y, Runnable click) {
        Primitive bounds = Primitive.of(new Rect(
            context.getCanvas().getWidth() - BUTTON_WIDTH - 2, y + 2,
            BUTTON_WIDTH, BUTTON_HEIGHT), color);

        Text text = new Text(label, font, Align.CENTER);
        text.getPosition().set(context.getCanvas().getWidth() - BUTTON_WIDTH / 2f, y + 19);

        new DisplayObject()
            .withGraphics(bounds, text)
            .withLayer(context.getStage().getLayer("hud"))
            .withClickHandler(click)
            .attachTo(context);
    }

    private void initEffects() {
        Timeline timeline = new Timeline(Interpolation.LINEAR, true);
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(2f, 1f);
        timeline.addKeyFrame(4f, 0f);

        colorizeLogo = Effect.forImage(context.getMediaLoader().loadImage(COLORIZE_LOGO), timeline);
        Transform transform = colorizeLogo.getTransform();
        colorizeLogo.modify(value -> colorizeLogo.setPosition(50, context.getCanvas().getHeight() - 50));
        colorizeLogo.modify(value -> transform.setScale(80 + Math.round(value * 40f)));
        colorizeLogo.modifyFrameUpdate(dt -> transform.addRotation(Math.round(dt * 100f)));
        colorizeLogo.attachTo(context);

        TransitionEffect.reveal(context, COLORIZE_COLOR, 1.5f)
            .attachTo(context);
    }

    private void toggleBackgroundColor(Stage stage) {
        if (stage.getBackgroundColor().equals(BACKGROUND_COLOR)) {
            stage.setBackgroundColor(ColorRGB.WHITE);
        } else {
            stage.setBackgroundColor(BACKGROUND_COLOR);
        }
    }

    @Override
    public void update(SceneContext context, float deltaTime) {
        InputDevice inputDevice = context.getInputDevice();
        handleClick(inputDevice, context.getStage());
        checkLogoClick(inputDevice);
        handleSystemControls(inputDevice, context.getNetwork());

        for (Mario mario : marios) {
            updateMario(mario, deltaTime);
        }

        updateHUD();
    }

    private void updateMario(Mario mario, float deltaTime) {
        mario.update(deltaTime);
        mario.sprite.setPosition(mario.position);
        mario.sprite.setTransform(mario.mask ? MASK_TRANSFORM : null);
    }

    private void updateHUD() {
        FrameStats stats = context.getFrameStats();

        hud.setText(
            "Canvas:  " + context.getCanvas(),
            "Framerate:  " + Math.round(stats.getFramerate()),
            "Update time:  " + stats.getUpdateTime() + "ms",
            "Render time:  " + stats.getRenderTime() + "ms",
            "Sprites:  " + marios.size()
        );
    }

    private void checkLogoClick(InputDevice input) {
        Rect area = new Rect(0f, context.getCanvas().getHeight() - 80f, 80f, 80f);

        if (input.isPointerReleased(area)) {
            Transform transform = colorizeLogo.getTransform();
            transform.setFlipHorizontal(!transform.isFlipHorizontal());
        }
    }

    private void handleClick(InputDevice inputDevice, Stage stage) {
        if (checkMarioClick(inputDevice)) {
            return;
        }

        if (inputDevice.isPointerReleased(stage.getCanvas().getBounds())) {
            createTouchMarker(inputDevice.getPointers());
        }
    }

    private boolean checkMarioClick(InputDevice inputDevice) {
        for (Mario mario : marios) {
            if (inputDevice.isPointerReleased(mario.getBounds())) {
                mario.mask = !mario.mask;
                return true;
            }
        }
        return false;
    }

    private void handleSystemControls(InputDevice input, NetworkAccess network) {
        if (input.isKeyReleased(KeyCode.U)) {
            sendRequest(network);
        }

        List<KeyCode> canvasControls = ImmutableList.of(KeyCode.N1, KeyCode.N2, KeyCode.N3, KeyCode.N4);

        for (int i = 0; i < canvasControls.size(); i++) {
            if (input.isKeyReleased(canvasControls.get(i))) {
                changeCanvasStrategy(i);
            }
        }
    }

    private void changeCanvasStrategy(int index) {
        Canvas.ZoomStrategy strategy = Canvas.ZoomStrategy.values()[index];
        LOGGER.info("Changing canvas zoom strategy to " + strategy);
        context.getCanvas().changeStrategy(strategy);
    }

    private void createTouchMarker(List<Point2D> positions) {
        for (Point2D position : positions) {
            Timeline timeline = new Timeline();
            timeline.addKeyFrame(0f, 100f);
            timeline.addKeyFrame(1f, 100f);
            timeline.addKeyFrame(1.5f, 0f);

            String text = Math.round(position.getX()) + ", " + Math.round(position.getY());

            Effect.forTextAlpha(text, font, Align.LEFT, timeline)
                .setPosition(position)
                .attachTo(context);
        }
    }

    public void addMarios(Stage stage, int amount) {
        for (int i = 0; i < amount; i++) {
            Sprite marioSprite = createMarioSprite();
            Mario mario = new Mario(marioSprite,
                new Rect(0, 0, context.getCanvas().getWidth(), context.getCanvas().getHeight()));
            marios.add(mario);
            updateMario(mario, 0f);
            stage.add(marioSprite);
        }
    }

    private Sprite createMarioSprite() {
        Sprite marioSprite = new Sprite();
        for (String direction : DIRECTIONS) {
            List<Image> frames = marioSpriteSheet.get(direction + "_0",
                direction + "_1", direction + "_2", direction + "_3", direction + "_4");
            Animation anim = new Animation(frames, 0.1f, true);
            marioSprite.addState(direction, anim);
        }
        return marioSprite;
    }

    private void removeMarios(int amount) {
        for (int i = 0; i < amount && !marios.isEmpty(); i++) {
            marios.remove(marios.size() - 1);
        }
    }

    private void sendRequest(NetworkAccess internetAccess) {
        LOGGER.info("Sending request to " + TEST_URL);

        Headers headers = new Headers();
        headers.add(HttpHeaders.X_DO_NOT_TRACK, "1");
        internetAccess.get(TEST_URL, headers, Callback.from(LOGGER::info, LOGGER));
    }

    /**
     * Represents one of the mario sprites that walks around the scene.
     */
    private static class Mario implements Updatable {

        private Sprite sprite;
        private Rect canvasBounds;
        private Point2D position;
        private int direction;
        private float speed;
        private boolean mask;

        public Mario(Sprite sprite, Rect canvasBounds) {
            this.sprite = sprite;
            this.position = new Point2D(RandomGenerator.getFloat(0f, canvasBounds.getWidth()),
                RandomGenerator.getFloat(0f, canvasBounds.getHeight()));
            this.canvasBounds = canvasBounds;
            this.direction = RandomGenerator.getInt(0, 4);
            this.speed = RandomGenerator.getFloat(20f, 80f);
            this.mask = false;
        }

        @Override
        public void update(float deltaTime) {
            sprite.changeState(DIRECTIONS.get(direction));
            sprite.update(deltaTime);

            switch (direction) {
                case 0 : position.add(0, -speed * deltaTime); break;
                case 1 : position.add(speed * deltaTime, 0); break;
                case 2 : position.add(0, speed * deltaTime); break;
                case 3 : position.add(-speed * deltaTime, 0); break;
                default : throw new AssertionError();
            }

            checkBounds();
        }

        private void checkBounds() {
            if (position.getX() < 0 || position.getX() > canvasBounds.getWidth() ||
                position.getY() < 0 || position.getY() > canvasBounds.getHeight()) {
                direction = (direction + 2) % 4;
            }
        }

        private Rect getBounds() {
            return new Rect(position.getX() - sprite.getCurrentWidth() / 2f,
                position.getY() - sprite.getCurrentHeight() / 2f,
                sprite.getCurrentWidth(), sprite.getCurrentHeight());
        }
    }
}
