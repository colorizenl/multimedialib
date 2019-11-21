//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.graphics.Alignment;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.graphics.TrueTypeFont;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.AbstractRenderer;
import nl.colorize.multimedialib.renderer.ApplicationData;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsContext;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.WindowOptions;
import nl.colorize.multimedialib.renderer.java2d.StandardApplicationData;
import nl.colorize.util.Platform;

import java.util.List;

/**
 * Renderer built on top of the libGDX framework. In turn, libGDX supports multiple
 * back-end implementations that determine which platforms are supported and which
 * libraries are used.
 */
public class GDXRenderer extends AbstractRenderer implements ApplicationListener, GraphicsContext {

    private Application app;
    private GDXInput input;
    private GDXMediaLoader mediaLoader;
    private int framerate;

    private OrthographicCamera camera;
    private SpriteBatch batch;

    private static final Transform DEFAULT_TRANSFORM = new Transform();
    private static final List<Integer> SUPPORTED_FRAMERATES = ImmutableList.of(20, 25, 30, 60);

    public GDXRenderer(Canvas canvas, int framerate, WindowOptions windowOptions) {
        super(canvas);

        Preconditions.checkArgument(SUPPORTED_FRAMERATES.contains(framerate),
            "Framerate is not supported: " + framerate);

        this.framerate = framerate;
        this.app = initApplication(framerate, windowOptions);
    }

    private Application initApplication(int framerate, WindowOptions windowOptions) {
        Canvas canvas = getCanvas();

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(canvas.getWidth(), canvas.getHeight());
        config.setDecorated(true);
        config.setIdleFPS(framerate);
        config.setTitle(windowOptions.getTitle());
        if (windowOptions.hasIcon()) {
            config.setWindowIcon(Files.FileType.Internal, windowOptions.getIconFile().getPath());
        }

        return new Lwjgl3Application(this, config);
    }

    @Override
    public void create() {
        input = new GDXInput();
        mediaLoader = new GDXMediaLoader();

        camera = new OrthographicCamera();
        resize(getCanvas().getWidth(), getCanvas().getHeight());
        batch = new SpriteBatch();
    }

    @Override
    public void dispose() {
        mediaLoader.dispose();
        mediaLoader = null;

        batch.dispose();
        batch = null;
    }

    @Override
    public void resize(int width, int height) {
        getCanvas().resize(width, height);
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void pause() {
        //TODO
    }

    @Override
    public void resume() {
        //TODO
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        input.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        performFrameUpdate();
        performFrameRender();
        batch.end();
    }

    private void performFrameUpdate() {
        float frameTime = 1f / framerate;
        notifyUpdateCallbacks(frameTime);
    }

    private void performFrameRender() {
        notifyRenderCallbacks(this);
    }

    @Override
    public void drawBackground(ColorRGB backgroundColor) {
        Canvas canvas = getCanvas();
        Rect background = new Rect(0f, 0f, canvas.getWidth(), canvas.getHeight());
        drawRect(background, backgroundColor, null);
    }

    @Override
    public void drawRect(Rect rect, ColorRGB color, Transform transform) {
        if (transform == null) {
            transform = DEFAULT_TRANSFORM;
        }

        Texture colorTexture = mediaLoader.getColorTexture(color);
        TextureRegion colorTextureRegion = new TextureRegion(colorTexture, 0, 0,
                colorTexture.getWidth(), colorTexture.getHeight());

        draw(rect.getCenterX(), rect.getCenterY(), rect.getWidth(), rect.getHeight(),
                color, colorTextureRegion, transform);
    }

    @Override
    public void drawPolygon(Polygon polygon, ColorRGB color, Transform transform) {
        float minX = polygon.getPointX(0);
        float minY = polygon.getPointY(1);
        float maxX = polygon.getPointX(0);
        float maxY = polygon.getPointY(1);

        for (int i = 1; i < polygon.getNumPoints(); i++) {
            minX = Math.min(minX, polygon.getPointX(i));
            minY = Math.min(minY, polygon.getPointY(i));
            maxX = Math.max(maxX, polygon.getPointX(i));
            maxY = Math.max(maxY, polygon.getPointY(i));
        }

        drawRect(new Rect(minX, minY, maxX - minX, maxY - minY), color, transform);
    }

    @Override
    public void drawImage(Image image, float x, float y, Transform transform) {
        int width = image.getWidth();
        int height = image.getHeight();

        TextureRegion textureRegion = ((GDXTexture) image).getTextureRegion();

        if (transform == null) {
            transform = DEFAULT_TRANSFORM;
        }

        draw(x, y, width, height, ColorRGB.WHITE, textureRegion, transform);
    }

    private void draw(float x, float y, float width, float height, ColorRGB color,
                      TextureRegion texture, Transform transform) {
        float canvasX = x - width / 2f;
        float canvasY = Math.abs(y - getCanvas().getHeight()) - height / 2f;
        float originY = Math.abs(y - getCanvas().getHeight());

        batch.setColor(color.getR() / 255f, color.getG() / 255f, color.getB() / 255f,
            transform.getAlpha() / 100f);

        batch.draw(texture, canvasX, canvasY, x, originY, width, height,
            transform.getScaleX() / 100f, transform.getScaleY() / 100f,
            transform.getRotation());
    }

    @Override
    public void drawText(String text, TrueTypeFont font, float x, float y, Alignment align) {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public InputDevice getInputDevice() {
        return input;
    }

    @Override
    public MediaLoader getMediaLoader() {
        return mediaLoader;
    }

    @Override
    public ApplicationData getApplicationData(String appName) {
        if (Platform.isWindows() || Platform.isMac()) {
            return new StandardApplicationData(appName);
        } else {
            return new GDXApplicationData(appName);
        }
    }
}
