//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import nl.colorize.multimedialib.math.MathUtils;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.ErrorHandler;
import nl.colorize.multimedialib.renderer.FrameSync;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.RenderCapabilities;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.WindowOptions;
import nl.colorize.multimedialib.scene.RenderContext;
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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
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

    private DisplayMode displayMode;
    private Canvas canvas;
    private FrameSync frameSync;
    private SceneContext context;
    private StandardMediaLoader mediaLoader;
    private AWTInput inputDevice;
    private WindowOptions windowOptions;

    private JFrame window;
    private Java2DGraphicsContext graphicsContext;
    private AtomicBoolean canvasDirty;
    private AtomicBoolean terminated;

    private static final boolean ANTI_ALIASING = true;
    private static final boolean BILINEAR_SCALING = true;
    private static final long MIN_SLEEP_TIME = 1L;
    private static final long MAX_SLEEP_TIME = 50L;
    private static final Logger LOGGER = LogHelper.getLogger(Java2DRenderer.class);

    public Java2DRenderer(DisplayMode displayMode, WindowOptions windowOptions) {
        SwingUtils.initializeSwing();

        this.displayMode = displayMode;
        this.canvas = displayMode.canvas();
        this.frameSync = new FrameSync(displayMode);
        this.mediaLoader = new StandardMediaLoader();
        this.windowOptions = windowOptions;

        // Makes sure the canvas and graphics context are initialized during
        // the first frame after the rendering thread starts.
        canvasDirty = new AtomicBoolean(true);
        terminated = new AtomicBoolean(false);
    }

    @Override
    public void start(Scene initialScene, ErrorHandler errorHandler) {
        window = initializeWindow(windowOptions);
        graphicsContext = new Java2DGraphicsContext(canvas);

        context = new RenderContext(this);
        context.changeScene(initialScene);

        Runnable animationLoop = () -> runAnimationLoop(errorHandler);
        Thread renderingThread = new Thread(animationLoop, "MultimediaLib-Java2D-Renderer");
        renderingThread.start();
    }

    private JFrame initializeWindow(WindowOptions windowOptions) {
        window = new JFrame();
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setResizable(true);
        window.setIgnoreRepaint(true);
        window.setFocusTraversalKeysEnabled(false);
        window.addWindowListener(createWindowCloseListener());
        window.addComponentListener(createResizeListener());
        window.setTitle(windowOptions.title());
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
            MacIntegration.setApplicationMenuListener(windowOptions.appMenuListener());
        }

        if (windowOptions.fullscreen()) {
            SwingUtils.goFullScreen(window);
        }

        return window;
    }

    private ComponentListener createResizeListener() {
        return new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                canvasDirty.set(true);
            }
        };
    }

    private WindowListener createWindowCloseListener() {
        return new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                LOGGER.info("Closing window");
                terminated.set(true);

                if (!windowOptions.embedded()) {
                    System.exit(0);
                }
            }
        };
    }

    private Image loadIcon(WindowOptions windowOptions) {
        if (windowOptions.iconFile() != null) {
            ResourceFile iconFile = new ResourceFile(windowOptions.iconFile().path());
            return SwingUtils.loadIcon(iconFile).getImage();
        } else {
            return null;
        }
    }

    /**
     * Main entry point for the rendering thread. This will keep running the
     * animation loop for as long as the application is active.
     * <p>
     * After every frame, this method will sleep the rendering thread to
     * synchronize the animation loop as close to the targeted framerate as
     * possible.
     */
    private void runAnimationLoop(ErrorHandler errorHandler) {
        Stopwatch timer = new Stopwatch();

        try {
            while (!terminated.get()) {
                long timestamp = timer.value();
                long elapsed = timer.tick();

                if (canvasDirty.get()) {
                    canvasDirty.set(false);
                    prepareCanvas();
                }

                frameSync.requestFrame(timestamp, this::doFrame);

                long sleepTime = Math.max(elapsed - displayMode.getFrameTimeMS(), 0) - timer.tock();
                Thread.yield();
                Thread.sleep(MathUtils.clamp(sleepTime, MIN_SLEEP_TIME, MAX_SLEEP_TIME));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during animation loop", e);
            errorHandler.onError(context, e);
            quit();
        }
    }

    /**
     * Performs an application frame update. This method is called from the
     * application loop, but might be called at different intervals than the
     * native display refresh rate.
     */
    private void doFrame(float deltaTime) {
        context.getFrameStats().markFrameStart();
        inputDevice.update(deltaTime);
        context.update(deltaTime);
        context.getFrameStats().markFrameUpdate();
        drawFrame();
        context.getFrameStats().markFrameRender();
    }

    private void drawFrame() {
        BufferStrategy windowBuffer = prepareWindowBuffer();
        Graphics bufferGraphics = accessWindowGraphics(windowBuffer);

        if (bufferGraphics != null) {
            Graphics2D g2 = Utils2D.createGraphics(bufferGraphics, ANTI_ALIASING, BILINEAR_SCALING);
            drawFrame(g2);
            blitGraphicsContext(windowBuffer);
            graphicsContext.dispose();
        }
    }

    private Graphics accessWindowGraphics(BufferStrategy windowBuffer) {
        try {
            return windowBuffer.getDrawGraphics();
        } catch (IllegalStateException e) {
            LOGGER.warning("Window buffer graphics not available: " + e.getMessage());
            return null;
        }
    }

    private void drawFrame(Graphics2D g2) {
        graphicsContext.bind(g2);
        graphicsContext.clear(window.getWidth(), window.getHeight() + 50);
        context.getStage().visit(graphicsContext);
    }

    private void prepareCanvas() {
        Insets windowInsets = window.getInsets();
        int windowWidth = window.getWidth() - windowInsets.left - windowInsets.right;
        int windowHeight = window.getHeight() - windowInsets.top - windowInsets.bottom;

        canvas.resizeScreen(windowWidth, windowHeight);
        canvas.offsetScreen(windowInsets.left, windowInsets.top);
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
    public RenderCapabilities getCapabilities() {
        StandardNetwork network = new StandardNetwork();
        return new RenderCapabilities(GraphicsMode.MODE_2D, displayMode,
            graphicsContext, inputDevice, mediaLoader, network);
    }

    public void quit() {
        terminated.set(true);
        if (window != null) {
            window.dispose();
        }
    }
}
