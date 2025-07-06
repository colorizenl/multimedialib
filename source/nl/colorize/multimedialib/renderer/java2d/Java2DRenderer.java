//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import lombok.Getter;
import nl.colorize.multimedialib.math.Box;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.math.Shape3D;
import nl.colorize.multimedialib.math.Size;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.RenderConfig;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.WindowOptions;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.SceneManager;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.Stopwatch;
import nl.colorize.util.swing.ApplicationMenuListener;
import nl.colorize.util.swing.MacIntegration;
import nl.colorize.util.swing.MultiLabel;
import nl.colorize.util.swing.Popups;
import nl.colorize.util.swing.SwingUtils;
import nl.colorize.util.swing.Utils2D;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
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

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

/**
 * Implementation of a renderer that uses APIs from the Java standard library.
 * Graphics are displayed using Java 2D, AWT is used to create windows and capture
 * keyboard events, and Java Sound is used to play audio.
 * <p>
 * The renderer will use two different threads: the rendering thread is used to
 * update the graphics, while the Swing thread is used to listen for user input.
 */
public class Java2DRenderer implements Renderer, SceneContext, ApplicationMenuListener {

    @Getter private RenderConfig config;
    @Getter private AWTInput input;
    @Getter private StandardMediaLoader mediaLoader;
    @Getter private Network network;
    @Getter private SceneManager sceneManager;
    @Getter private Stage stage;

    private JFrame window;
    private Java2DGraphicsContext graphicsContext;
    private AtomicBoolean canvasDirty;
    private AtomicBoolean terminated;

    private static final boolean ANTI_ALIASING = true;
    private static final boolean BILINEAR_SCALING = true;
    private static final Logger LOGGER = LogHelper.getLogger(Java2DRenderer.class);

    public Java2DRenderer() {
        SwingUtils.initializeSwing();

        // Makes sure the canvas and graphics context are initialized during
        // the first frame after the rendering thread starts.
        canvasDirty = new AtomicBoolean(true);
        terminated = new AtomicBoolean(false);
    }

    @Override
    public void start(RenderConfig config, Scene initialScene) {
        this.config = config;
        window = initializeWindow(config.getWindowOptions());
        input = initializeInput();
        mediaLoader = new StandardMediaLoader();
        graphicsContext = new Java2DGraphicsContext(config.getCanvas(), StandardMediaLoader.fontCache);
        network = new StandardNetwork();
        sceneManager = new SceneManager();
        stage = new Stage(config.getGraphicsMode(), config.getCanvas());

        changeScene(initialScene);

        Thread renderingThread = new Thread(this::runAnimationLoop, "MultimediaLib-Java2D-Renderer");
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
        window.setIconImage(loadIcon(windowOptions).getImage());
        window.getContentPane().setPreferredSize(getWindowSize());
        if (windowOptions.isFullscreen()) {
            SwingUtils.goFullScreen(window);
        }
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        window.createBufferStrategy(2);
        if (Platform.isMac()) {
            MacIntegration.setApplicationMenuListener(this);
        }
        return window;
    }

    private Dimension getWindowSize() {
        Size windowSize = config.getWindowOptions().getWindowSize()
            .orElse(config.getCanvas().getSize());
        return new Dimension(windowSize.width(), windowSize.height());
    }

    private AWTInput initializeInput() {
        AWTInput input = new AWTInput(config);
        window.addKeyListener(input);
        window.addMouseListener(input);
        window.addMouseMotionListener(input);
        return input;
    }

    private ComponentListener createResizeListener() {
        return new ComponentAdapter() {
            @Override
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
                System.exit(0);
            }
        };
    }

    private ImageIcon loadIcon(WindowOptions windowOptions) {
        ResourceFile iconFile = new ResourceFile(windowOptions.getIconFile().path());
        return SwingUtils.loadIcon(iconFile);
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
    private void runAnimationLoop() {
        Stopwatch timer = new Stopwatch();
        long targetFrameTime = Math.round(1000f / config.getFramerate());

        try {
            while (!terminated.get()) {
                long oversleep = Math.max(timer.tick() - targetFrameTime, 0L);

                if (canvasDirty.get()) {
                    canvasDirty.set(false);
                    prepareCanvas();
                }

                if (sceneManager.requestFrameUpdate(this) > 0) {
                    renderFrame();
                }

                long frameTime = timer.tock();
                long sleepTime = targetFrameTime - frameTime - oversleep;
                Thread.yield();
                Thread.sleep(Math.clamp(sleepTime, 1L, 100L));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during animation loop", e);
            config.getErrorHandler().onError(this, e);
            terminate();
        }
    }

    private void renderFrame() {
        BufferStrategy windowBuffer = window.getBufferStrategy();
        Graphics bufferGraphics = accessWindowGraphics(windowBuffer);
        FrameStats frameStats = sceneManager.getFrameStats();

        if (bufferGraphics != null) {
            frameStats.markStart(FrameStats.PHASE_FRAME_RENDER);
            Graphics2D g2 = Utils2D.createGraphics(bufferGraphics, ANTI_ALIASING, BILINEAR_SCALING);
            graphicsContext.bind(g2);
            getStage().visit(graphicsContext);
            blitGraphicsContext(windowBuffer);
            graphicsContext.dispose();
            frameStats.markEnd(FrameStats.PHASE_FRAME_RENDER);
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

        config.getCanvas().resizeScreen(windowWidth, windowHeight);
        config.getCanvas().offsetScreen(windowInsets.left, windowInsets.top);
    }

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
    public Mesh createMesh(Shape3D shape, ColorRGB color) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Point2D project(Point3D position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean castPickRay(Point2D canvasPosition, Box area) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSupported(GraphicsMode graphicsMode) {
        return graphicsMode == GraphicsMode.MODE_2D;
    }

    @Override
    public void terminate() {
        System.exit(0);
    }

    @Override
    public void takeScreenshot(File screenshotFile) {
        Java2DGraphicsContext screenshotContext = new Java2DGraphicsContext(
            config.getCanvas(), StandardMediaLoader.fontCache);
        BufferedImage image = new BufferedImage(window.getWidth(), window.getHeight(), TYPE_INT_ARGB);
        Graphics2D g2 = Utils2D.createGraphics(image, false, false);
        screenshotContext.bind(g2);
        getStage().visit(screenshotContext);
        screenshotContext.dispose();

        try {
            Utils2D.savePNG(image, screenshotFile);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to save screenshot", e);
        }
    }

    @Override
    public String getRendererName() {
        return "Java2D renderer";
    }

    @Override
    public void onQuit() {
        terminate();
    }

    @Override
    public void onAbout() {
        Popups.message(null, "", new MultiLabel(config.getWindowOptions().getTitle(), 400));
    }
}
