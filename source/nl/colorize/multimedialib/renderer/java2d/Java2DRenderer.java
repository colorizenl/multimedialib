//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.badlogic.gdx.math.MathUtils;
import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.renderer.AbstractRenderer;
import nl.colorize.multimedialib.renderer.ApplicationData;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.WindowOptions;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;
import nl.colorize.util.Stopwatch;
import nl.colorize.util.swing.MacIntegration;
import nl.colorize.util.swing.SwingUtils;
import nl.colorize.util.swing.Utils2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferStrategy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Implementation of a renderer that uses APIs from the Java standard library.
 * Graphics are displayed using Java 2D, AWT is used to create windows and capture
 * keyboard events, and Java Sound is used to play audio.
 * <p>
 * The renderer will use two different threads: the rendering thread is used to
 * update the graphics, while the Swing thread is used to listen for user input.
 */
public class Java2DRenderer extends AbstractRenderer {

    private int framerate;
    private Stopwatch syncTimer;

    private StandardMediaLoader mediaLoader;
    private AWTInput inputDevice;

    private JFrame window;
    private BufferStrategy windowBuffer;
    private Java2DGraphicsContext graphicsContext;
    private AtomicBoolean canvasDirty;

    private static final boolean ANTI_ALIASING = true;
    private static final boolean BILINEAR_SCALING = true;
    private static final long MIN_SLEEP_TIME = 1L;
    private static final long MAX_SLEEP_TIME = 50L;
    private static final Logger LOGGER = LogHelper.getLogger(Java2DRenderer.class);

    public Java2DRenderer(Canvas canvas, int framerate, WindowOptions windowOptions) {
        super(canvas);

        Preconditions.checkArgument(framerate >= 1 && framerate <= 120,
            "Invalid framerate: " + framerate);

        SwingUtils.initializeSwing();

        this.framerate = framerate;
        this.syncTimer = new Stopwatch();

        this.mediaLoader = new StandardMediaLoader();

        // Makes sure the canvas and graphics context are initialized during
        // the first frame after the rendering thread starts.
        canvasDirty = new AtomicBoolean(true);

        initializeWindow(windowOptions);
        initializeInput();

        window.createBufferStrategy(2);
        windowBuffer = window.getBufferStrategy();

        Thread renderingThread = new Thread(this::runAnimationLoop, "MultimediaLib-RenderingThread");
        renderingThread.start();
    }

    private void initializeWindow(WindowOptions windowOptions) {
        window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(true);
        window.setIgnoreRepaint(true);
        window.setFocusTraversalKeysEnabled(false);
        window.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                canvasDirty.set(true);
            }
        });
        window.setTitle(windowOptions.getTitle());
        window.setIconImage(loadIcon(windowOptions));
        // JFrame.setSize() includes the borders and titlebar. To get a window
        // of the desired size we need to embed a fake window it in. It has to
        // be fake, since JPanel doesn't have a BufferStrategy.
        window.setLayout(new BorderLayout());
        window.add(createCanvasPanel(), BorderLayout.CENTER);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        if (Platform.isMac()) {
            MacIntegration.setApplicationMenuListener(windowOptions.getAppMenuListener(), false);
        }
    }

    private Image loadIcon(WindowOptions windowOptions) {
        if (windowOptions.hasIcon()) {
            return SwingUtils.loadIcon(windowOptions.getIconFile()).getImage();
        } else {
            return null;
        }
    }

    private JPanel createCanvasPanel() {
        Canvas canvas = getCanvas();

        JPanel canvasPanel = new JPanel();
        canvasPanel.setPreferredSize(new Dimension(canvas.getWidth(), canvas.getHeight()));
        canvasPanel.setOpaque(false);
        canvasPanel.setFocusable(false);
        return canvasPanel;
    }

    private void initializeInput() {
        inputDevice = new AWTInput(getCanvas());
        window.addKeyListener(inputDevice);
        window.addMouseListener(inputDevice);
        window.addMouseMotionListener(inputDevice);
        addUpdateCallback(inputDevice);
    }

    /**
     * Main entry point for the rendering thread. This will keep running the
     * animation loop for as long as the application is active.
     */
    private void runAnimationLoop() {
        while (true) {
            syncTimer.tick();

            if (canvasDirty.get()) {
                canvasDirty.set(false);
                prepareCanvas();
            }

            Graphics bufferGraphics = windowBuffer.getDrawGraphics();
            Graphics2D g2 = Utils2D.createGraphics(bufferGraphics, ANTI_ALIASING, BILINEAR_SCALING);
            graphicsContext.bind(g2);

            performFrameUpdate();
            performFrameRender(g2);
            blitGraphicsContext();
            graphicsContext.dispose();

            long elapsedTime = syncTimer.tock();
            syncFrame(elapsedTime);
        }
    }

    private void prepareCanvas() {
        Insets windowInsets = window.getInsets();
        int windowWidth = window.getWidth() - windowInsets.left - windowInsets.right;
        int windowHeight = window.getHeight() - windowInsets.top - windowInsets.bottom;

        Canvas canvas = getCanvas();
        canvas.resize(windowWidth, windowHeight);
        canvas.offset(windowInsets.left, windowInsets.top);
        graphicsContext = new Java2DGraphicsContext(canvas, mediaLoader);
    }

    private void performFrameUpdate() {
        float frameTime = 1f / framerate;
        notifyUpdateCallbacks(frameTime);
    }

    private void performFrameRender(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, window.getWidth(), window.getHeight());

        notifyRenderCallbacks(graphicsContext);
    }

    /**
     * Synchronizes the timing of frame updates to make the animation loop run
     * as close to the targeted framerate as possible.
     */
    private void syncFrame(long elapsedFrameTime) {
        long target = 1000L / framerate;

        long sleepTime = target - elapsedFrameTime;
        sleepTime = MathUtils.clamp(sleepTime, MIN_SLEEP_TIME, MAX_SLEEP_TIME);

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            LOGGER.warning("Frame sync interrupted");
        }
    }

    private void blitGraphicsContext() {
        if (!windowBuffer.contentsLost()) {
            windowBuffer.show();
            // Linux used to have problems with BufferStrategy, this
            // will make sure the window's graphics are up to date.
            if (Platform.isLinux()) {
                window.getToolkit().sync();
            }
        }

        graphicsContext.dispose();

        Thread.yield();
    }

    @Override
    public InputDevice getInputDevice() {
        return inputDevice;
    }

    @Override
    public MediaLoader getMediaLoader() {
        return mediaLoader;
    }

    @Override
    public ApplicationData getApplicationData(String appName) {
        return new StandardApplicationData(appName);
    }
}
