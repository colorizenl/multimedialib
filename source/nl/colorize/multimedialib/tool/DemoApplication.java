//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.graphics.Align;
import nl.colorize.multimedialib.graphics.Animation;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.SpriteSheet;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Point;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.RandomGenerator;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.GraphicsContext;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.scene.Application;
import nl.colorize.multimedialib.scene.Button;
import nl.colorize.multimedialib.scene.Effect;
import nl.colorize.multimedialib.scene.EffectManager;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SelectBox;
import nl.colorize.multimedialib.scene.Subsystem;
import nl.colorize.multimedialib.scene.TextField;
import nl.colorize.multimedialib.scene.Updatable;
import nl.colorize.util.LogHelper;
import nl.colorize.util.animation.Timeline;

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
public class DemoApplication implements Scene {

    private Renderer renderer;
    private Application app;

    private SpriteSheet marioSpriteSheet;
    private TTFont font;
    private List<Mario> marios;
    private Audio audioClip;
    private Transform shapeTransform;
    private EffectManager effectManager;
    private boolean canvasMask;
    private List<Subsystem> uiWidgets;

    public static final int DEFAULT_CANVAS_WIDTH = 800;
    public static final int DEFAULT_CANVAS_HEIGHT = 600;
    public static final int DEFAULT_FRAMERATE = 60;

    private static final FilePointer MARIO_SPRITES_FILE = new FilePointer("mario.png");
    private static final FilePointer AUDIO_FILE = new FilePointer("test.mp3");
    private static final int INITIAL_MARIOS = 20;
    private static final List<String> DIRECTIONS = ImmutableList.of("north", "east", "south", "west");
    private static final int NUM_BUTTONS = 7;
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 25;
    private static final ColorRGB RED_BUTTON = new ColorRGB(228, 93, 97);
    private static final ColorRGB GREEN_BUTTON = ColorRGB.parseHex("#72A725");
    private static final ColorRGB SHAPE_COLOR = new ColorRGB(200, 0, 0);
    private static final ColorRGB BACKGROUND_COLOR = ColorRGB.parseHex("#343434");
    private static final Transform MASK_TRANSFORM = Transform.withMask(ColorRGB.WHITE);
    private static final Logger LOGGER = LogHelper.getLogger(DemoApplication.class);

    public DemoApplication(Application app) {
        this.app = app;
        this.renderer = app.getRenderer();
    }

    @Override
    public void start() {
        MediaLoader mediaLoader = renderer.getMediaLoader();

        initMarioSprites(mediaLoader);
        marios = new ArrayList<>();
        addMarios(INITIAL_MARIOS);

        font = mediaLoader.loadDefaultFont().derive(ColorRGB.WHITE);
        audioClip = mediaLoader.loadAudio(AUDIO_FILE);
        shapeTransform = new Transform();
        effectManager = new EffectManager();
        uiWidgets = Collections.emptyList();
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

    @Override
    public void update(float deltaTime) {
        InputDevice inputDevice = renderer.getInputDevice();
        if (inputDevice.isPointerReleased()) {
            handleClick(inputDevice);
        }

        for (Mario mario : marios) {
            mario.update(deltaTime);
        }

        effectManager.update(deltaTime);
        uiWidgets.forEach(widget -> widget.update(deltaTime));
    }

    private void handleClick(InputDevice inputDevice) {
        for (int i = 0; i <= NUM_BUTTONS; i++) {
            if (isButtonClicked(i)) {
                handleButtonClick(i);
                return;
            }
        }

        for (Mario mario : marios) {
            if (mario.contains(inputDevice.getPointer())) {
                mario.mask = !mario.mask;
                return;
            }
        }

        Point pointer = inputDevice.getPointer();
        if (pointer.getX() <= 100 && pointer.getY() >= renderer.getCanvas().getHeight() - 100) {
            randomizeShapeTransform();
            return;
        }

        createTouchMarker(pointer);
    }

    private boolean isButtonClicked(int buttonIndex) {
        Rect buttonBounds = new Rect(renderer.getCanvas().getWidth() - BUTTON_WIDTH,
            buttonIndex * 30, BUTTON_WIDTH, BUTTON_HEIGHT);
        return buttonBounds.contains(renderer.getInputDevice().getPointer());
    }

    private void handleButtonClick(int index) {
        switch (index) {
            case 0 : addMarios(10); break;
            case 1 : removeMarios(10); break;
            case 2 : audioClip.play(); break;
            case 3 : canvasMask = !canvasMask; break;
            case 4 : initUIWidgets(); break;
            default : break;
        }
    }

    private void initUIWidgets() {
        if (!uiWidgets.isEmpty()) {
            uiWidgets = Collections.emptyList();
            return;
        }

        Button button = new Button(new Rect(200, 200, 200, 30), ColorRGB.RED, "Click", font);
        button.setClickHandler(app.getInputDevice(), () -> LOGGER.info("Button clicked"));

        SelectBox select = new SelectBox(new Rect(200, 240, 200, 30),
            ImmutableList.of("A", "B", "C"), "A", ColorRGB.BLUE, font);
        select.setClickHandler(app.getInputDevice(), item -> LOGGER.info("Selected item " + item));

        TextField textField = new TextField(new Rect(200, 280, 200, 30), ColorRGB.WHITE,
            font.derive(ColorRGB.BLACK), "Enter text:");
        textField.setChangeHandler(app.getInputDevice(), text -> LOGGER.info("Entered text: " + text));

        uiWidgets = ImmutableList.of(button, select, textField);
    }

    private void randomizeShapeTransform() {
        if (Math.random() >= 0.5) {
            shapeTransform.setRotation(shapeTransform.getRotation() + 45);
        } else {
            shapeTransform.setScale(shapeTransform.getScaleX() + 10, shapeTransform.getScaleX() + 10);
        }
    }

    private void createTouchMarker(Point position) {
        Timeline timeline = new Timeline();
        timeline.addKeyFrame(0f, 100f);
        timeline.addKeyFrame(1f, 100f);
        timeline.addKeyFrame(1.5f, 0f);

        String text = Math.round(position.getX()) + ", " + Math.round(position.getY());

        Effect effect = Effect.forTextAlpha(text, font, Align.LEFT, timeline);
        effect.setPosition(position);
        effectManager.play(effect);
    }

    @Override
    public void render(GraphicsContext graphics) {
        graphics.drawBackground(BACKGROUND_COLOR);
        drawSprites(graphics);
        drawHUD(graphics);
        if (canvasMask) {
            graphics.drawRect(new Rect(10f, 10f, DEFAULT_CANVAS_WIDTH - 20f, DEFAULT_CANVAS_HEIGHT - 20f),
                ColorRGB.WHITE, Transform.withAlpha(10));
        }
        effectManager.render(graphics);
        uiWidgets.forEach(widget -> widget.render(graphics));
    }

    private void drawSprites(GraphicsContext graphics) {
        for (Mario mario : marios) {
            graphics.drawSprite(mario.sprite, Math.round(mario.position.getX()),
                Math.round(mario.position.getY()), mario.mask ? MASK_TRANSFORM : null);
        }

        graphics.drawRect(new Rect(10, renderer.getCanvas().getHeight() - 110, 100, 100),
            SHAPE_COLOR, shapeTransform);
        Polygon circle = Polygon.createCircle(10, renderer.getCanvas().getHeight() - 110, 20f, 16);
        graphics.drawPolygon(circle, ColorRGB.WHITE, Transform.withAlpha(50));
    }

    private void drawHUD(GraphicsContext graphics) {
        drawButton(graphics, "Add sprites", RED_BUTTON, 0);
        drawButton(graphics, "Remove sprites", RED_BUTTON, 30);
        drawButton(graphics, "Play sound", GREEN_BUTTON, 60);
        drawButton(graphics, "Canvas bounds", GREEN_BUTTON, 90);
        drawButton(graphics, "UI widgets", GREEN_BUTTON, 120);

        Canvas canvas = renderer.getCanvas();

        graphics.drawText("Canvas:  " + canvas, font, 20, 20);
        graphics.drawText("Framerate:  " + Math.round(app.getAverageFPS()), font, 20, 40);
        graphics.drawText("Frame time:  " + Math.round(app.getAverageFrameTime()) + "ms",
            font, 20, 60);
        graphics.drawText("Sprites:  " + marios.size(), font, 20, 80);
    }

    private void drawButton(GraphicsContext graphics, String label, ColorRGB background, int y) {
        graphics.drawRect(new Rect(renderer.getCanvas().getWidth() - BUTTON_WIDTH - 2, y + 2,
            BUTTON_WIDTH, BUTTON_HEIGHT), background, null);
        graphics.drawText(label, font, renderer.getCanvas().getWidth() - BUTTON_WIDTH / 2f, y + 17,
            Align.CENTER);
    }

    public void addMarios(int amount) {
        for (int i = 0; i < amount; i++) {
            Sprite marioSprite = createMarioSprite();
            marios.add(new Mario(marioSprite,
                new Rect(0, 0, renderer.getCanvas().getWidth(), renderer.getCanvas().getHeight())));
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

    /**
     * Represents one of the mario sprites that walks around the scene.
     */
    private static class Mario implements Updatable {

        private Sprite sprite;
        private Rect canvasBounds;
        private Point position;
        private int direction;
        private int speed;
        private boolean mask;

        public Mario(Sprite sprite, Rect canvasBounds) {
            this.sprite = sprite;
            this.position = new Point(RandomGenerator.getFloat(0f, canvasBounds.getWidth()),
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

        private boolean contains(Point p) {
            Rect bounds = new Rect(position.getX() - sprite.getCurrentWidth() / 2f,
                position.getY() - sprite.getCurrentHeight() / 2f,
                sprite.getCurrentWidth(), sprite.getCurrentHeight());
            return bounds.contains(p);
        }
    }
}
