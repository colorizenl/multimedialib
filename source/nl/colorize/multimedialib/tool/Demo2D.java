//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.collect.ImmutableList;
import com.google.common.net.HttpHeaders;
import nl.colorize.multimedialib.graphics.Align;
import nl.colorize.multimedialib.graphics.Animation;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.GraphicsLayer2D;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.SpriteSheet;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.RandomGenerator;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.GraphicsContext2D;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.NetworkAccess;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.Updatable;
import nl.colorize.multimedialib.scene.effect.Effect;
import nl.colorize.multimedialib.scene.effect.FireEffect;
import nl.colorize.multimedialib.scene.effect.TransitionEffect;
import nl.colorize.multimedialib.scene.ui.Button;
import nl.colorize.multimedialib.scene.ui.Location;
import nl.colorize.multimedialib.scene.ui.SelectBox;
import nl.colorize.multimedialib.scene.ui.TextField;
import nl.colorize.multimedialib.scene.ui.Widget;
import nl.colorize.multimedialib.scene.ui.WidgetStyle;
import nl.colorize.util.LogHelper;
import nl.colorize.util.animation.Interpolation;
import nl.colorize.util.animation.Timeline;
import nl.colorize.util.http.Headers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Simple demo application that displays a number of animated Mario sprites on
 * top of a black background.
 * <p>
 * The demo application serves two purposes. First, it can be used as an example
 * application when using the framework to implement an application. Second, it
 * can be used for verification purposes to determine if a new platform is fully
 * supported.
 * <p>
 * The demo application can be started from the command line using the
 * {@link DemoLauncher}. It can also be embedded in applications by creating an
 * instance of this class from the application code.
 */
public class Demo2D implements Scene, GraphicsLayer2D {

    private SceneContext context;

    private SpriteSheet marioSpriteSheet;
    private TTFont font;
    private List<Mario> marios;
    private Effect colorizeLogo;
    private Audio audioClip;
    private boolean canvasMask;
    private List<Widget> uiWidgets;

    public static final int DEFAULT_CANVAS_WIDTH = 800;
    public static final int DEFAULT_CANVAS_HEIGHT = 600;
    public static final int DEFAULT_FRAMERATE = 60;

    private static final FilePointer MARIO_SPRITES_FILE = new FilePointer("mario.png");
    private static final FilePointer AUDIO_FILE = new FilePointer("test.mp3");
    private static final FilePointer UI_WIDGET_FILE = new FilePointer("ui-widget-background.png");
    private static final FilePointer COLORIZE_LOGO = new FilePointer("colorize-logo.png");
    private static final int INITIAL_MARIOS = 20;
    private static final List<String> DIRECTIONS = ImmutableList.of("north", "east", "south", "west");
    private static final int NUM_BUTTONS = 7;
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

        initMarioSprites(mediaLoader);
        marios = new ArrayList<>();
        addMarios(INITIAL_MARIOS);

        font = mediaLoader.loadDefaultFont(12, ColorRGB.WHITE);
        audioClip = mediaLoader.loadAudio(AUDIO_FILE);
        uiWidgets = Collections.emptyList();

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

    private void initEffects() {
        Timeline animationTimeline = new Timeline(Interpolation.LINEAR, true);
        animationTimeline.addKeyFrame(0f, 0f);
        animationTimeline.addKeyFrame(2f, 1f);
        animationTimeline.addKeyFrame(4f, 0f);

        colorizeLogo = Effect.forImage(context.getMediaLoader().loadImage(COLORIZE_LOGO),
            animationTimeline);
        Transform transform = colorizeLogo.getTransform();
        colorizeLogo.modify(value -> colorizeLogo.setPosition(50, context.getCanvas().getHeight() - 50));
        colorizeLogo.modify(value -> transform.setScale(80 + Math.round(value * 40f)));
        colorizeLogo.modifyFrameUpdate(dt -> transform.addRotation(Math.round(dt * 100f)));
        context.attachAgent(colorizeLogo);

        TransitionEffect transition = TransitionEffect.reveal(context.getCanvas(),
            context.getMediaLoader(), COLORIZE_COLOR, 1.5f);
        context.attachAgent(transition);
    }

    @Override
    public void update(SceneContext context, float deltaTime) {
        InputDevice inputDevice = context.getInputDevice();
        handleClick(inputDevice, context.getCanvas());
        checkLogoClick(inputDevice);
        handleSystemControls(inputDevice, context.getNetworkAccess());

        for (Mario mario : marios) {
            mario.update(deltaTime);
        }

        uiWidgets.forEach(widget -> widget.update(deltaTime));
    }

    private void checkLogoClick(InputDevice input) {
        Rect area = new Rect(0f, context.getCanvas().getHeight() - 80f, 80f, 80f);

        if (input.isPointerReleased(area)) {
            Transform transform = colorizeLogo.getTransform();
            transform.setFlipHorizontal(!transform.isFlipHorizontal());
        }
    }

    private void handleClick(InputDevice inputDevice, Canvas canvas) {
        for (int i = 0; i <= NUM_BUTTONS; i++) {
            if (isButtonClicked(inputDevice, i)) {
                handleButtonClick(i);
                return;
            }
        }

        if (checkMarioClick(inputDevice)) {
            return;
        }

        if (inputDevice.isPointerReleased(canvas.getBounds())) {
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

    private boolean isButtonClicked(InputDevice input, int buttonIndex) {
        Rect buttonBounds = new Rect(context.getCanvas().getWidth() - BUTTON_WIDTH,
            buttonIndex * 30, BUTTON_WIDTH, BUTTON_HEIGHT);
        return input.isPointerReleased(buttonBounds);
    }

    private void handleButtonClick(int index) {
        switch (index) {
            case 0 : addMarios(10); break;
            case 1 : removeMarios(10); break;
            case 2 : audioClip.play(); break;
            case 3 : canvasMask = !canvasMask; break;
            case 4 : initUIWidgets(context.getMediaLoader(), context.getInputDevice()); break;
            case 5 : createFireEffect(); break;
            default : break;
        }
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

    private void initUIWidgets(MediaLoader mediaLoader, InputDevice input) {
        if (!uiWidgets.isEmpty()) {
            uiWidgets = Collections.emptyList();
            return;
        }

        Image widget = mediaLoader.loadImage(UI_WIDGET_FILE);

        Button button = new Button(new WidgetStyle(font, widget), "Click");
        button.setLocation(Location.fixed(200, 200));
        button.setClickHandler(input, () -> LOGGER.info("Button clicked"));

        SelectBox select = new SelectBox(new WidgetStyle(font, widget),
            ImmutableList.of("A", "B", "C"), "A");
        select.setLocation(Location.fixed(200, 240));
        select.setClickHandler(input, item -> LOGGER.info("Selected item " + item));

        TextField textField = new TextField(new WidgetStyle(font, widget), "Enter text:");
        textField.setLocation(Location.fixed(200, 280));
        textField.setChangeHandler(input, text -> LOGGER.info("Entered text: " + text));

        uiWidgets = ImmutableList.of(button, select, textField);
    }

    private void createTouchMarker(List<Point2D> positions) {
        for (Point2D position : positions) {
            Timeline timeline = new Timeline();
            timeline.addKeyFrame(0f, 100f);
            timeline.addKeyFrame(1f, 100f);
            timeline.addKeyFrame(1.5f, 0f);

            String text = Math.round(position.getX()) + ", " + Math.round(position.getY());

            Effect effect = Effect.forTextAlpha(text, font, Align.LEFT, timeline);
            effect.setPosition(position);
            context.attachAgent(effect);
        }
    }

    @Override
    public void render(GraphicsContext2D graphics) {
        graphics.drawBackground(BACKGROUND_COLOR);
        drawSprites(graphics);
        drawHUD(graphics);

        if (canvasMask) {
            graphics.drawRect(new Rect(10f, 10f, DEFAULT_CANVAS_WIDTH - 20f, DEFAULT_CANVAS_HEIGHT - 20f),
                ColorRGB.WHITE, Transform.withAlpha(10));
        }

        Polygon hexagon = new Polygon(80, 70, 120, 70, 135, 100, 120, 130, 80, 130, 65, 100);
        hexagon.move(-50f, 200f);
        graphics.drawPolygon(hexagon, COLORIZE_COLOR, 70f);

        uiWidgets.forEach(widget -> widget.render(graphics));
    }

    private void drawSprites(GraphicsContext2D graphics) {
        for (Mario mario : marios) {
            mario.sprite.setPosition(mario.position);
            mario.sprite.setTransform(mario.mask ? MASK_TRANSFORM : null);
            graphics.drawSprite(mario.sprite);
        }
    }

    private void drawHUD(GraphicsContext2D graphics) {
        drawButton(graphics, "Add sprites", RED_BUTTON, 0);
        drawButton(graphics, "Remove sprites", RED_BUTTON, 30);
        drawButton(graphics, "Play sound", GREEN_BUTTON, 60);
        drawButton(graphics, "Canvas bounds", GREEN_BUTTON, 90);
        drawButton(graphics, "UI widgets", GREEN_BUTTON, 120);
        drawButton(graphics, "Fire effect", GREEN_BUTTON, 150);

        Canvas canvas = context.getCanvas();

        graphics.drawText("Canvas:  " + canvas, font, 20, 20);
        graphics.drawText("Framerate:  " + Math.round(context.getAverageFramerate()), font, 20, 40);
        graphics.drawText("Frame time:  " + Math.round(context.getAverageFrameTime()) + "ms",
            font, 20, 60);
        graphics.drawText("Sprites:  " + marios.size(), font, 20, 80);
    }

    private void drawButton(GraphicsContext2D graphics, String label, ColorRGB background, int y) {
        graphics.drawRect(new Rect(context.getCanvas().getWidth() - BUTTON_WIDTH - 2, y + 2,
            BUTTON_WIDTH, BUTTON_HEIGHT), background, null);
        graphics.drawText(label, font, context.getCanvas().getWidth() - BUTTON_WIDTH / 2f, y + 17,
            Align.CENTER);
    }

    public void addMarios(int amount) {
        for (int i = 0; i < amount; i++) {
            Sprite marioSprite = createMarioSprite();
            marios.add(new Mario(marioSprite,
                new Rect(0, 0, context.getCanvas().getWidth(), context.getCanvas().getHeight())));
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
        internetAccess.get(TEST_URL, headers)
            .then(LOGGER::info)
            .error(error -> LOGGER.warning("Sending request failed"));
    }

    private void createFireEffect() {
        Canvas canvas = context.getCanvas();
        Rect bounds = new Rect(0, canvas.getHeight() / 2f, canvas.getWidth(), canvas.getHeight() / 2f);
        context.attachAgent(new FireEffect(10f, 4, bounds));
    }

    /**
     * Represents one of the mario sprites that walks around the scene.
     */
    private static class Mario implements Updatable {

        private Sprite sprite;
        private Rect canvasBounds;
        private Point2D position;
        private int direction;
        private int speed;
        private boolean mask;

        public Mario(Sprite sprite, Rect canvasBounds) {
            this.sprite = sprite;
            this.position = new Point2D(RandomGenerator.getFloat(0f, canvasBounds.getWidth()),
                RandomGenerator.getFloat(0f, canvasBounds.getHeight()));
            this.canvasBounds = canvasBounds;
            this.direction = RandomGenerator.getInt(0, 4);
            this.speed = RandomGenerator.getInt(1, 4);
            this.mask = false;
        }

        @Override
        public void update(float deltaTime) {
            sprite.changeState(DIRECTIONS.get(direction));
            sprite.update(deltaTime);

            switch (direction) {
                case 0 : position.add(0, -speed); break;
                case 1 : position.add(speed, 0); break;
                case 2 : position.add(0, speed); break;
                case 3 : position.add(-speed, 0); break;
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
