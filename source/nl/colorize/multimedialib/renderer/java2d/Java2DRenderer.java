//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.ErrorHandler;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.WindowOptions;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.stage.StageVisitor;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.Stopwatch;
import nl.colorize.util.swing.MacIntegration;
import nl.colorize.util.swing.SwingUtils;
import nl.colorize.util.swing.Utils2D;

import javax.swing.JFrame;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
    private WindowOptions windowOptions;

    private JFrame window;
    private Java2DGraphicsContext graphicsContext;
    private AWTInput input;
    private StandardMediaLoader mediaLoader;
    private AtomicBoolean canvasDirty;
    private AtomicBoolean terminated;
    private SceneContext context;

    private static final boolean ANTI_ALIASING = true;
    private static final boolean BILINEAR_SCALING = true;
    private static final Logger LOGGER = LogHelper.getLogger(Java2DRenderer.class);

    public Java2DRenderer(DisplayMode displayMode, WindowOptions windowOptions) {
        SwingUtils.initializeSwing();

        this.displayMode = displayMode;
        this.canvas = displayMode.canvas();
        this.windowOptions = windowOptions;

        // Makes sure the canvas and graphics context are initialized during
        // the first frame after the rendering thread starts.
        canvasDirty = new AtomicBoolean(true);
        terminated = new AtomicBoolean(false);
    }

    @Override
    public void start(Scene initialScene, ErrorHandler errorHandler) {
        window = initializeWindow(windowOptions);
        input = initializeInput();
        graphicsContext = new Java2DGraphicsContext(canvas);
        mediaLoader = new StandardMediaLoader();

        context = new SceneContext(this, new Stopwatch());
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
        window.setTitle(windowOptions.getTitle());
        window.setIconImage(loadIcon(windowOptions));
        window.getContentPane().setPreferredSize(new Dimension(canvas.getWidth(), canvas.getHeight()));
        if (windowOptions.isFullscreen()) {
            SwingUtils.goFullScreen(window);
        }
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        window.createBufferStrategy(2);

        if (Platform.isMac()) {
            MacIntegration.setApplicationMenuListener(windowOptions.getAppMenuListener());
        }

        return window;
    }

    private AWTInput initializeInput() {
        AWTInput input = new AWTInput(canvas);
        window.addKeyListener(input);
        window.addMouseListener(input);
        window.addMouseMotionListener(input);
        return input;
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

                if (!windowOptions.isEmbedded()) {
                    System.exit(0);
                }
            }
        };
    }

    private Image loadIcon(WindowOptions windowOptions) {
        ResourceFile iconFile = new ResourceFile(windowOptions.getIconFile().path());
        return SwingUtils.loadIcon(iconFile).getImage();
    }

    /**
     * Main entry point for the rendering thread. This will keep running
     * the animation loop for as long as the application is active.
     * <p>
     * After every frame, this method will sleep the rendering thread to
     * synchronize the animation loop as close to the targeted framerate as
     * possible. This mechanism is slightly different from the frame
     * synchronization performed by {@link SceneContext}, as the Java2D
     * renderer does not use vsync and needs to manually manage the
     * framerate.
     */
    private void runAnimationLoop(ErrorHandler errorHandler) {
        Stopwatch timer = new Stopwatch();

        try {
            while (!terminated.get()) {
                long oversleep = Math.max(timer.tick() - displayMode.getFrameTimeMS(), 0L);

                if (canvasDirty.get()) {
                    canvasDirty.set(false);
                    prepareCanvas();
                }

                context.syncFrame();
                renderFrame();

                long frameTime = timer.tock();
                long sleepTime = displayMode.getFrameTimeMS() - frameTime - oversleep;
                Thread.yield();
                Thread.sleep(Math.clamp(sleepTime, 1L, 100L));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during animation loop", e);
            errorHandler.onError(context, e);
            terminate();
        }
    }

    private void renderFrame() {
        BufferStrategy windowBuffer = prepareWindowBuffer();
        Graphics bufferGraphics = accessWindowGraphics(windowBuffer);

        if (bufferGraphics != null) {
            context.getFrameStats().markStart(FrameStats.PHASE_FRAME_RENDER);

            Graphics2D g2 = Utils2D.createGraphics(bufferGraphics, ANTI_ALIASING, BILINEAR_SCALING);
            graphicsContext.bind(g2);
            context.getStage().visit(graphicsContext);
            blitGraphicsContext(windowBuffer);
            graphicsContext.dispose();

            context.getFrameStats().markEnd(FrameStats.PHASE_FRAME_RENDER);
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
            // will make sure the window's graphics are up-to-date.
            if (Platform.isLinux()) {
                window.getToolkit().sync();
            }
        }
    }

    @Override
    public GraphicsMode getGraphicsMode() {
        return GraphicsMode.MODE_2D;
    }

    @Override
    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    @Override
    public StageVisitor getGraphics() {
        return graphicsContext;
    }

    @Override
    public InputDevice getInput() {
        return input;
    }

    @Override
    public MediaLoader getMediaLoader() {
        return mediaLoader;
    }

    @Override
    public Network getNetwork() {
        return new StandardNetwork();
    }

    @Override
    public void takeScreenshot(File outputFile) {
        try {
            Rectangle windowBounds = window.getBounds();
            Insets insets = window.getInsets();

            Rectangle bounds = new Rectangle(
                windowBounds.x + insets.left,
                windowBounds.y + insets.top,
                windowBounds.width - insets.left - insets.right,
                windowBounds.height - insets.top - insets.bottom
            );

            Robot robot = new Robot();
            BufferedImage screenshot = robot.createScreenCapture(bounds);
            Utils2D.savePNG(screenshot, outputFile);
        } catch (AWTException e) {
            LOGGER.warning("AWT Robot not supported");
        } catch (IOException e) {
            LOGGER.warning("Failed to write screenshot to " + outputFile.getAbsolutePath());
        }
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        File workDir = Platform.getUserWorkingDirectory();
        return new File(workDir, "build.gradle").exists();
    }

    @Override
    public void terminate() {
        System.exit(0);
    }
}
