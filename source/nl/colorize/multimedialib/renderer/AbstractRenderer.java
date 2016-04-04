//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import nl.colorize.multimedialib.graphics.AudioData;
import nl.colorize.multimedialib.graphics.BitmapFont;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.ImageRegion;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.Text;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.util.ResourceFile;

/**
 * Base implementation of the {@link Renderer} interface that provides default
 * implementations for all non-graphical methods.
 * <p>
 * This class implements the {@link RenderCallback} interface so that it can be
 * used as a callback for {@link TimingUtils}. By default all methods from this
 * interface will forward to the corresponding methods from the renderer's own
 * callbacks.
 */
public abstract class AbstractRenderer implements Renderer, RenderCallback {
	
	protected AtomicBoolean active;
	protected List<RenderCallback> callbacks;
	protected RenderStatistics renderStats;
	
	protected ScaleStrategy scaleStrategy;
	protected int targetFramerate;
	protected ColorRGB backgroundColor;
	
	public AbstractRenderer(ScaleStrategy scaleStrategy, int targetFramerate) {
		this.active = new AtomicBoolean(false);
		this.callbacks = new ArrayList<RenderCallback>();
		this.renderStats = new RenderStatistics();
		
		this.scaleStrategy = scaleStrategy;
		this.targetFramerate = targetFramerate;
		this.backgroundColor = ColorRGB.BLACK;
	}
	
	public final void initialize() {
		if (active.get()) {
			throw new RendererException("Renderer is already running");
		}
		active.set(true);
		startRenderer();
		onDisplayChanged();
	}
	
	protected abstract void startRenderer();
	
	public void stop() {
		if (active.get()) {
			getAudioQueue().stopAll();
			stopRenderer();
			active.set(false);
		}
	}
	
	protected abstract void stopRenderer();
	
	public boolean isActive() {
		return active.get();
	}
	
	public void onInitialized() {
		for (RenderCallback callback : callbacks) {
			callback.onInitialized();
		}
	}
	
	public void onFrame(float deltaTime) {
		for (RenderCallback callback : callbacks) {
			callback.onFrame(deltaTime);
		}
	}
	
	public void onStopped() {
		for (RenderCallback callback : callbacks) {
			callback.onStopped();
		}
	}
	
	public void registerCallback(RenderCallback callback) {
		callbacks.add(callback);
	}
	
	public void unregisterCallback(RenderCallback callback) {
		callbacks.remove(callback);
	}
	
	public List<RenderCallback> getCallbacks() {
		return callbacks;
	}
	
	public int getCanvasWidth() {
		return scaleStrategy.getCanvasWidth(getScreenBounds());
	}
	
	public int getCanvasHeight() {
		return scaleStrategy.getCanvasHeight(getScreenBounds());
	}
	
	public Rect getCanvasBounds() {
		return new Rect(0, 0, getCanvasWidth(), getCanvasHeight());
	}
	
	public final void setScaleStrategy(ScaleStrategy scaleStrategy) {
		this.scaleStrategy = scaleStrategy;
		onDisplayChanged();
	}
	
	public ScaleStrategy getScaleStrategy() {
		return scaleStrategy;
	}
	
	public final void setTargetFramerate(int framerate) {
		if (framerate < 1 || framerate > 120) {
			throw new IllegalArgumentException("Invalid framerate");
		}
		this.targetFramerate = framerate;
		onDisplayChanged();
	}
	
	public int getTargetFramerate() {
		return targetFramerate;
	}
	
	public final void setBackgroundColor(ColorRGB backgroundColor) {
		this.backgroundColor = backgroundColor;
		onDisplayChanged();
	}
	
	public ColorRGB getBackgroundColor() {
		return backgroundColor;
	}
	
	/**
	 * Called every time one of the display's properties is changed. This includes
	 * methods like {@link #setTargetFramerate(int)},
	 * {@link #setScaleStrategy(ScaleStrategy)}, {@link #setBackgroundColor(ColorRGB)}, 
	 * and any other properties that are made available by subclasses.
	 */
	protected abstract void onDisplayChanged();
	
	/**
	 * Draws the specified sprite by drawing its current graphics. Drawing the image
	 * is delegated to {@link #drawImageRegion(ImageRegion, int, int, Transform)}.
	 */
	public void drawSprite(Sprite sprite) {
		ImageRegion currentGraphics = sprite.getCurrentGraphics();
		drawImageRegion(currentGraphics, sprite.getX(), sprite.getY(), sprite.getTransform());
	}
	
	/**
	 * Draws the specified text by drawing all glyphs. Drawing the images is
	 * delegated to {@link #drawImageRegion(ImageRegion, int, int, Transform)}.
	 */
	public void drawText(Text text) {
		List<String> lines = text.getLines();
		BitmapFont font = text.getFont();
		
		int y = text.getY() - font.getBaseline();
		for (int line = 0; line < lines.size(); line++) {
			int x = text.getLineAnchorX(line);
			for (int i = 0; i < lines.get(line).length(); i++) {
				char c = lines.get(line).charAt(i);
				ImageRegion glyph = font.getGlyph(c);
				Rect glyphBounds = glyph.getRegion();
				drawImageRegion(glyph, x + glyphBounds.getWidth() / 2, 
						y + glyphBounds.getHeight() / 2, text.getTransform());
				x += glyphBounds.getWidth() + font.getLetterSpacing();
			}
			y += font.getLineHeight();
		}
	}
	
	/**
	 * Loads an audio clip by opening a stream to the file, without parsing or
	 * buffering the contents of the audio clip.
	 */
	public AudioData loadAudio(ResourceFile source) {
		return new AudioData(source);
	}
	
	public RenderStatistics getStats() {
		return renderStats;
	}
}
