//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.badlogic.gdx.math.MathUtils;
import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.NetworkAccess;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.WindowOptions;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.Stopwatch;
import nl.colorize.util.swing.MacIntegration;
import nl.colorize.util.swing.SwingUtils;
import nl.colorize.util.swing.Utils2D;

import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
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
public class Java2DRenderer implements Renderer {

    private Canvas canvas;
    private int framerate;
    private Stopwatch syncTimer;
    private long oversleep;

    private SceneContext context;
    private StandardMediaLoader mediaLoader;
    private AWTInput inputDevice;
    private WindowOptions options;

    private JFrame window;
    private Java2DGraphicsContext graphicsContext;
    private AtomicBoolean canvasDirty;
    private AtomicBoolean terminated;

    private static final boolean ANTI_ALIASING = true;
    private static final boolean BILINEAR_SCALING = true;
    private static final long MIN_SLEEP_TIME = 1L;
    private static final long MAX_SLEEP_TIME = 50L;
    private static final Logger LOGGER = LogHelper.getLogger(Java2DRenderer.class);

    public Java2DRenderer(Canvas canvas, int framerate, WindowOptions options) {
        Preconditions.checkArgument(framerate >= 1 && framerate <= 120,
            "Invalid framerate: " + framerate);

        SwingUtils.initializeSwing();

        this.canvas = canvas;
        this.framerate = framerate;
        this.syncTimer = new Stopwatch();

        this.mediaLoader = new StandardMediaLoader();
        this.options = options;

        // Makes sure the canvas and graphics context are initialized during
        // the first frame after the rendering thread starts.
        canvasDirty = new AtomicBoolean(true);
        terminated = new AtomicBoolean(false);
    }

    @Override
    public void start(Scene initialScene) {
        window = initializeWindow(options);
        graphicsContext = new Java2DGraphicsContext(canvas, mediaLoader);

        NetworkAccess network = new StandardNetworkAccess();
        context = new SceneContext(canvas, inputDevice, mediaLoader, network);
        context.changeScene(initialScene);

        Thread renderingThread = new Thread(this::runAnimationLoop, "MultimediaLib-RenderingThread");
        renderingThread.start();
    }

    private JFrame initializeWindow(WindowOptions windowOptions) {
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
        window.getContentPane().setPreferredSize(new Dimension(canvas.getWidth(), canvas.getHeight()));
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        window.createBufferStrategy(2);

        inputDevice = new AWTInput(canvas);
        window.addKeyListener(inputDevice);
        window.addMouseListener(inputDevice);
        window.addMouseMotionListener(inputDevice);

        if (Platform.isMac()) {
            MacIntegration.setApplicationMenuListener(windowOptions.getAppMenuListener());
        }

        if (windowOptions.isFullscreen()) {
            SwingUtils.goFullScreen(window);
        }

        return window;
    }

    private Image loadIcon(WindowOptions windowOptions) {
        if (windowOptions.hasIcon()) {
            ResourceFile iconFile = new ResourceFile(windowOptions.getIconFile().getPath());
            return SwingUtils.loadIcon(iconFile).getImage();
        } else {
            return null;
        }
    }

    @Override
    public GraphicsMode getGraphicsMode() {
        return GraphicsMode.MODE_2D;
    }

    /**
     * Main entry point for the rendering thread. This will keep running the
     * animation loop for as long as the application is active.
     */
    private void runAnimationLoop() {
        while (!terminated.get()) {
            syncTimer.tick();

            if (canvasDirty.get()) {
                canvasDirty.set(false);
                prepareCanvas();
            }

            float frameTime = 1f / framerate;
            inputDevice.update(frameTime);
            context.update(frameTime);
            drawFrame();

            Thread.yield();
            long elapsedTime = syncTimer.tock();
            syncFrame(elapsedTime);
        }
    }

    private void drawFrame() {
        BufferStrategy windowBuffer = prepareWindowBuffer();
        Graphics bufferGraphics = windowBuffer.getDrawGraphics();
        Graphics2D g2 = Utils2D.createGraphics(bufferGraphics, ANTI_ALIASING, BILINEAR_SCALING);
        drawFrame(g2);
        blitGraphicsContext(windowBuffer);
        graphicsContext.dispose();
    }

    private void drawFrame(Graphics2D g2) {
        graphicsContext.bind(g2);
        prepareGraphics(g2);
        context.getStage().render2D(graphicsContext);
    }

    private void prepareCanvas() {
        Insets windowInsets = window.getInsets();
        int windowWidth = window.getWidth() - windowInsets.left - windowInsets.right;
        int windowHeight = window.getHeight() - windowInsets.top - windowInsets.bottom;

        canvas.resizeScreen(windowWidth, windowHeight);
        canvas.offsetScreen(windowInsets.left, windowInsets.top);
    }

    private void prepareGraphics(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, window.getWidth(), window.getHeight() + 50);
    }

    /**
     * Synchronizes the timing of frame updates to make the animation loop
     * run as close to the targeted framerate as possible.
     * <p>
     * This method uses {@code Thread.sleep} to pause the rendering thread.
     * Unfortunately, the sleep accuracy is not perfect and will often be
     * off by 1-2 milliseconds. This is normally not a big deal, but for
     * higher framerates this will have a noticeable impact. This method
     * therefore attempts to compensate for these inaccuracies.
     */
    private void syncFrame(long currentTrameTime) {
        long targetFrameTime = 1000L / framerate;

        long sleepTime = targetFrameTime - currentTrameTime - oversleep;
        sleepTime = MathUtils.clamp(sleepTime, MIN_SLEEP_TIME, MAX_SLEEP_TIME);

        try {
            Stopwatch sleepTimer = new Stopwatch();
            Thread.sleep(sleepTime);
            long actualSleepTime = sleepTimer.tock();
            oversleep = actualSleepTime - sleepTime;
        } catch (InterruptedException e) {
            LOGGER.warning("Frame sync interrupted");
        }
    }

    /**
     * Prepares the window buffer for the current frame. This buffer will be
     * used to display the graphics once the entire frame has been rendered.
     */
    private BufferStrategy prepareWindowBuffer() {
        return window.getBufferStrategy();
    }

    /**
     * Updates the window graphics with the contents of the buffer.
     */
    private void blitGraphicsContext(BufferStrategy windowBuffer) {
        if (!windowBuffer.contentsLost()) {
            windowBuffer.show();
            // Linux used to have problems with BufferStrategy, this
            // will make sure the window's graphics are up to date.
            if (Platform.isLinux()) {
                window.getToolkit().sync();
            }
        }
    }

    @Override
    public String takeScreenshot() {
        BufferedImage screenshot = new BufferedImage(canvas.getWidth(), canvas.getHeight(),
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = Utils2D.createGraphics(screenshot, true, true);
        graphicsContext.bind(g2);
        prepareGraphics(g2);
        context.getStage().render2D(graphicsContext);
        g2.dispose();

        return Utils2D.toDataURL(screenshot);
    }

    public void quit() {
        terminated.set(true);
        if (window != null) {
            window.dispose();
        }
    }
}
