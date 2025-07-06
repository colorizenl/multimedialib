//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.base.Splitter;
import com.google.common.net.HttpHeaders;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.RandomGenerator;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.ErrorHandler;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.PeerConnection;
import nl.colorize.multimedialib.renderer.Pointer;
import nl.colorize.multimedialib.renderer.teavm.PeerMessage;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.Updatable;
import nl.colorize.multimedialib.scene.effect.Effect;
import nl.colorize.multimedialib.scene.effect.ParticleWipe;
import nl.colorize.multimedialib.scene.effect.PerformanceMonitor;
import nl.colorize.multimedialib.scene.effect.SwipeTracker;
import nl.colorize.multimedialib.stage.Align;
import nl.colorize.multimedialib.stage.Animation;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.SpriteAtlas;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.util.LogHelper;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.animation.Interpolation;
import nl.colorize.util.animation.Timeline;
import nl.colorize.util.http.Headers;
import nl.colorize.util.http.PostData;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import static nl.colorize.multimedialib.stage.ColorRGB.BLUE;
import static nl.colorize.multimedialib.stage.ColorRGB.GREEN;
import static nl.colorize.multimedialib.stage.ColorRGB.RED;
import static nl.colorize.multimedialib.stage.ColorRGB.WHITE;
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
 * {@link DemoLauncher}. It can also be embedded in applications.
 */
public class Demo2D implements Scene, ErrorHandler {

    private SceneContext context;
    private Container contentLayer;
    private Container hudLayer;

    private SpriteAtlas marioSpriteSheet;
    private FontFace font;
    private List<Mario> marios;
    private Audio audioClip;
    private Text hud;
    private PerformanceMonitor performanceMonitor;

    public static final int DEFAULT_CANVAS_WIDTH = 800;
    public static final int DEFAULT_CANVAS_HEIGHT = 600;

    protected static final int BUTTON_WIDTH = 100;
    protected static final int BUTTON_HEIGHT = 25;
    protected static final ColorRGB RED_BUTTON = new ColorRGB(228, 93, 97);
    protected static final ColorRGB GREEN_BUTTON = ColorRGB.parseHex("#72A725");
    protected static final ColorRGB PINK_BUTTON = ColorRGB.parseHex("#B75797");
    protected static final ColorRGB BLUE_BUTTON = ColorRGB.parseHex("#43A1C7");
    protected static final ColorRGB ORANGE_BUTTON = ColorRGB.parseHex("#F1723D");
    protected static final ResourceFile MARIO_SPRITES_FILE = new ResourceFile("demo/demo.png");

    private static final ResourceFile AUDIO_FILE = new ResourceFile("demo/demo-sound.mp3");
    private static final ResourceFile COLORIZE_LOGO = new ResourceFile("colorize-logo.png");
    private static final ColorRGB BACKGROUND_COLOR = ColorRGB.parseHex("#343434");
    private static final ColorRGB ALT_BACKGROUND_COLOR = ColorRGB.parseHex("#EBEBEB");
    private static final ColorRGB COLORIZE_COLOR = ColorRGB.parseHex("#e45d61");
    private static final String EXAMPLE_URL = "https://dashboard.clrz.nl/rest/echo";
    private static final List<ColorRGB> SWIPE_COLORS = List.of(RED, GREEN, BLUE, YELLOW);
    private static final List<String> DIRECTIONS = List.of("north", "east", "south", "west");
    private static final Logger LOGGER = LogHelper.getLogger(Demo2D.class);

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

        font = mediaLoader.loadDefaultFont(12, WHITE);
        audioClip = mediaLoader.loadAudio(AUDIO_FILE);

        initHUD();
        initEffects();
        attachSwipeTracker();
        sendHttpRequest(context.getNetwork());
        loadApplicationData();

        performanceMonitor = new PerformanceMonitor(false);
        performanceMonitor.setActive(false);
        context.attach(performanceMonitor);
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
        hud.getTransform().setPosition(20, 30);
        hud.setLineHeight(20);
        hudLayer.addChild(hud);

        createButton(context, "Add sprites", RED_BUTTON, 0, this::addMarios);
        createButton(context, "Remove sprites", RED_BUTTON, 30, () -> removeMarios(10));
        createButton(context, "Play sound", GREEN_BUTTON, 60, audioClip::play);
        createButton(context, "Background", GREEN_BUTTON, 90, this::toggleBackgroundColor);
        createButton(context, "App data", PINK_BUTTON, 120, this::saveApplicationData);
        createButton(context, "Cause error", PINK_BUTTON, 150, this::causeError);
        createButton(context, "Performance", PINK_BUTTON, 180, this::togglePerformanceMonitor);
        if (context.getNetwork().isPeerToPeerSupported()) {
            createButton(context, "Open connection", BLUE_BUTTON, 210, this::openPeerConnection);
            createButton(context, "Join connection", BLUE_BUTTON, 240, this::joinPeerConnection);
        }
    }

    private void createButton(SceneContext context, String label, ColorRGB color, int y, Runnable click) {
        Primitive bounds = new Primitive(new Rect(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT), color);
        bounds.getTransform().setPosition(-BUTTON_WIDTH / 2f - 2, 2);

        Text text = new Text(label, font, Align.CENTER);
        text.getTransform().setY(19);

        Container button = new Container();
        button.addChild(bounds);
        button.addChild(text);
        hudLayer.addChild(button);

        // Need to keep the button in position for when the canvas
        // is resized.
        context.attach(deltaTime -> {
            int buttonX = context.getCanvas().getWidth() - BUTTON_WIDTH / 2;
            button.getTransform().setPosition(buttonX, y);
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
            sprite.setPosition(context.getCanvas().getWidth() / 2f, context.getCanvas().getHeight() - 50);
            sprite.getTransform().setScale(80 + value * 40f);
        }).addFrameHandler(dt -> sprite.getTransform().addRotation(dt * 100f))
        .attach(context);

        Image diamond = context.getMediaLoader().loadImage(ParticleWipe.DIAMOND);
        ParticleWipe wipe = new ParticleWipe(diamond, COLORIZE_COLOR, 1.5f, true);
        context.attach(wipe);
    }

    private void attachSwipeTracker() {
        SwipeTracker swipeTracker = new SwipeTracker(50f);
        context.attach(swipeTracker);

        context.attach(deltaTime -> {
            List<Line> swipes = swipeTracker.getSwipes().flush().toList();

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
        Headers headers = Headers.of(HttpHeaders.ACCEPT, "text/plain");
        PostData data = PostData.create("message", "1234");

        Text info = new Text("Network request pending", font, Align.RIGHT);
        info.setPosition(context.getCanvas().getWidth() - 20, context.getCanvas().getHeight() - 100);
        hudLayer.addChild(info);

        network.post(EXAMPLE_URL, headers, data).subscribe(response -> {
            List<String> text = new ArrayList<>();
            text.add("Network request succeeded");
            text.add("Content-Type: " + response.getContentType().orElse("?"));
            text.addAll(Splitter.on("\n").omitEmptyStrings().splitToList(response.readBody()));
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

    private void loadApplicationData() {
        MediaLoader mediaLoader = context.getMediaLoader();
        Properties data = mediaLoader.loadApplicationData("MultimediaLib-Demo2D");
        String message = data.getProperty("test", "");

        if (!message.isEmpty()) {
            Text info = new Text("Loaded message:\n" + message, font, Align.RIGHT);
            info.getTransform().setPosition(context.getCanvas().getWidth() - 20,
                context.getCanvas().getHeight() - 200);
            hudLayer.addChild(info);
        }
    }

    private void saveApplicationData() {
        context.getInput().requestTextInput("Enter some text:", "").subscribe(input -> {
            Properties data = new Properties();
            data.setProperty("test", input);

            MediaLoader mediaLoader = context.getMediaLoader();
            mediaLoader.saveApplicationData("MultimediaLib-Demo2D", data);
        });
    }

    /**
     * This method will intentionally produce an exception, so that the demo
     * application can be used for testing the error handler.
     */
    private void causeError() {
        throw new RuntimeException("Intentional error");
    }

    private void togglePerformanceMonitor() {
        performanceMonitor.setActive(!performanceMonitor.isActive());
    }

    private PeerConnection openPeerConnection() {
        PeerConnection peerConnection = context.getNetwork().openPeerConnection();

        context.attach(() -> {
            for (PeerMessage message : peerConnection.flushReceivedMessages()) {
                LOGGER.info("Received message: " + message.type() + " / " + message.value());
                if (message.type().equals(PeerMessage.TYPE_INIT)) {
                    context.getInput().fillClipboard(message.value());
                } else if (!message.type().equals(PeerMessage.TYPE_CONNECT)) {
                    showNotification("Received message: " + message.value());
                }
            }
        });

        return peerConnection;
    }

    private void joinPeerConnection() {
        context.getInput().requestTextInput("Peer-to-peer connection ID", "").subscribe(id -> {
            PeerConnection peerConnection = openPeerConnection();
            peerConnection.connect(id);
            peerConnection.sendMessage("Hello from a peer-to-peer connection");
        });
    }

    @Override
    public void onError(SceneContext context, Exception cause) {
        showNotification("Error:\n\n" + cause.getMessage());
    }

    private void showNotification(String message) {
        Text notification = new Text(message, font, Align.CENTER);
        notification.getTransform().setPosition(context.getCanvas().getCenter());
        hudLayer.addChild(notification);

        Timeline timeline = new Timeline(Interpolation.LINEAR)
            .addKeyFrame(0f, context.getCanvas().getCenter().y())
            .addKeyFrame(4f, context.getCanvas().getCenter().y() - 100f);

        Effect.delay(4f, () -> hudLayer.removeChild(notification))
            .addTimelineHandler(timeline, value -> notification.getTransform().setY(value))
            .attach(context);
    }

    @Override
    public void update(SceneContext context, float deltaTime) {
        InputDevice inputDevice = context.getInput();
        handleClick(inputDevice);
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

    private void handleClick(InputDevice input) {
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
        }
    }

    private void createTouchMarker(Point2D position) {
        Timeline timeline = new Timeline()
            .addKeyFrame(0f, 100f)
            .addKeyFrame(1f, 100f)
            .addKeyFrame(1.5f, 0f);

        Container container = new Container();
        container.getTransform().setPosition(position);
        container.addChild(new Primitive(new Circle(Point2D.ORIGIN, 4f), WHITE));
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
            List<Image> frames = marioSpriteSheet.get(List.of(
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

            Point2D position = sprite.getTransform().getPosition();

            Point2D newPosition = switch (direction) {
                case 0 -> new Point2D(position.x(), position.y() - speed * deltaTime);
                case 1 -> new Point2D(position.x() + speed * deltaTime, position.y());
                case 2 -> new Point2D(position.x(), position.y() + speed * deltaTime);
                case 3 -> new Point2D(position.x() - speed * deltaTime, position.y());
                default -> position;
            };

            sprite.getTransform().setPosition(newPosition);
            sprite.getTransform().setMaskColor(mask ? WHITE : null);
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
