//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import nl.colorize.multimedialib.graphics.Animation;
import nl.colorize.multimedialib.graphics.Audio;
import nl.colorize.multimedialib.graphics.BitmapFont;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.ImageAtlas;
import nl.colorize.multimedialib.graphics.ImageAtlasLoader;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rand;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.RenderContext;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.multimedialib.renderer.java2d.Java2DRenderer;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneManager;
import nl.colorize.util.Formatting;
import nl.colorize.util.LogHelper;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.animation.Animatable;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Simple demo application that displays a number of animated Mario sprites on
 * top of a black background. The demo can be used to test both a renderer's
 * graphicss/audio/input features as well as its performance.
 * <p>
 * The demo can be configured using two ways. The first is to create a subclass
 * and provide a renderer programatically, the second is running the demo from
 * the command line, and specifying the renderer class as a command line argument.
 */
public class RendererDemo extends CommandLineTool implements Scene {
    
    @Argument(index=0, metaVar="rendererClass", required=true, usage="Full class name for renderer")
    private String rendererClassName;
    
    @Option(name="-statsFile", required=false, usage="Output file for storing render statistics")
    private File statsFile;

    private Renderer renderer;
    private ImageAtlas marioImageAtlas;
    private List<Mario> marios;
    private BitmapFont hudFont;
    private Audio audioClip;
    private List<TouchMarker> touchMarkers;
    private Transform shapeTransform;

    private static final int CANVAS_WIDTH = 800;
    private static final int CANVAS_HEIGHT = 600;
    private static final int FRAMERATE = 60;

    private static final ResourceFile MARIO_ATLAS_FILE = new ResourceFile("mario.png");
    private static final ResourceFile MARIO_ATLAS_XML_FILE = new ResourceFile("mario.atlas.xml");
    private static final ResourceFile FONT_IMAGE_FILE = new ResourceFile("lucidagrande.font.png");
    private static final ResourceFile FONT_XML_FILE = new ResourceFile("lucidagrande.font.xml");
    private static final ResourceFile AUDIO_FILE = new ResourceFile("test.mp3");
    private static final int INITIAL_MARIOS = 20;
    private static final List<String> DIRECTIONS = ImmutableList.of("north", "east", "south", "west");
    private static final int NUM_BUTTONS = 7;
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 22;
    private static final ColorRGB RED_BUTTON = new ColorRGB(228, 93, 97);
    private static final ColorRGB GRAY_BUTTON = new ColorRGB(173, 173, 173);
    private static final ColorRGB GREEN_BUTTON = ColorRGB.parseHex("#72A725");
    private static final ColorRGB SHAPE_COLOR = new ColorRGB(200, 0, 0);
    private static final int TOUCH_MARKER_AGE = 120;
    private static final Logger LOGGER = LogHelper.getLogger(RendererDemo.class);

    public static void main(String[] args) {
        RendererDemo demo = new RendererDemo();
        demo.start(args);
    }

    @Override
    public void run() {
        ScaleStrategy scaleStrategy = ScaleStrategy.flexible(CANVAS_WIDTH, CANVAS_HEIGHT);
        renderer = createRenderer(scaleStrategy, FRAMERATE);
        renderer.registerCallback(new SceneManager(renderer, this));
        renderer.initialize();
    }

    private Renderer createRenderer(ScaleStrategy scaling, int framerate) {
        if (rendererClassName == null || rendererClassName.isEmpty()) {
            return new Java2DRenderer(scaling, framerate);
        }

        try {
            Class<?> rendererClass = Class.forName(rendererClassName);
            return (Renderer) rendererClass.getConstructor(ScaleStrategy.class, int.class)
                    .newInstance(scaling, FRAMERATE);
        } catch (Exception e) {
            throw new IllegalArgumentException("Exception while initializing renderer", e);
        }
    }

    @Override
    public void onSceneStart(MediaLoader mediaLoader) {
        ImageAtlasLoader imageAtlasLoader = new ImageAtlasLoader(mediaLoader);

        marioImageAtlas = imageAtlasLoader.load(MARIO_ATLAS_FILE, MARIO_ATLAS_XML_FILE);
        marios = new ArrayList<>();
        addMarios(INITIAL_MARIOS);
        
        hudFont = imageAtlasLoader.loadBitmapFont(FONT_IMAGE_FILE, FONT_XML_FILE);
        audioClip = mediaLoader.loadAudio(AUDIO_FILE);
        touchMarkers = new ArrayList<>();
        shapeTransform = new Transform();
    }
    
    private void handleButtonClick(int index, Renderer renderer) {
        Map<Integer, ScaleStrategy> scaleStrategies = ImmutableMap.of(
            2, ScaleStrategy.flexible(CANVAS_WIDTH, CANVAS_HEIGHT),
            3, ScaleStrategy.fixed(CANVAS_WIDTH, CANVAS_HEIGHT),
            4, ScaleStrategy.stretch(CANVAS_WIDTH, CANVAS_HEIGHT),
            5, ScaleStrategy.proportional(CANVAS_WIDTH, CANVAS_HEIGHT),
            6, ScaleStrategy.smart(CANVAS_WIDTH, CANVAS_HEIGHT));

        switch (index) {
            case 0 : addMarios(10); break;
            case 1 : removeMarios(10); break;
            case 7 : audioClip.play(); break;
            default : attemptScaleStrategyChange(scaleStrategies.get(index)); break;
        }
    }

    private void attemptScaleStrategyChange(ScaleStrategy scaleStrategy) {
        if (renderer instanceof Java2DRenderer) {
            ((Java2DRenderer) renderer).setScaleStrategy(scaleStrategy);
        } else {
            LOGGER.info("Scale strategy change not supported by renderer " +
                    renderer.getClass().getSimpleName());
        }
    }

    @Override
    public void onFrame(float deltaTime, InputDevice input) {
        updateGraphics(deltaTime, renderer);
        
        handleInput(input, renderer);
    }

    private void updateGraphics(float deltaTime, Renderer renderer) {
        for (Mario mario : marios) {
            mario.onFrame(deltaTime);
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

    private void handleInput(InputDevice input, Renderer renderer) {
        if (input.isPointerReleased()) {
            for (int i = 0; i <= NUM_BUTTONS; i++) {
                if (isButtonClicked(input, i)) {
                    handleButtonClick(i, renderer);
                    return;
                }
            }

            Point2D pointer = input.getPointer();
            if (pointer.getX() <= 100 && pointer.getY() >= renderer.getCanvasHeight() - 100) {
                randomizeShapeTransform();
                return;
            }
            
            touchMarkers.add(new TouchMarker(pointer.getX(), pointer.getY()));
        }
    }

    private boolean isButtonClicked(InputDevice inputDevice, int buttonIndex) {
        Rect buttonBounds = new Rect(renderer.getCanvasWidth() - BUTTON_WIDTH, buttonIndex * 25,
                BUTTON_WIDTH, BUTTON_HEIGHT);
        return buttonBounds.contains(inputDevice.getPointer());
    }

    private void randomizeShapeTransform() {
        if (Math.random() >= 0.5) {
            shapeTransform.setRotation(shapeTransform.getRotation() + 45);
        } else {
            shapeTransform.setScale(shapeTransform.getScaleX() + 10, shapeTransform.getScaleX() + 10);
        }
    }

    @Override
    public void onRender(RenderContext context) {
        context.drawBackground(ColorRGB.BLACK);
        drawSprites(context);
        drawHUD(context);
    }

    private void drawSprites(RenderContext context) {
        for (Mario mario : marios) {
            context.drawSprite(mario.sprite, Math.round(mario.position.getX()),
                Math.round(mario.position.getY()), null);
        }

        context.drawRect(new Rect(10, renderer.getCanvasHeight() - 110, 100, 100),
                SHAPE_COLOR, shapeTransform);

        for (TouchMarker touchMarker : touchMarkers) {
            context.drawText(touchMarker.text, hudFont, (int) touchMarker.location.getX(),
                (int) touchMarker.location.getY());
        }
    }

    private void drawHUD(RenderContext context) {
        drawButton(context, "Add sprites", RED_BUTTON, 0);
        drawButton(context, "Remove sprites", RED_BUTTON, 25);
        drawButton(context, "Flexible scaling", GRAY_BUTTON, 50);
        drawButton(context, "Fixed scaling", GRAY_BUTTON, 75);
        drawButton(context, "Stretch scaling", GRAY_BUTTON, 100);
        drawButton(context, "Prop. scaling", GRAY_BUTTON, 125);
        drawButton(context, "Smart scaling", GRAY_BUTTON, 150);
        drawButton(context, "Play sound", GREEN_BUTTON, 175);

        context.drawText("Canvas: " + renderer.getCanvasWidth() + "x" + renderer.getCanvasHeight(),
                hudFont, 20, 20);
        context.drawText("Framerate: " + Formatting.numberFormat(renderer.getStats().getAverageFPS(), 1)
                + " / " + renderer.getTargetFramerate(), hudFont, 20, 40);
        context.drawText("# Sprites: " + marios.size(), hudFont, 20, 60);
    }

    private void drawButton(RenderContext context, String label, ColorRGB background, int y) {
        context.drawRect(new Rect(renderer.getCanvasWidth() - BUTTON_WIDTH - 2, y + 2,
                BUTTON_WIDTH, BUTTON_HEIGHT), background, null);
        context.drawText(label, hudFont, renderer.getCanvasWidth() - BUTTON_WIDTH + 5, y + 17);
    }

    @Override
    public void onSceneEnd() {
    }
    
    public void addMarios(int amount) {
        for (int i = 0; i < amount; i++) {
            Sprite marioSprite = createMarioSprite();
            marios.add(new Mario(marioSprite,
                    new Rect(0, 0, renderer.getCanvasWidth(), renderer.getCanvasHeight())));
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
    private static class Mario implements Animatable {
        
        private Sprite sprite;
        private Rect canvasBounds;
        private Point2D position;
        private int direction;
        private int speed;
        
        public Mario(Sprite sprite, Rect canvasBounds) {
            this.sprite = sprite;
            this.position = new Point2D(Rand.nextInt(0, canvasBounds.getWidth()),
                    Rand.nextInt(0, canvasBounds.getHeight()));
            this.canvasBounds = canvasBounds;
            this.direction = Rand.nextInt(0, 4);
            this.speed = Rand.nextInt(1, 4);
        }

        @Override
        public void onFrame(float deltaTime) {
            sprite.changeState(DIRECTIONS.get(direction));
            sprite.onFrame(deltaTime);
            
            switch (direction) {
                case 0 : position.set(0, -speed); break;
                case 1 : position.set(speed, 0); break;
                case 2 : position.set(0, speed); break;
                case 3 : position.set(-speed, 0); break;
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
        private Point2D location;
        private int age;

        public TouchMarker(float x, float y) {
            this.text = x + ", " + y;
            this.location = new Point2D(x, y);
        }
    }
}
