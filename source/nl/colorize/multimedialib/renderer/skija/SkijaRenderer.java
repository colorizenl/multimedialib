//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.skija;

import io.github.humbleui.skija.BackendRenderTarget;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.ColorSpace;
import io.github.humbleui.skija.ColorType;
import io.github.humbleui.skija.DirectContext;
import io.github.humbleui.skija.FramebufferFormat;
import io.github.humbleui.skija.Image;
import io.github.humbleui.skija.PixelGeometry;
import io.github.humbleui.skija.Surface;
import io.github.humbleui.skija.SurfaceOrigin;
import io.github.humbleui.skija.SurfaceProps;
import io.github.humbleui.types.IRect;
import lombok.Getter;
import nl.colorize.multimedialib.math.Size;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.RenderConfig;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.RendererException;
import nl.colorize.multimedialib.renderer.java2d.StandardMediaLoader;
import nl.colorize.multimedialib.renderer.java2d.StandardNetwork;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.SceneManager;
import nl.colorize.util.LogHelper;
import nl.colorize.util.ResourceFile;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 2D renderer that uses <a href="https://github.com/HumbleUI/Skija">Skija</a>
 * to render graphics. Skija is only used for graphics, window and input are
 * provided by LWJGL.
 * <p>
 * Using the Skija renderer on Mac OS requires the {@code -XstartOnFirstThread}
 * command line argument when starting the application.
 */
public class SkijaRenderer implements Renderer, SceneContext {

    @Getter private RenderConfig config;
    @Getter private MediaLoader mediaLoader;
    @Getter private Network network;
    @Getter private LWJGLInput input;
    @Getter private SceneManager sceneManager;

    private Surface surface;
    private BackendRenderTarget renderTarget;

    private static final Logger LOGGER = LogHelper.getLogger(SkijaRenderer.class);

    @Override
    public void start(RenderConfig config, Scene initialScene) {
        this.config = config;
        this.network = new StandardNetwork();
        this.mediaLoader = new SkijaMediaLoader();
        this.input = new LWJGLInput(config);
        this.sceneManager = new SceneManager(this);

        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()) {
            throw new RendererException("Failed to initialize GLFW");
        }

        IRect windowBounds = getWindowBounds();
        long windowId = createWindow(windowBounds);

        try {
            sceneManager.changeScene(initialScene);
            runAnimationLoop(windowId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during animation loop", e);
            config.getErrorHandler().onError(this, e);
        }

        Callbacks.glfwFreeCallbacks(windowId);
        GLFW.glfwDestroyWindow(windowId);
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }

    private IRect getWindowBounds() {
        long monitorId = GLFW.glfwGetPrimaryMonitor();
        GLFWVidMode videoMode = GLFW.glfwGetVideoMode(monitorId);
        //TODO
        int width = (int) (videoMode.width() * 0.75);
        int height = (int) (videoMode.height() * 0.75);
        int windowX = Math.max(0, (videoMode.width() - width) / 2);
        int windowY = Math.max(0, (videoMode.height() - height) / 2);
        return IRect.makeXYWH(windowX, windowY, width, height);
    }

    private long createWindow(IRect windowBounds) {
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        long windowId = GLFW.glfwCreateWindow(windowBounds.getWidth(), windowBounds.getHeight(),
            config.getWindowOptions().getTitle(), 0L, 0L);

        if (windowId == 0L) {
            throw new RendererException("Failed to create GLFW window");
        }

        GLFW.glfwSetWindowPos(windowId, windowBounds.getLeft(), windowBounds.getTop());
        refreshContextDimensions(windowId);

        GLFW.glfwMakeContextCurrent(windowId);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(windowId);
        return windowId;
    }

    private void refreshContextDimensions(long windowId) {
        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetFramebufferSize(windowId, width, height);

        float[] scaleX = new float[1];
        float[] scaleY = new float[1];
        GLFW.glfwGetWindowContentScale(windowId, scaleX, scaleY);

        config.getCanvas().resizeScreen(width[0], height[0], scaleX[0]);
    }

    private Canvas initSkia(DirectContext context) {
        if (surface != null) {
            surface.close();
        }

        if (renderTarget != null) {
            renderTarget.close();
        }

        Size screenSize = config.getCanvas().getScreenSize();

        renderTarget = BackendRenderTarget.makeGL(screenSize.width(), screenSize.height(),
            0, 8, 0, FramebufferFormat.GR_GL_RGBA8);

        surface = Surface.makeFromBackendRenderTarget(
            context, renderTarget, SurfaceOrigin.BOTTOM_LEFT,
            ColorType.RGBA_8888, ColorSpace.getDisplayP3(),
            new SurfaceProps(PixelGeometry.RGB_H));

        return surface.getCanvas();
    }

    private void runAnimationLoop(long windowId) {
        GL.createCapabilities();
        DirectContext context = DirectContext.makeGL();

        GLFW.glfwSetWindowSizeCallback(windowId, (_, _, _) -> refreshContextDimensions(windowId));
        GLFW.glfwSetCursorPosCallback(windowId, input::onMouseMove);
        GLFW.glfwSetMouseButtonCallback(windowId, input::onMouseButton);
        GLFW.glfwSetKeyCallback(windowId, input::onKey);

        Canvas skija = initSkia(context);
        SkijaGraphics graphics = new SkijaGraphics(skija, config);

        while (!GLFW.glfwWindowShouldClose(windowId)) {
            input.reset();
            GLFW.glfwPollEvents();
            sceneManager.requestFrameUpdate();

            getFrameStats().markStart(FrameStats.PHASE_FRAME_RENDER);
            getStage().visit(graphics);
            getFrameStats().markEnd(FrameStats.PHASE_FRAME_RENDER);
            context.flush();
            GLFW.glfwSwapBuffers(windowId);
        }
    }

    @Override
    public void terminate() {
        System.exit(0);
    }

    @Override
    public String getDisplayName() {
        return "Skija renderer";
    }

    @Override
    public List<GraphicsMode> getSupportedGraphicsModes() {
        return List.of(GraphicsMode.MODE_2D);
    }

    /**
     * The Skija renderer mostly relies on the desktop renderer, but it needs
     * to load its own images due to issues with converting AWT images.
     */
    private static class SkijaMediaLoader extends StandardMediaLoader {

        @Override
        public SkijaImage loadImage(ResourceFile file) {
            return new SkijaImage(Image.makeDeferredFromEncodedBytes(file.readBytes()));
        }
    }
}
