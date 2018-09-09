//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.AnimationLoopRenderer;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;
import nl.colorize.util.swing.SwingUtils;
import nl.colorize.util.swing.Utils2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Implementation of a renderer that uses APIs from the Java standard library.
 * Graphics are displayed using Java 2D, AWT is used to create windows and capture
 * keyboard events, and Java Sound is used to play audio. 
 */
public class Java2DRenderer extends AnimationLoopRenderer {

    private AtomicBoolean active;
    private AWTInput inputDevice;

    private JFrame window;
    private Rect screenBounds;
    private AtomicBoolean layoutDirty;
    private Graphics2D graphicsContext;
    private BufferStrategy bufferStrategy;
    private AffineTransform transformHolder;
    private Color backgroundColor;
    private Map<ColorRGB, Color> colorCache;
    private String windowTitle;
    private BufferedImage windowIcon;

    private static final boolean ANTI_ALIASING = true;
    private static final boolean BILINEAR_SCALING = true;
    private static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    private static final Logger LOGGER = LogHelper.getLogger(Java2DRenderer.class);
    
    public Java2DRenderer(ScaleStrategy scaling, int framerate) {
        super(scaling, framerate);

        active = new AtomicBoolean(false);
        screenBounds = scaling.getPreferredCanvasBounds();
        windowTitle = "";
        windowIcon = null;
        layoutDirty = new AtomicBoolean(true);
        transformHolder = new AffineTransform();
        backgroundColor = DEFAULT_BACKGROUND_COLOR;
        colorCache = new HashMap<>();
    }
    
    @Override
    public void initialize() {
        Preconditions.checkState(!active.get(), "Renderer is already active");
        active.set(true);

        SwingUtils.initializeSwing();
        initializeWindow();
        reLayoutCanvas();

        inputDevice = new AWTInput(this);
        window.addKeyListener(inputDevice);
        window.addMouseListener(inputDevice);
        window.addMouseMotionListener(inputDevice);

        Thread renderingThread = new Thread(() -> runAnimationLoop(),
                "MultimediaLib-RenderingThread");
        renderingThread.start();
    }

    private void initializeWindow() {
        window = new JFrame();
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setResizable(true);
        window.setIgnoreRepaint(true);
        window.setFocusTraversalKeysEnabled(false);
        window.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                layoutDirty.set(true);
            }
        });
        // JFrame.setSize() includes the borders and titlebar. To get a window
        // of the desired size we need to embed a fake window it in. It has to
        // be fake, since JPanel doesn't have a BufferStrategy.
        window.setLayout(new BorderLayout());
        window.add(createCanvasPanel(), BorderLayout.CENTER);
        window.pack();
        window.setLocationRelativeTo(null);
        onDisplayChanged();
        window.setVisible(true);
        window.createBufferStrategy(2);
    }

    private JPanel createCanvasPanel() {
        JPanel canvasPanel = new JPanel();
        canvasPanel.setPreferredSize(convertDimension(scaling.getPreferredCanvasBounds()));
        canvasPanel.setOpaque(false);
        canvasPanel.setFocusable(false);
        return canvasPanel;
    }

    private void onDisplayChanged() {
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

    @Override
    public void terminate() {
        window.dispose();
    }

    private void runAnimationLoop() {
        while (active.get()) {
            prepareGraphicsContext();
            performFrameUpdate();
            blitGraphicsContext();
        }
    }

    private void prepareGraphicsContext() {
        if (window != null && layoutDirty.get()) {
            layoutDirty.set(false);
            reLayoutCanvas();
        }
        
        inputDevice.refreshFromEventBuffer();

        bufferStrategy = window.getBufferStrategy();
        Graphics bufferGraphics = bufferStrategy.getDrawGraphics();
        graphicsContext = Utils2D.createGraphics(bufferGraphics, ANTI_ALIASING, BILINEAR_SCALING);

        graphicsContext.setColor(backgroundColor);
        if (window != null) {
            graphicsContext.fillRect(0, 0, window.getWidth(), window.getHeight());
        } else {
            graphicsContext.fillRect(0, 0, screenBounds.getWidth(), screenBounds.getHeight());
        }

        clipToCanvas();
    }

    private void clipToCanvas() {
        int clipX = scaling.convertToScreenX(screenBounds, 0);
        int clipY = scaling.convertToScreenY(screenBounds, 0);
        int clipWidth = scaling.convertToScreenX(screenBounds, getCanvasWidth()) - clipX;
        int clipHeight = scaling.convertToScreenY(screenBounds, getCanvasHeight()) - clipY;

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
    public void drawBackground(ColorRGB backgroundColor) {
        this.backgroundColor = convertColor(backgroundColor);
    }

    @Override
    public void drawRect(Rect rect, ColorRGB color, Transform transform) {
        if (transform == null || (!transform.isRotated() && !transform.isScaled())) {
            int screenX = scaling.convertToScreenX(screenBounds, rect.getCenterX());
            int screenY = scaling.convertToScreenY(screenBounds, rect.getCenterY());
            int screenWidth = (int) (rect.getWidth() * scaling.getScaleFactorX(screenBounds));
            int screenHeight = (int) (rect.getHeight() * scaling.getScaleFactorY(screenBounds));

            Composite originalComposite = graphicsContext.getComposite();
            applyAlphaCompositeForTransform(transform);
            graphicsContext.setColor(convertColor(color));
            graphicsContext.fillRect(screenX - screenWidth / 2, screenY - screenHeight / 2,
                    screenWidth, screenHeight);
            graphicsContext.setComposite(originalComposite);
        } else {
            drawPolygon(rect.toPolygon(), color, transform);
        }
    }

    public void drawPolygon(Polygon polygon, ColorRGB color, Transform transform) {
        int[] px = new int[polygon.getNumPoints()];
        int[] py = new int[polygon.getNumPoints()];
        for (int i = 0; i < polygon.getNumPoints(); i++) {
            px[i] = scaling.convertToScreenX(screenBounds, polygon.getPointX(i));
            py[i] = scaling.convertToScreenY(screenBounds, polygon.getPointY(i));
        }

        Composite originalComposite = graphicsContext.getComposite();
        applyAlphaCompositeForTransform(transform);
        graphicsContext.setColor(convertColor(color));
        graphicsContext.fillPolygon(px, py, polygon.getNumPoints());
        graphicsContext.setComposite(originalComposite);
    }

    @Override
    public void drawImage(Image image, int x, int y, Transform transform) {
        drawImage(((Java2DImage) image).getImage(), x, y, transform);
    }

    private void drawImage(BufferedImage image, int x, int y, Transform transform) {
        int screenX = scaling.convertToScreenX(screenBounds, x);
        int screenY = scaling.convertToScreenY(screenBounds, y);
        float scaleX = scaling.getScaleFactorX(screenBounds);
        float scaleY = scaling.getScaleFactorY(screenBounds);
        int screenWidth = (int) (image.getWidth() * scaleX);
        int screenHeight = (int) (image.getHeight() * scaleY);
        
        if (isTransformed(transform)) {
            Composite originalComposite = graphicsContext.getComposite();
            applyAlphaCompositeForTransform(transform);
            
            scaleX *= transform.getScaleX() / 100f;
            scaleY *= transform.getScaleY() / 100f;
            
            transformHolder.setToIdentity();
            transformHolder.translate(screenX - screenWidth / 2f, screenY - screenHeight / 2f);
            transformHolder.rotate(transform.getRotationInRadians(), screenWidth / 2.0, screenHeight / 2.0);
            transformHolder.scale(scaleX, scaleY);
            graphicsContext.drawImage(image, transformHolder, null);
            
            graphicsContext.setComposite(originalComposite);
        } else {
            graphicsContext.drawImage(image, screenX - screenWidth / 2, screenY - screenHeight / 2, 
                    screenWidth, screenHeight, null);
        }
    }

    private boolean isTransformed(Transform transform) {
        if (transform == null) {
            return false;
        }
        return transform.isRotated() || transform.isScaled() || transform.getAlpha() < 100;
    }

    private void applyAlphaCompositeForTransform(Transform transform) {
        if (transform != null && transform.getAlpha() != 100) {
            Composite alphaComposite = AlphaComposite.SrcOver.derive(transform.getAlpha() / 100f);
            graphicsContext.setComposite(alphaComposite);
        }
    }

    @Override
    protected boolean shouldSyncFrames() {
        return true;
    }

    @Override
    public MediaLoader getMediaLoader() {
        return new StandardMediaLoader();
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

    @Override
    protected Rect getScreenBounds() {
        return screenBounds;
    }

    @Override
    protected InputDevice getInputDevice() {
        return inputDevice;
    }

    public void setScaleStrategy(ScaleStrategy scaleStrategy) {
        this.scaling = scaleStrategy;
        reLayoutCanvas();
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
