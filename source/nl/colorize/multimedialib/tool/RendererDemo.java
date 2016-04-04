//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import nl.colorize.multimedialib.app.AnimationLoop;
import nl.colorize.multimedialib.app.ResourceLoader;
import nl.colorize.multimedialib.app.Scene;
import nl.colorize.multimedialib.graphics.Animation;
import nl.colorize.multimedialib.graphics.AudioData;
import nl.colorize.multimedialib.graphics.BitmapFont;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.DisplayList;
import nl.colorize.multimedialib.graphics.ImageAtlas;
import nl.colorize.multimedialib.graphics.ImageRegion;
import nl.colorize.multimedialib.graphics.Shape2D;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.Text;
import nl.colorize.multimedialib.math.Point;
import nl.colorize.multimedialib.math.Rand;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.TimeSeries;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.RenderStatistics;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.util.FormatUtils;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.Tuple;
import nl.colorize.util.animation.Animatable;

/**
 * Simple demo application that displays a number of animated Mario sprites on
 * top of a black background. The demo can be used to test both a renderer's
 * graphicss/audio/input features as well as its performance.
 * <p>
 * The demo can be configured using two ways. The first is to create a subclass
 * and provide a renderer programatically when calling {@link #startDemo(Renderer)}.
 * The second is running the demo from the command line, and specifying the
 * renderer class as a command line argument.
 */
public class RendererDemo extends CommandLineTool implements Scene {
	
	@Argument(index=0, metaVar="rendererClass", required=true, usage="Full class name for renderer")
	private String rendererClassName;
	
	@Option(name="-statsFile", required=false, usage="Output file for storing render statistics")
	private File statsFile;
	
	private ImageAtlas marioImageAtlas;
	private List<Mario> marios;
	private BitmapFont hudFont;
	private List<Button> buttons;
	private AudioData audioClip;
	private List<TouchMarker> touchMarkers;
	//TODO 
	private Text kees;
	private Shape2D leftBorder;
	private Shape2D rightBorder;
	
	private static final ResourceFile MARIO_ATLAS_FILE = new ResourceFile("mario.png");
	private static final ResourceFile MARIO_ATLAS_XML_FILE = new ResourceFile("mario.atlas.xml");
	private static final ResourceFile FONT_IMAGE_FILE = new ResourceFile("lucidagrande.font.png");
	private static final ResourceFile FONT_XML_FILE = new ResourceFile("lucidagrande.font.xml");
	private static final ResourceFile AUDIO_FILE = new ResourceFile("test.mp3");
	private static final int INITIAL_MARIOS = 10;
	private static final List<String> DIRECTIONS = ImmutableList.of("north", "east", "south", "west");
	private static final Rect BUTTON_SIZE = new Rect(0, 0, 100, 22);
	private static final int BUTTON_ALPHA = 80;
	
	private static final int CANVAS_WIDTH = 800;
	private static final int CANVAS_HEIGHT = 600;
	private static final int FRAMERATE = 30;
	
	public static void main(String[] args) {
		RendererDemo demo = new RendererDemo();
		demo.start(args);
	}
	
	public void run() {
		ScaleStrategy defaultScaleStrategy = ScaleStrategy.flexible(CANVAS_WIDTH, CANVAS_HEIGHT);
		
		try {
			Class<?> rendererClass = Class.forName(rendererClassName);
			Renderer renderer = (Renderer) rendererClass.getConstructor(ScaleStrategy.class, int.class)
					.newInstance(defaultScaleStrategy, FRAMERATE);
			startDemo(renderer);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception while initializing renderer", e);
		}
	}

	public void startDemo(Renderer renderer) {
		AnimationLoop animationLoop = new AnimationLoop(renderer, this);
		animationLoop.start();
	}
	
	public void onSceneStart(AnimationLoop animationLoop) {
		Renderer renderer = animationLoop.getRenderer();
		ResourceLoader resourceLoader = animationLoop.getResourceLoader();
		
		marioImageAtlas = resourceLoader.loadImageAtlas(MARIO_ATLAS_FILE, MARIO_ATLAS_XML_FILE);
		marios = new ArrayList<Mario>();
		addMarios(INITIAL_MARIOS, renderer.getCanvasBounds());
		
		hudFont = resourceLoader.loadBitmapFont(FONT_IMAGE_FILE, FONT_XML_FILE);
		hudFont.setLetterSpacing(0);
		
		kees = new Text("This is a text that will animate...", hudFont, renderer.getCanvasWidth() / 2, 300);
		kees.setAlignment(Text.Align.CENTER);
		kees.enableAnimation(1f);
		
		leftBorder = new Shape2D(1, 2048, ColorRGB.WHITE);
		leftBorder.setZIndex(100);
		rightBorder = new Shape2D(1, 2048, ColorRGB.WHITE);
		rightBorder.setZIndex(100);
		
		audioClip = renderer.loadAudio(AUDIO_FILE);
		
		touchMarkers = new ArrayList<TouchMarker>();
		
		ColorRGB red = new ColorRGB(228, 93, 97);
		ColorRGB gray = new ColorRGB(173, 173, 173);
		ColorRGB green = ColorRGB.parseHex("#72A725");
		
		buttons = new ArrayList<Button>();
		addButton("Add sprite", red, renderer);
		addButton("Remove sprite", red, renderer);
		addButton("Flexible scaling", gray, renderer);
		addButton("Fixed scaling", gray, renderer);
		addButton("Stretch scaling", gray, renderer);
		addButton("Prop. scaling", gray, renderer);
		addButton("Smart scaling", gray, renderer);
		addButton("Play sound", green, renderer);
	}
	
	private void handleButtonClick(int index, Renderer renderer) {
		switch (index) {
			case 0 : addMarios(1, renderer.getCanvasBounds()); break;
			case 1 : removeMarios(1); break;
			case 2 : renderer.setScaleStrategy(ScaleStrategy.flexible(CANVAS_WIDTH, CANVAS_HEIGHT)); break;
			case 3 : renderer.setScaleStrategy(ScaleStrategy.fixed(CANVAS_WIDTH, CANVAS_HEIGHT)); break;
			case 4 : renderer.setScaleStrategy(ScaleStrategy.stretch(CANVAS_WIDTH, CANVAS_HEIGHT)); break;
			case 5 : renderer.setScaleStrategy(ScaleStrategy.proportional(CANVAS_WIDTH, CANVAS_HEIGHT)); break;
			case 6 : renderer.setScaleStrategy(ScaleStrategy.smart(CANVAS_WIDTH, CANVAS_HEIGHT)); break;
			case 7 : renderer.getAudioQueue().play(audioClip); break;
			default : return;
		}
	}

	private void addButton(String label, ColorRGB backgroundColor, Renderer renderer) {
		int x = renderer.getCanvasWidth() - BUTTON_SIZE.getWidth() / 2 - 20;
		int y = 50 + buttons.size() * (BUTTON_SIZE.getHeight() + 5);
		buttons.add(new Button(label, hudFont, x, y, backgroundColor));
	}

	public void onFrame(AnimationLoop animationLoop, float deltaTime) {
		Renderer renderer = animationLoop.getRenderer();
		updateGraphics(deltaTime, renderer);
		
		InputDevice inputDevice = renderer.getInputDevice();
		handleInput(inputDevice, renderer);
	}

	private void updateGraphics(float deltaTime, Renderer renderer) {
		for (Mario mario : marios) {
			mario.onFrame(deltaTime);
		}
		kees.onFrame(deltaTime);
		
		for (Button button : buttons) {
			button.background.setX(renderer.getCanvasWidth() - BUTTON_SIZE.getWidth() / 2 - 20);
			button.foreground.setX(renderer.getCanvasWidth() - BUTTON_SIZE.getWidth() / 2 - 20);
		}
		
		leftBorder.setX(1);
		rightBorder.setX(renderer.getCanvasWidth() - 2);
		
		for (TouchMarker touchMarker : touchMarkers) {
			int alpha = touchMarker.marker.getTransform().getAlpha();
			touchMarker.marker.getTransform().setAlpha(alpha - 2);
		}
	}

	private void handleInput(InputDevice inputDevice, Renderer renderer) {
		if (inputDevice.isPointerReleased()) {
			for (int i = 0; i < buttons.size(); i++) {
				if (buttons.get(i).background.getBounds().contains(inputDevice.getPointer())) {
					handleButtonClick(i, renderer);
					return;
				}
			}
			
			touchMarkers.add(new TouchMarker(inputDevice.getPointer(), hudFont));
		}
	}

	@SuppressWarnings("deprecation")
	public DisplayList onRender(AnimationLoop animationLoop) {
		DisplayList displayList = new DisplayList();
		for (Mario mario : marios) {
			displayList.add(mario.sprite);
		}
		
		for (Button button : buttons) {
			displayList.add(button.background);
			displayList.add(button.foreground);
		}
		
		for (TouchMarker touchMarker : touchMarkers) {
			displayList.add(touchMarker.marker);
		}
		
		Renderer renderer = animationLoop.getRenderer();
		RenderStatistics stats = animationLoop.getRenderStats();
		displayList.add(new Text("Display: " + renderer.getCanvasWidth() + "x" + renderer.getCanvasHeight(), 
				hudFont, 20, 20));
		displayList.add(new Text("Framerate: " + FormatUtils.numberFormat(stats.getFramerate(), 1), 
				hudFont, 20, 40));
		displayList.add(new Text("Memory usage: " + FormatUtils.memoryFormat(stats.getMemoryUsage(), 1) + 
				" (GC: " + Math.round(stats.getTimeSinceGC()) + "s)", hudFont, 20, 60));
		displayList.add(new Text("# Sprites: " + marios.size(), hudFont, 20, 80));
		displayList.add(kees);
		
		displayList.add(leftBorder);
		displayList.add(rightBorder);
		
		return displayList;
	}

	public void onSceneEnd(AnimationLoop animationLoop) {
		if (statsFile != null) {
			saveRendererStats(animationLoop.getRenderStats());
		}
	}
	
	public void addMarios(int amount, Rect canvasBounds) {
		for (int i = 0; i < amount; i++) {
			Sprite marioSprite = createMarioSprite();
			marios.add(new Mario(marioSprite, canvasBounds));
		}
	}
	
	private Sprite createMarioSprite() {
		Sprite marioSprite = new Sprite();
		for (String direction : DIRECTIONS) {
			List<ImageRegion> frames = marioImageAtlas.getSubImages(direction + "_0", 
					direction + "_1", direction + "_2", direction + "_3", direction + "_4");
			Animation anim = new Animation(frames, 0.1f, true);
			marioSprite.addState(direction, anim);
		}
		return marioSprite;
	}
	
	public void removeMarios(int amount) {
		for (int i = 0; i < amount && !marios.isEmpty(); i++) {
			marios.remove(marios.size() - 1);
		}
	}
	
	private void saveRendererStats(RenderStatistics renderStats) {
		if (statsFile.exists()) {
			throw new IllegalArgumentException("Statistics file already exists");
		}
		
		float timeRunning = renderStats.getTimeRunning();
		TimeSeries frameTimes = renderStats.getFrameTimeSeries();
		
		try {
			PrintWriter statsWriter = new PrintWriter(statsFile, Charsets.UTF_8.displayName());
			statsWriter.println("Time,FrameTime");
			for (Tuple<Long, Float> dataPoint : frameTimes.getDataPointTuples()) {
				statsWriter.printf("%.3f,%.3f\n", dataPoint.getLeft() / 1000f, dataPoint.getRight());
			}
			statsWriter.close();
		
			LOGGER.info("Saved render statistics to " + statsFile.getAbsolutePath());
			LOGGER.info(String.format("Time running: %.1fs", timeRunning));
			LOGGER.info(String.format("Frame time: %.3fs (min %.3fs, max %.3fs)", 
					frameTimes.getAverage(), frameTimes.getMin(), frameTimes.getMax()));
		} catch (IOException e) {
			LOGGER.warning("Could not save render statistics to " + statsFile.getAbsolutePath());
		}
	}

	/**
	 * Represents one of the mario sprites that walks around the scene.
	 */
	private static class Mario implements Animatable {
		
		private Sprite sprite;
		private Rect canvasBounds;
		private int direction;
		private int speed;
		
		public Mario(Sprite sprite, Rect canvasBounds) {
			this.sprite = sprite;
			this.sprite.setX(Rand.nextInt(0, canvasBounds.getWidth()));
			this.sprite.setY(Rand.nextInt(0, canvasBounds.getHeight()));
			
			this.canvasBounds = canvasBounds;
			this.direction = Rand.nextInt(0, 4);
			this.speed = Rand.nextInt(1, 4);
		}

		public void onFrame(float deltaTime) {
			sprite.changeState(DIRECTIONS.get(direction));
			sprite.onFrame(deltaTime);
			
			switch (direction) {
				case 0 : sprite.move(0, -speed); break;
				case 1 : sprite.move(speed, 0); break;
				case 2 : sprite.move(0, speed); break;
				case 3 : sprite.move(-speed, 0); break;
				default : throw new AssertionError();
			}
			
			if (sprite.getX() < 0 || sprite.getX() > canvasBounds.getWidth() ||
					sprite.getY() < 0 || sprite.getY() > canvasBounds.getHeight()) {
				direction = (direction + 2) % 4;
			}
		}
	}
	
	/**
	 * Represents one of the buttons that can be used to interact with the demo.
	 */
	private static class Button {
		
		private Shape2D background;
		private Text foreground;
		
		public Button(String label, BitmapFont labelFont, int x, int y, ColorRGB backgroundColor) {
			background = new Shape2D(BUTTON_SIZE.getWidth(), BUTTON_SIZE.getHeight(), backgroundColor);
			background.setPosition(x, y);
			background.getTransform().setAlpha(BUTTON_ALPHA);
			
			foreground = new Text(label, labelFont, x, y + 5);
			foreground.setAlignment(Text.Align.CENTER);
		}
	}
	
	/**
	 * Shows the coordinates when the screen has been touched or clicked anywhere
	 * that isn't a button.
	 */
	private static class TouchMarker {
		
		private Text marker;
		
		public TouchMarker(Point location, BitmapFont font) {
			marker = new Text(location.getX() + ", " + location.getY(), font,
					location.getX(), location.getY());
		}
	}
}
