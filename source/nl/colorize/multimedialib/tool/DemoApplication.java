//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.graphics.Alignment;
import nl.colorize.multimedialib.graphics.Animation;
import nl.colorize.multimedialib.graphics.Audio;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.ImageAtlas;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.graphics.TrueTypeFont;
import nl.colorize.multimedialib.math.Point;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.RandomGenerator;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.GraphicsContext;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.Updatable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Simple demo application that displays a number of animated Mario sprites on
 * top of a black background. The demo application serves two purposes. First,
 * it can be used as an example when using the framework to implement applications.
 * Second, it can be used for verification purposes to determine if a new platform
 * is fully supported.
 * <p>
 * The demo application can be started from the command line using the
 * {@link DemoLauncher}. It can also be embedded in applications by embedding
 * this class directly.
 */
public class DemoApplication implements Scene {

    private Renderer renderer;
    private SceneContext sceneContext;

    private ImageAtlas marioImageAtlas;
    private TrueTypeFont font;
    private List<Mario> marios;
    private Audio audioClip;
    private List<TouchMarker> touchMarkers;
    private Transform shapeTransform;
    private boolean mask;

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
    private static final int TOUCH_MARKER_AGE = 120;
    private static final ColorRGB BACKGROUND_COLOR = ColorRGB.parseHex("#343434");
    private static final Transform MASK_TRANSFORM = Transform.withMask(ColorRGB.WHITE);

    public DemoApplication(Renderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void start(SceneContext sceneContext) {
        this.sceneContext = sceneContext;

        MediaLoader mediaLoader = sceneContext.getMediaLoader();

        initMarioSprites(mediaLoader);
        marios = new ArrayList<>();
        addMarios(INITIAL_MARIOS);

        font = mediaLoader.loadDefaultFont().derive(ColorRGB.WHITE);
        audioClip = mediaLoader.loadAudio(AUDIO_FILE);
        touchMarkers = new ArrayList<>();
        shapeTransform = new Transform();
    }

    private void initMarioSprites(MediaLoader mediaLoader) {
        Image image = mediaLoader.loadImage(MARIO_SPRITES_FILE);
        marioImageAtlas = new ImageAtlas(image);

        int y = 0;
        for (String direction : ImmutableList.of("north", "east", "south", "west")) {
            for (int i = 0; i <= 4; i++) {
                marioImageAtlas.markSubImage(direction + "_" + i, new Rect(i * 48, y, 48, 64));
            }
            y += 64;
        }
    }

    @Override
    public void update(float deltaTime) {
        handleInput(renderer.getInputDevice());
        updatePositions(deltaTime);
    }

    private void handleInput(InputDevice inputDevice) {
        if (inputDevice.isPointerReleased()) {
            for (int i = 0; i <= NUM_BUTTONS; i++) {
                if (isButtonClicked(i)) {
                    handleButtonClick(i);
                    return;
                }
            }

            Point pointer = inputDevice.getPointer();
            if (pointer.getX() <= 100 && pointer.getY() >= renderer.getCanvas().getHeight() - 100) {
                randomizeShapeTransform();
                return;
            }

            touchMarkers.add(new TouchMarker(pointer.getX(), pointer.getY()));
        }
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
            default : break;
        }
    }

    private void updatePositions(float deltaTime) {
        for (Mario mario : marios) {
            mario.update(deltaTime);
        }

        Iterator<TouchMarker> touchMarkerIterator = touchMarkers.iterator();
        while (touchMarkerIterator.hasNext()) {
            TouchMarker touchMarker = touchMarkerIterator.next();
            touchMarker.age++;
            if (touchMarker.age >= TOUCH_MARKER_AGE) {
                touchMarkerIterator.remove();
            }
        }
    }

    private void randomizeShapeTransform() {
        if (Math.random() >= 0.5) {
            shapeTransform.setRotation(shapeTransform.getRotation() + 45);
        } else {
            shapeTransform.setScale(shapeTransform.getScaleX() + 10, shapeTransform.getScaleX() + 10);
        }
    }

    @Override
    public void render(GraphicsContext context) {
        context.drawBackground(BACKGROUND_COLOR);
        drawSprites(context);
        drawHUD(context);
    }

    private void drawSprites(GraphicsContext context) {
        for (Mario mario : marios) {
            context.drawSprite(mario.sprite, Math.round(mario.position.getX()),
                Math.round(mario.position.getY()), mask ? MASK_TRANSFORM : null);
        }

        context.drawRect(new Rect(10, renderer.getCanvas().getHeight() - 110, 100, 100),
            SHAPE_COLOR, shapeTransform);
        Polygon circle = Polygon.createCircle(10, renderer.getCanvas().getHeight() - 110, 20f, 16);
        context.drawPolygon(circle, ColorRGB.WHITE, Transform.withAlpha(50));

        for (TouchMarker touchMarker : touchMarkers) {
            context.drawText(touchMarker.text, font, (int) touchMarker.location.getX(),
                (int) touchMarker.location.getY(), Alignment.LEFT);
        }
    }

    private void drawHUD(GraphicsContext context) {
        drawButton(context, "Add sprites", RED_BUTTON, 0);
        drawButton(context, "Remove sprites", RED_BUTTON, 30);
        drawButton(context, "Play sound", GREEN_BUTTON, 60);

        Canvas canvas = renderer.getCanvas();

        context.drawText(String.format("Canvas:  %dx%d @ %dx", canvas.getWidth(), canvas.getHeight(),
            Math.round(canvas.getZoomLevel())), font, 20, 20, Alignment.LEFT);
        context.drawText("Framerate:  " + Math.round(sceneContext.getAverageFPS()), font, 20, 40,
            Alignment.LEFT);
        context.drawText("Frame time:  " + Math.round(sceneContext.getAverageFrameTime()) + "ms",
            font, 20, 60, Alignment.LEFT);
        context.drawText("Sprites:  " + marios.size(), font, 20, 80,
            Alignment.LEFT);
    }

    private void drawButton(GraphicsContext context, String label, ColorRGB background, int y) {
        context.drawRect(new Rect(renderer.getCanvas().getWidth() - BUTTON_WIDTH - 2, y + 2,
            BUTTON_WIDTH, BUTTON_HEIGHT), background, null);
        context.drawText(label, font, renderer.getCanvas().getWidth() - BUTTON_WIDTH / 2f, y + 17,
            Alignment.CENTER);
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
            List<Image> frames = marioImageAtlas.getSubImages(direction + "_0",
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

        public Mario(Sprite sprite, Rect canvasBounds) {
            RandomGenerator random = new RandomGenerator();

            this.sprite = sprite;
            this.position = new Point(random.getFloat(0f, canvasBounds.getWidth()),
                random.getFloat(0f, canvasBounds.getHeight()));
            this.canvasBounds = canvasBounds;
            this.direction = random.getInt(0, 4);
            this.speed = random.getInt(1, 4);
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
    }

    /**
     * Shows the coordinates when the screen has been touched or clicked anywhere
     * that isn't a button.
     */
    private static class TouchMarker {

        private String text;
        private Point location;
        private int age;

        public TouchMarker(float x, float y) {
            this.text = Math.round(x) + ", " + Math.round(y);
            this.location = new Point(x, y);
        }
    }
}
