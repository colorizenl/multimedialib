//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.demo;

import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.RandomGenerator;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.scene.Effect;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.Updatable;
import nl.colorize.multimedialib.scene.WipeTransition;
import nl.colorize.multimedialib.stage.Align;
import nl.colorize.multimedialib.stage.Animation;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.Layer2D;
import nl.colorize.multimedialib.stage.OutlineFont;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Shader;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.SpriteAtlas;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.stage.Transform;
import nl.colorize.util.LogHelper;
import nl.colorize.util.animation.Interpolation;
import nl.colorize.util.animation.Timeline;
import nl.colorize.util.http.Headers;
import nl.colorize.util.http.PostData;

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

    private SpriteAtlas marioSpriteSheet;
    private OutlineFont font;
    private List<Mario> marios;
    private Effect colorizeLogo;
    private Audio audioClip;
    private Text hud;
    private Shader sepiaShader;

    public static final int DEFAULT_CANVAS_WIDTH = 800;
    public static final int DEFAULT_CANVAS_HEIGHT = 600;

    private static final FilePointer MARIO_SPRITES_FILE = new FilePointer("demo/demo.png");
    private static final FilePointer AUDIO_FILE = new FilePointer("demo/demo.mp3");
    private static final FilePointer COLORIZE_LOGO = new FilePointer("colorize-logo.png");
    private static final FilePointer FRAGMENT_SHADER = new FilePointer("demo/sepia-fragment.glsl");
    private static final FilePointer VERTEX_SHADER = new FilePointer("demo/sepia-vertex.glsl");

    private static final int INITIAL_MARIOS = 20;
    private static final List<String> DIRECTIONS = List.of("north", "east", "south", "west");
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 25;
    private static final ColorRGB RED_BUTTON = new ColorRGB(228, 93, 97);
    private static final ColorRGB GREEN_BUTTON = ColorRGB.parseHex("#72A725");
    private static final ColorRGB PINK_BUTTON = ColorRGB.parseHex("#B75797");
    private static final ColorRGB BACKGROUND_COLOR = ColorRGB.parseHex("#343434");
    private static final Transform MASK_TRANSFORM = Transform.withMask(ColorRGB.WHITE);
    private static final ColorRGB COLORIZE_COLOR = ColorRGB.parseHex("#e45d61");
    private static final String EXAMPLE_URL = "https://dashboard.clrz.nl/rest/echo";
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
        sepiaShader = context.getMediaLoader().loadShader(VERTEX_SHADER, FRAGMENT_SHADER);

        initHUD(context);
        initEffects();
        sendHttpRequest(context.getNetwork());
    }

    private void initMarioSprites(MediaLoader mediaLoader) {
        Image image = mediaLoader.loadImage(MARIO_SPRITES_FILE);
        marioSpriteSheet = new SpriteAtlas();

        int y = 0;
        for (String direction : DIRECTIONS) {
            for (int i = 0; i <= 4; i++) {
                marioSpriteSheet.add(direction + "_" + i, image, new Region(i * 48, y, 48, 64));
            }
            y += 64;
        }
    }

    private void initHUD(SceneContext context) {
        Stage stage = context.getStage();
        Layer2D hudLayer = stage.addLayer("hud");

        hud = new Text("", font);
        hud.getPosition().set(20, 20);
        hud.setLineHeight(20);
        stage.add("hud", hud);

        createButton(context, "Add sprites", RED_BUTTON, 0, () -> addMarios(stage, 10));
        createButton(context, "Remove sprites", RED_BUTTON, 30, () -> removeMarios(10));
        createButton(context, "Play sound", GREEN_BUTTON, 60, audioClip::play);
        createButton(context, "Background", GREEN_BUTTON, 90, this::toggleBackgroundColor);
        createButton(context, "Toggle shader", PINK_BUTTON, 120, this::toggleShader);

        Polygon hexagon = new Polygon(80, 70, 120, 70, 135, 100, 120, 130, 80, 130, 65, 100);
        Primitive hexagonPrimitive = Primitive.of(hexagon, COLORIZE_COLOR);
        hexagonPrimitive.setAlpha(50f);
        hudLayer.add(hexagonPrimitive);

        Effect effect = new Effect();
        effect.addFrameHandler(deltaTime -> {
            hexagonPrimitive.setPosition(50f, stage.getCanvas().getHeight() - 150f);
        });
        context.attach(effect);
    }

    private void createButton(SceneContext context, String label, ColorRGB color, int y, Runnable click) {
        Primitive bounds = Primitive.of(new Rect(
            context.getCanvas().getWidth() - BUTTON_WIDTH - 2, y + 2,
            BUTTON_WIDTH, BUTTON_HEIGHT), color);
        context.getStage().add(context.getStage().getLayer("hud"), bounds);

        Text text = new Text(label, font, Align.CENTER);
        text.getPosition().set(context.getCanvas().getWidth() - BUTTON_WIDTH / 2f, y + 19);
        context.getStage().add(context.getStage().getLayer("hud"), text);

        Effect effect = new Effect();
        effect.addClickHandler(bounds, click);
        context.attach(effect);
    }

    private void initEffects() {
        Timeline timeline = new Timeline(Interpolation.LINEAR, true);
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(2f, 1f);
        timeline.addKeyFrame(4f, 0f);

        Sprite sprite = new Sprite(context.getMediaLoader().loadImage(COLORIZE_LOGO));
        context.getStage().getLayer("hud").add(sprite);

        colorizeLogo = new Effect();
        colorizeLogo.addTimelineHandler(timeline, value -> {
            sprite.setPosition(50, context.getCanvas().getHeight() - 50);
            sprite.getTransform().setScale(80 + value * 40f);
        });
        colorizeLogo.addFrameHandler(dt -> sprite.getTransform().addRotation(dt * 100f));
        context.attach(colorizeLogo);

        context.attach(WipeTransition.reveal(context, COLORIZE_COLOR, 1.5f));
    }

    private void sendHttpRequest(Network network) {
        PostData data = PostData.create("message", "1234");
        network.post(EXAMPLE_URL, new Headers(), data, response -> {
            LOGGER.info("Received response from HTTP request:");
            LOGGER.info("Content-Type is " + response.getContentType().orElse("?"));
            LOGGER.info(response.getBody().replace("\n", ""));
        });
    }

    private void toggleBackgroundColor() {
        Stage stage = context.getStage();
        if (stage.getBackgroundColor().equals(BACKGROUND_COLOR)) {
            stage.setBackgroundColor(ColorRGB.WHITE);
        } else {
            stage.setBackgroundColor(BACKGROUND_COLOR);
        }
    }

    private void toggleShader() {
        //TODO Layer2D defaultLayer = context.getStage().getLayer(Layer2D.DEFAULT_LAYER_NAME);
        // defaultLayer.setShader(defaultLayer.getShader() == null ? sepiaShader : null);
    }

    @Override
    public void update(SceneContext context, float deltaTime) {
        InputDevice inputDevice = context.getInputDevice();
        handleClick(inputDevice, context.getStage());
        checkLogoClick(inputDevice);
        handleSystemControls(inputDevice);

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
        Canvas canvas = context.getCanvas();

        hud.setText(
            "Canvas:  " + canvas,
            "Framerate:  " + Math.round(stats.getFramerate()),
            "Update time:  " + stats.getUpdateTime() + "ms",
            "Render time:  " + stats.getRenderTime() + "ms",
            "Sprites:  " + marios.size()
        );
    }

    private void checkLogoClick(InputDevice input) {
        Rect area = new Rect(0f, context.getCanvas().getHeight() - 80f, 80f, 80f);

        if (input.isPointerReleased(area)) {
            colorizeLogo.withLinkedGraphics(g -> {
                Sprite sprite = (Sprite) g;
                Transform transform = sprite.getTransform();
                transform.setFlipHorizontal(!transform.isFlipHorizontal());
            });
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
            if (inputDevice.isPointerReleased(mario.sprite.getBounds())) {
                mario.mask = !mario.mask;
                return true;
            }
        }
        return false;
    }

    private void handleSystemControls(InputDevice input) {
        if (input.isKeyReleased(KeyCode.N1)) {
            context.getCanvas().changeStrategy(false);
        } else if (input.isKeyReleased(KeyCode.N2)) {
            context.getCanvas().changeStrategy(true);
        }
    }

    private void createTouchMarker(List<Point2D> positions) {
        for (Point2D position : positions) {
            Timeline timeline = new Timeline();
            timeline.addKeyFrame(0f, 100f);
            timeline.addKeyFrame(1f, 100f);
            timeline.addKeyFrame(1.5f, 0f);

            Text text = new Text(Math.round(position.getX()) + ", " + Math.round(position.getY()),
                font, Align.LEFT);
            text.setPosition(position);
            context.getStage().getLayer("hud").add(text);

            context.attach(Effect.forTextAlpha(text, timeline));
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
            List<Image> frames = marioSpriteSheet.get(List.of(direction + "_0",
                direction + "_1", direction + "_2", direction + "_3", direction + "_4"));
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
    }
}
