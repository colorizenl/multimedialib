//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.JPanel;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.ImageData;
import nl.colorize.multimedialib.graphics.ImageRegion;
import nl.colorize.multimedialib.graphics.Shape2D;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Shape;
import nl.colorize.multimedialib.renderer.AbstractRenderer;
import nl.colorize.multimedialib.renderer.RendererException;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.multimedialib.renderer.TimingUtils;
import nl.colorize.util.Platform;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.swing.SwingUtils;
import nl.colorize.util.swing.Utils2D;

/**
 * Implementation of a renderer that uses APIs from the Java standard library.
 * Graphics are displayed using Java 2D, AWT is used to create windows and capture
 * keyboard events, and Java Sound is used to play audio. 
 */
public class Java2DRenderer extends AbstractRenderer {
	
	private AWTInputDevice inputDevice;
	private MP3Player mp3Player;
	
	private String windowTitle;
	private BufferedImage windowIcon;
	
	private JFrame window;
	private Rect screenBounds;
	private AtomicBoolean layoutDirty;
	private Graphics2D graphicsContext;
	private BufferStrategy bufferStrategy;
	private AffineTransform transformHolder;
	private Map<ColorRGB, Color> colorCache;
	private Map<ImageRegion, BufferedImage> subImageCache;
	
	private static final boolean ANTI_ALIASING = true;
	private static final boolean BILINEAR_SCALING = true;
	
	public Java2DRenderer(ScaleStrategy scaleStrategy, int targetFramerate) {
		super(scaleStrategy, targetFramerate);
		
		screenBounds = scaleStrategy.getPreferredCanvasBounds();
		windowTitle = "";
		windowIcon = null;
		layoutDirty = new AtomicBoolean(true);
		transformHolder = new AffineTransform();
		colorCache = new HashMap<ColorRGB, Color>();
		subImageCache = new HashMap<ImageRegion, BufferedImage>();
	}
	
	@Override
	protected void startRenderer() {
		SwingUtils.initializeSwing();
		TimingUtils.runFixedTimestepAnimationLoop(targetFramerate, this, getStats());
	}
	
	@Override
	public void onInitialized() {
		initializeWindow();
		reLayoutCanvas();
		attachController();
		mp3Player = new MP3Player();
		super.onInitialized();
	}
	
	private void initializeWindow() {
		// JFrame.setSize() includes the borders and titlebar. To get a window
		// of the desired size we need to embed a fake window it in. It has to
		// be fake, since JPanel doesn't have a BufferStrategy.
		JPanel canvasPanel = new JPanel();
		canvasPanel.setPreferredSize(convertDimension(scaleStrategy.getPreferredCanvasBounds()));
		canvasPanel.setOpaque(false);
		canvasPanel.setFocusable(false);
		
		window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(true);
		window.setIgnoreRepaint(true);
		window.setFocusTraversalKeysEnabled(false);
		window.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				layoutDirty.set(true);
			}
		});
		window.setLayout(new BorderLayout());
		window.add(canvasPanel, BorderLayout.CENTER);
		window.pack();
		window.setLocationRelativeTo(null);
		onDisplayChanged();
		window.setVisible(true);
		window.createBufferStrategy(2);
		
		// Re-initialize some properties to apply them to the now
		// created window.
		setBackgroundColor(getBackgroundColor());
		setWindowTitle(windowTitle);
		setWindowIcon(windowIcon);
	}
	
	@Override
	protected void onDisplayChanged() {
		if (window != null) {
			window.setTitle(windowTitle);
			window.setIconImage(windowIcon);
			reLayoutCanvas();
		}
	}
	
	private void reLayoutCanvas() {
		Insets windowInsets = window.getInsets();
		int windowWidth = window.getWidth() - windowInsets.left - windowInsets.right;
		int windowHeight = window.getHeight() - windowInsets.top - windowInsets.bottom;
		screenBounds = new Rect(windowInsets.left, windowInsets.top, windowWidth, windowHeight);
	}
	
	private void attachController() {
		inputDevice = new AWTInputDevice(this);
		window.addKeyListener(inputDevice);
		window.addMouseListener(inputDevice);
		window.addMouseMotionListener(inputDevice);
	}
	
	@Override
	public void onFrame(float deltaTime) {
		prepareFrame();
		super.onFrame(deltaTime);
		blitGraphicsContext();
	}
	
	private void prepareFrame() {
		if (window != null && layoutDirty.get()) {
			layoutDirty.set(false);
			reLayoutCanvas();
		}
		
		inputDevice.refreshFromEventBuffer();
		
		graphicsContext = prepareGraphicsContext();
		graphicsContext.setColor(convertColor(getBackgroundColor()));
		if (window != null) {
			graphicsContext.fillRect(0, 0, window.getWidth(), window.getHeight());
		} else {
			graphicsContext.fillRect(0, 0, screenBounds.getWidth(), screenBounds.getHeight());
		}
		clipToCanvas();
	}
	
	private Graphics2D prepareGraphicsContext() {
		bufferStrategy = window.getBufferStrategy();
		Graphics bufferGraphics = bufferStrategy.getDrawGraphics();
		return Utils2D.createGraphics(bufferGraphics, ANTI_ALIASING, BILINEAR_SCALING);
	}
	
	private void clipToCanvas() {
		int clipX = scaleStrategy.convertToScreenX(screenBounds, 0);
		int clipY = scaleStrategy.convertToScreenY(screenBounds, 0);
		int clipWidth = scaleStrategy.convertToScreenX(screenBounds, getCanvasWidth()) - clipX;
		int clipHeight = scaleStrategy.convertToScreenY(screenBounds, getCanvasHeight()) - clipY;
		graphicsContext.setClip(clipX, clipY, clipWidth, clipHeight);
	}
	
	private void blitGraphicsContext() {
		graphicsContext.dispose();
		graphicsContext = null;
		
		if (!bufferStrategy.contentsLost()) {
			bufferStrategy.show();
			// Linux used to have problems with BufferStrategy, this
			// will make sure the window's graphics are up to date.
			if (Platform.isLinux()) {
				window.getToolkit().sync();
			}
		}
		bufferStrategy = null;
	}
	
	@Override
	protected void stopRenderer() {
		mp3Player.stopAll();
		window.dispose();
	}
	
	public Rect getScreenBounds() {
		return screenBounds;
	}
	
	public void drawImage(ImageData image, int x, int y, Transform transform) {
		drawImage(((RasterImage) image).getImage(), x, y, transform);
	}
	
	private void drawImage(BufferedImage image, int x, int y, Transform transform) {
		int screenX = scaleStrategy.convertToScreenX(screenBounds, x);
		int screenY = scaleStrategy.convertToScreenY(screenBounds, y);
		float scaleX = scaleStrategy.getScaleFactorX(screenBounds);
		float scaleY = scaleStrategy.getScaleFactorY(screenBounds);
		int screenWidth = (int) (image.getWidth() * scaleX);
		int screenHeight = (int) (image.getHeight() * scaleY);
		
		if (transform.isRotated() || transform.isScaled() || transform.getAlpha() < 100) {
			Composite originalComposite = graphicsContext.getComposite();
			applyAlphaCompositeForTransform(transform);
			
			scaleX *= transform.getHorizontalScale() / 100f;
			scaleY *= transform.getVerticalScale() / 100f;
			
			transformHolder.setToIdentity();
			transformHolder.translate(screenX - screenWidth / 2, screenY - screenHeight / 2);
			transformHolder.rotate(transform.getRotationInRadians(), screenWidth / 2.0, screenHeight / 2.0);
			transformHolder.scale(scaleX, scaleY);
			graphicsContext.drawImage(image, transformHolder, null);
			
			graphicsContext.setComposite(originalComposite);
		} else {
			graphicsContext.drawImage(image, screenX - screenWidth / 2, screenY - screenHeight / 2, 
					screenWidth, screenHeight, null);
		}
	}
	
	public void drawImageRegion(ImageRegion imageRegion, int x, int y, Transform transform) {
		BufferedImage subImage = subImageCache.get(imageRegion);
		if (subImage == null) {
			RasterImage image = (RasterImage) imageRegion.getImage();
			Rect region = imageRegion.getRegion();
			subImage = image.getImage().getSubimage(region.getX(), region.getY(), 
					region.getWidth(), region.getHeight());
			subImageCache.put(imageRegion, subImage);
		}
		
		drawImage(subImage, x, y, transform);
	}
	
	public void drawShape(Shape2D shape) {
		Shape shapeToDraw = shape.getShapeCurrent();
		Transform transform = shape.getTransform();
		
		if (shapeToDraw instanceof Rect) {
			drawRect((Rect) shapeToDraw, shape.getColor(), shape.getX(), shape.getY(), transform);
		} else {
			drawPolygon(shapeToDraw.toPolygon(), shape.getColor(), shape.getX(), shape.getY(), transform);
		}
	}
	
	private void drawRect(Rect shape, ColorRGB color, int x, int y, Transform transform) {
		int screenX = scaleStrategy.convertToScreenX(screenBounds, x);
		int screenY = scaleStrategy.convertToScreenY(screenBounds, y);
		int screenWidth = (int) (shape.getWidth() * scaleStrategy.getScaleFactorX(screenBounds));
		int screenHeight = (int) (shape.getHeight() * scaleStrategy.getScaleFactorY(screenBounds));
		
		Composite originalComposite = graphicsContext.getComposite();
		applyAlphaCompositeForTransform(transform);
		graphicsContext.setColor(convertColor(color));
		graphicsContext.fillRect(screenX - screenWidth / 2, screenY - screenHeight / 2, 
				screenWidth, screenHeight);
		graphicsContext.setComposite(originalComposite);
	}

	private void drawPolygon(Polygon polygon, ColorRGB color, int x, int y, Transform transform) {
		int[] px = new int[polygon.getNumPoints()];
		int[] py = new int[polygon.getNumPoints()];
		for (int i = 0; i < polygon.getNumPoints(); i++) {
			px[i] = scaleStrategy.convertToScreenX(screenBounds, polygon.getPointX(i));
			py[i] = scaleStrategy.convertToScreenY(screenBounds, polygon.getPointY(i));
		}
		
		Composite originalComposite = graphicsContext.getComposite();
		applyAlphaCompositeForTransform(transform);
		graphicsContext.setColor(convertColor(color));
		graphicsContext.fillPolygon(px, py, polygon.getNumPoints());
		graphicsContext.setComposite(originalComposite);
	}
	
	private void applyAlphaCompositeForTransform(Transform transform) {
		if (transform.getAlpha() != 100) {
			Composite alphaComposite = AlphaComposite.SrcOver.derive(transform.getAlpha() / 100f);
			graphicsContext.setComposite(alphaComposite);
		}
	}

	public ImageData loadImage(ResourceFile source) {
		try {
			BufferedImage loadedImage = Utils2D.loadImage(source.openStream());
			BufferedImage compatibleImage = Utils2D.makeImageCompatible(loadedImage);
			return new RasterImage(compatibleImage);
		} catch (IOException e) {
			throw new RendererException("Cannot load image from " + source.getPath(), e);
		}
	}
	
	private Color convertColor(ColorRGB c) {
		if (!colorCache.containsKey(c)) {
			colorCache.put(c, new Color(c.getR(), c.getG(), c.getB()));
		}
		return colorCache.get(c);
	}
	
	private Dimension convertDimension(Rect r) {
		return new Dimension(r.getWidth(), r.getHeight());
	}
	
	public AWTInputDevice getInputDevice() {
		return inputDevice;
	}
	
	public MP3Player getAudioQueue() {
		return mp3Player;
	}

	public void setWindowTitle(String windowTitle) {
		this.windowTitle = windowTitle;
		onDisplayChanged();
	}
	
	public String getWindowTitle() {
		return windowTitle;
	}
	
	public void setWindowIcon(BufferedImage windowIcon) {
		this.windowIcon = windowIcon;
		onDisplayChanged();
	}
	
	public BufferedImage getWindowIcon() {
		return windowIcon;
	}
}
