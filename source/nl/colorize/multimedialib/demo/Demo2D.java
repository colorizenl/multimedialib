//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.demo;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.net.HttpHeaders;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.RandomGenerator;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.ErrorHandler;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.Pointer;
import nl.colorize.multimedialib.scene.Effect;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.SwipeTracker;
import nl.colorize.multimedialib.scene.Updatable;
import nl.colorize.multimedialib.scene.WipeTransition;
import nl.colorize.multimedialib.stage.Align;
import nl.colorize.multimedialib.stage.Animation;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.OutlineFont;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.SpriteAtlas;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.stage.Transform;
import nl.colorize.util.animation.Interpolation;
import nl.colorize.util.animation.Timeline;
import nl.colorize.util.http.Headers;
import nl.colorize.util.http.PostData;
import nl.colorize.util.stats.Tuple;

import java.util.ArrayList;
import java.util.List;

import static nl.colorize.multimedialib.stage.ColorRGB.BLUE;
import static nl.colorize.multimedialib.stage.ColorRGB.GREEN;
import static nl.colorize.multimedialib.stage.ColorRGB.RED;
import static nl.colorize.multimedialib.stage.ColorRGB.YELLOW;

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
public class Demo2D implements Scene, ErrorHandler {

    private SceneContext context;
    private Container contentLayer;
    private Container hudLayer;

    private SpriteAtlas marioSpriteSheet;
    private OutlineFont font;
    private List<Mario> marios;
    private Effect colorizeLogo;
    private Audio audioClip;
    private Text hud;

    public static final int DEFAULT_CANVAS_WIDTH = 800;
    public static final int DEFAULT_CANVAS_HEIGHT = 600;

    private static final FilePointer MARIO_SPRITES_FILE = new FilePointer("demo/demo.png");
    private static final FilePointer AUDIO_FILE = new FilePointer("demo/demo-sound.mp3");
    private static final FilePointer COLORIZE_LOGO = new FilePointer("colorize-logo.png");
    private static final FilePointer FRAGMENT_SHADER = new FilePointer("demo/sepia-fragment.glsl");
    private static final FilePointer VERTEX_SHADER = new FilePointer("demo/sepia-vertex.glsl");

    private static final List<String> DIRECTIONS = List.of("north", "east", "south", "west");
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 25;
    private static final ColorRGB RED_BUTTON = new ColorRGB(228, 93, 97);
    private static final ColorRGB GREEN_BUTTON = ColorRGB.parseHex("#72A725");
    private static final ColorRGB PINK_BUTTON = ColorRGB.parseHex("#B75797");
    private static final ColorRGB BACKGROUND_COLOR = ColorRGB.parseHex("#343434");
    private static final ColorRGB ALT_BACKGROUND_COLOR = ColorRGB.parseHex("#EBEBEB");
    private static final ColorRGB COLORIZE_COLOR = ColorRGB.parseHex("#e45d61");
    private static final String EXAMPLE_URL = "https://dashboard.clrz.nl/rest/echo";

    private static final List<ColorRGB> SWIPE_COLORS = List.of(RED, GREEN, BLUE, YELLOW);

    @Override
    public void start(SceneContext context) {
        this.context = context;
        this.contentLayer = context.getStage().addContainer();
        this.hudLayer = context.getStage().addContainer();

        MediaLoader mediaLoader = context.getMediaLoader();

        context.getStage().setBackgroundColor(BACKGROUND_COLOR);

        initMarioSprites(mediaLoader);
        marios = new ArrayList<>();
        addMarios();

        font = mediaLoader.loadDefaultFont(12, ColorRGB.WHITE);
        audioClip = mediaLoader.loadAudio(AUDIO_FILE);

        initHUD();
        initEffects();
        attachSwipeTracker();
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

    private void initHUD() {
        hud = new Text("", font);
        hud.getTransform().setPosition(20, 20);
        hud.setLineHeight(20);
        hudLayer.addChild(hud);

        createButton(context, "Add sprites", RED_BUTTON, 0, this::addMarios);
        createButton(context, "Remove sprites", RED_BUTTON, 30, () -> removeMarios(10));
        createButton(context, "Play sound", GREEN_BUTTON, 60, audioClip::play);
        createButton(context, "Background", GREEN_BUTTON, 90, this::toggleBackgroundColor);
        createButton(context, "Cause error", PINK_BUTTON, 120, this::causeError);

        Polygon hexagon = new Polygon(80, 70, 120, 70, 135, 100, 120, 130, 80, 130, 65, 100);
        Primitive hexagonPrimitive = new Primitive(hexagon, COLORIZE_COLOR);
        hexagonPrimitive.getTransform().setAlpha(50f);
        hudLayer.addChild(hexagonPrimitive);

        Effect.forFrameHandler(deltaTime -> {
            hexagonPrimitive.setPosition(50f, context.getCanvas().getHeight() - 150f);
        }).attach(context);
    }

    private void createButton(SceneContext context, String label, ColorRGB color, int y, Runnable click) {
        Primitive bounds = new Primitive(new Rect(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT), color);
        hudLayer.addChild(bounds);

        Text text = new Text(label, font, Align.CENTER);
        hudLayer.addChild(text);

        // Need to keep the button in position for when the canvas
        // is resized.
        context.attach(deltaTime -> {
            bounds.getTransform().setPosition(context.getCanvas().getWidth() - BUTTON_WIDTH - 2, y + 2);
            text.getTransform().setPosition(context.getCanvas().getWidth() - BUTTON_WIDTH / 2f, y + 19);
        });

        Effect.forClickHandler(bounds, click).attach(context);
    }

    private void initEffects() {
        Timeline timeline = new Timeline(Interpolation.LINEAR, true);
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(2f, 1f);
        timeline.addKeyFrame(4f, 0f);

        Sprite sprite = new Sprite(context.getMediaLoader().loadImage(COLORIZE_LOGO));
        hudLayer.addChild(sprite);

        Effect.forTimeline(timeline, value -> {
            sprite.setPosition(50, context.getCanvas().getHeight() - 50);
            sprite.getTransform().setScale(80 + value * 40f);
        }).addFrameHandler(dt -> sprite.getTransform().addRotation(dt * 100f))
        .attach(context);

        Image diamond = context.getMediaLoader().loadImage(WipeTransition.DIAMOND);
        WipeTransition wipe = new WipeTransition(diamond, COLORIZE_COLOR, 1.5f, true);
        context.attach(wipe);
        hudLayer.addChild(wipe);
    }

    private void attachSwipeTracker() {
        SwipeTracker swipeTracker = new SwipeTracker(50f);
        context.attach(swipeTracker);

        context.attach(deltaTime -> {
            List<Line> swipes = ImmutableList.copyOf(swipeTracker.getSwipes().flush());

            for (int i = 0; i < swipes.size(); i++) {
                drawSwipeMarker(swipes.get(i), SWIPE_COLORS.get(i % SWIPE_COLORS.size()));
            }
        });
    }

    private void drawSwipeMarker(Line swipe, ColorRGB color) {
        Primitive swipeMarker = new Primitive(swipe, color);
        swipeMarker.setStroke(2f);
        hudLayer.addChild(swipeMarker);

        Timeline timeline = new Timeline()
            .addKeyFrame(0f, 100f)
            .addKeyFrame(1f, 100f)
            .addKeyFrame(1.5f, 0f);

        context.attach(Effect.forPrimitiveAlpha(swipeMarker, timeline));
    }

    private void sendHttpRequest(Network network) {
        Headers headers = new Headers(Tuple.of(HttpHeaders.ACCEPT, "text/plain"));
        PostData data = PostData.create("message", "1234");

        Text info = new Text("Network request pending", font, Align.RIGHT);
        info.setPosition(context.getCanvas().getWidth() - 20, context.getCanvas().getHeight() - 100);
        hudLayer.addChild(info);

        network.post(EXAMPLE_URL, headers, data).subscribe(response -> {
            List<String> text = new ArrayList<>();
            text.add("Network request succeeded");
            text.add("Content-Type: " + response.getContentType().orElse("?"));
            text.addAll(Splitter.on("\n").omitEmptyStrings().splitToList(response.getBody()));
            info.setText(text);
        }, e -> info.setText("Failed to send network request"));
    }

    private void toggleBackgroundColor() {
        Stage stage = context.getStage();

        if (stage.getBackgroundColor().equals(BACKGROUND_COLOR)) {
            stage.setBackgroundColor(ALT_BACKGROUND_COLOR);
        } else {
            stage.setBackgroundColor(BACKGROUND_COLOR);
        }
    }

    /**
     * This method will intentionally produce an exception, so that the demo
     * application can be used for testing the error handler.
     */
    private void causeError() {
        throw new RuntimeException("Intentional error");
    }

    @Override
    public void onError(SceneContext context, Exception cause) {
        Text errorText = new Text("Error:\n\n" + cause.getMessage(), font, Align.CENTER);
        errorText.getTransform().setPosition(context.getCanvas().getCenter());
        hudLayer.addChild(errorText);
    }

    @Override
    public void update(SceneContext context, float deltaTime) {
        InputDevice inputDevice = context.getInput();
        handleClick(inputDevice, context.getStage());
        checkLogoClick(inputDevice);
        handleSystemControls(inputDevice);

        for (Mario mario : marios) {
            mario.update(deltaTime);
        }

        List<String> info = context.getDebugInformation();
        info.add("Sprites:  " + marios.size());
        info.add("Keyboard:  " + inputDevice.isKeyboardAvailable());
        info.add("Touch:  " + inputDevice.isTouchAvailable());
        hud.setText(info);
    }

    private void checkLogoClick(InputDevice input) {
        Rect area = new Rect(0f, context.getCanvas().getHeight() - 80f, 80f, 80f);

        for (Pointer pointer : input.getPointers()) {
            if (pointer.isReleased(area)) {
                colorizeLogo.withLinkedGraphics(g -> {
                    Sprite sprite = (Sprite) g;
                    Transform transform = sprite.getTransform();
                    transform.setFlipHorizontal(!transform.isFlipHorizontal());
                });
            }
        }
    }

    private void handleClick(InputDevice input, Stage stage) {
        for (Pointer pointer : input.getPointers()) {
            if (checkMarioClick(pointer)) {
                return;
            }

            if (pointer.isReleased()) {
                createTouchMarker(pointer.getPosition());
            }
        }
    }

    private boolean checkMarioClick(Pointer pointer) {
        for (Mario mario : marios) {
            if (pointer.isReleased(mario.sprite.getStageBounds())) {
                mario.mask = !mario.mask;
                return true;
            }
        }
        return false;
    }

    private void handleSystemControls(InputDevice input) {
        if (input.isKeyReleased(KeyCode.N9) || input.isKeyReleased(KeyCode.B)) {
            toggleBackgroundColor();
        } else if (input.isKeyReleased(KeyCode.F12)) {
            context.takeScreenshot();
        }
    }

    private void createTouchMarker(Point2D position) {
        Timeline timeline = new Timeline()
            .addKeyFrame(0f, 100f)
            .addKeyFrame(1f, 100f)
            .addKeyFrame(1.5f, 0f);

        Container container = new Container();
        container.getTransform().setPosition(position);
        container.addChild(new Primitive(new Circle(Point2D.ORIGIN, 4f), ColorRGB.WHITE));
        container.addChild(new Text(position.toString(), font, Align.LEFT), 4, -4);
        hudLayer.addChild(container);

        Effect effect = Effect.forTimeline(timeline, value -> container.getTransform().setAlpha(value));
        effect.removeAfterwards(container);
        context.attach(effect);
    }

    public void addMarios() {
        int amount = marios.size() >= 100 ? 100 : 20;

        for (int i = 0; i < amount; i++) {
            Sprite marioSprite = createMarioSprite();
            Mario mario = new Mario(marioSprite, context.getCanvas());
            marios.add(mario);
            mario.update(0f);
            contentLayer.addChild(marioSprite);
        }
    }

    private Sprite createMarioSprite() {
        Sprite marioSprite = new Sprite();

        for (String direction : DIRECTIONS) {
            List<Image> frames = marioSpriteSheet.get(List.of(direction + "_0",
                direction + "_1", direction + "_2", direction + "_3", direction + "_4"));
            Animation anim = new Animation(frames, 0.1f, true);
            marioSprite.addGraphics(direction, anim);
        }

        return marioSprite;
    }

    private void removeMarios(int amount) {
        for (int i = 0; i < amount && !marios.isEmpty(); i++) {
            Mario removed = marios.remove(marios.size() - 1);
            contentLayer.removeChild(removed.sprite);
        }
    }

    /**
     * Represents one of the mario sprites that walks around the scene.
     */
    private static class Mario implements Updatable {

        private Sprite sprite;
        private Canvas canvas;
        private int direction;
        private float speed;
        private boolean mask;

        public Mario(Sprite sprite, Canvas canvas) {
            this.sprite = sprite;
            this.canvas = canvas;
            sprite.getTransform().setPosition(RandomGenerator.pickPoint(canvas.getBounds()));
            this.direction = RandomGenerator.getInt(0, 4);
            this.speed = RandomGenerator.getFloat(20f, 80f);
            this.mask = false;
        }

        @Override
        public void update(float deltaTime) {
            sprite.changeGraphics(DIRECTIONS.get(direction));
            sprite.update(deltaTime);

            Point2D position = sprite.getTransform().getPosition();

            Point2D newPosition = switch (direction) {
                case 0 -> new Point2D(position.getX(), position.getY() - speed * deltaTime);
                case 1 -> new Point2D(position.getX() + speed * deltaTime, position.getY());
                case 2 -> new Point2D(position.getX(), position.getY() + speed * deltaTime);
                case 3 -> new Point2D(position.getX() - speed * deltaTime, position.getY());
                default -> position;
            };

            sprite.getTransform().setPosition(newPosition);
            sprite.getTransform().setMaskColor(mask ? ColorRGB.WHITE : null);
            checkBounds();
        }

        private void checkBounds() {
            Point2D position = sprite.getTransform().getPosition();
            if (!canvas.getBounds().contains(position)) {
                direction = (direction + 2) % 4;
            }
        }
    }
}
