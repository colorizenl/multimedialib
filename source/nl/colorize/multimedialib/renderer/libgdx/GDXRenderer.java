//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import nl.colorize.multimedialib.graphics.Audio;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.AnimationLoopRenderer;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.util.ResourceFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renderer built on top of the libGDX framework.
 */
public class GDXRenderer extends AnimationLoopRenderer implements ApplicationListener, MediaLoader {

    private Rect screenBounds;
    private ColorRGB backgroundColor;

    private Application app;
    private GDXInput input;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private List<GDXTexture> loadedTextures;
    private List<GDXSound> loadedSounds;
    private Map<ColorRGB, Texture> colorTextureCache;
    private String windowTitle;
    private ResourceFile windowIcon;

    private static final Transform DEFAULT_TRANSFORM = new Transform();
    private static final int COLOR_TEXTURE_SIZE = 8;
    private static final int DESKTOP_TITLE_BAR_HEIGHT = 22;

    public GDXRenderer(ScaleStrategy scaling, int framerate) {
        super(scaling, framerate);

        screenBounds = scaling.getPreferredCanvasBounds();
        backgroundColor = ColorRGB.WHITE;
    }

    @Override
    public void initialize() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(screenBounds.getWidth(), screenBounds.getHeight());
        config.setDecorated(true);

        if (windowTitle != null) {
            config.setTitle(windowTitle);
        }
        if (windowIcon != null) {
            config.setWindowIcon(Files.FileType.Internal, windowIcon.getPath());
        }

        input = new GDXInput();
        app = new Lwjgl3Application(this, config);
    }

    @Override
    public void create() {
        camera = new OrthographicCamera();
        resize(screenBounds.getWidth(), screenBounds.getHeight());

        batch = new SpriteBatch();
        loadedTextures = new ArrayList<>();
        loadedSounds = new ArrayList<>();
        colorTextureCache = new HashMap<>();
    }

    @Override
    public void terminate() {
        app.exit();
    }

    @Override
    public void dispose() {
        loadedTextures.forEach(texture -> texture.dispose());
        loadedTextures.clear();

        loadedSounds.forEach(sound -> sound.dispose());
        loadedSounds.clear();

        colorTextureCache.values().forEach(colorTexture -> colorTexture.dispose());
        colorTextureCache.clear();

        batch.dispose();
    }

    @Override
    protected boolean shouldSyncFrames() {
        return false;
    }

    @Override
    public void resize(int width, int height) {
        screenBounds.setWidth(width);
        screenBounds.setHeight(height);

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
        Gdx.gl.glClearColor(backgroundColor.getR() / 255f, backgroundColor.getG() / 255f,
                backgroundColor.getB() / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        input.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        performFrameUpdate();
        batch.end();
    }

    @Override
    public void drawBackground(ColorRGB backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Override
    public void drawRect(Rect rect, ColorRGB color, Transform transform) {
        if (transform == null) {
            transform = DEFAULT_TRANSFORM;
        }

        Texture colorTexture = getColorTexture(color);
        TextureRegion colorTextureRegion = new TextureRegion(colorTexture, 0, 0,
                colorTexture.getWidth(), colorTexture.getHeight());

        draw(rect.getCenterX(), rect.getCenterY(), rect.getWidth(), rect.getHeight(),
                color, colorTextureRegion, transform);
    }

    @Override
    public void drawImage(Image image, int x, int y, Transform transform) {
        int width = image.getWidth();
        int height = image.getHeight();

        TextureRegion textureRegion = ((GDXTexture) image).getTextureRegion();

        if (transform == null) {
            transform = DEFAULT_TRANSFORM;
        }

        draw(x, y, width, height, ColorRGB.WHITE, textureRegion, transform);
    }

    private void draw(int x, int y, int width, int height, ColorRGB color, TextureRegion texture,
                      Transform transform) {
        float canvasX = x - width / 2f;
        float canvasY = Math.abs(y - getCanvasHeight()) - height / 2f;
        float originY = Math.abs(y - getCanvasHeight());

        batch.setColor(color.getR() / 255f, color.getG() / 255f, color.getB() / 255f, 1f);

        batch.draw(texture, canvasX, canvasY, x, originY, width, height,
            transform.getScaleX() / 100f, transform.getScaleY() / 100f,
            transform.getRotation());
    }

    @Override
    public MediaLoader getMediaLoader() {
        return this;
    }

    @Override
    public Image loadImage(ResourceFile source) {
        Texture texture = new Texture(Gdx.files.internal(source.getPath()));
        GDXTexture gdxTexture = new GDXTexture(texture);
        loadedTextures.add(gdxTexture);
        return gdxTexture;
    }

    private Texture getColorTexture(ColorRGB color) {
        Texture colorTexture = colorTextureCache.get(color);
        if (colorTexture == null) {
            colorTexture = generateColorTexture(color);
            colorTextureCache.put(color, colorTexture);
        }
        return colorTexture;
    }

    private Texture generateColorTexture(ColorRGB color) {
        Pixmap pixelData = new Pixmap(COLOR_TEXTURE_SIZE, COLOR_TEXTURE_SIZE, Pixmap.Format.RGBA8888);
        pixelData.setColor(color.getR() / 255f, color.getG() / 255f, color.getB() / 255f, 1f);
        pixelData.fillRectangle(0, 0, COLOR_TEXTURE_SIZE, COLOR_TEXTURE_SIZE);
        return new Texture(pixelData);
    }

    @Override
    public Audio loadAudio(ResourceFile source) {
        Sound sound = Gdx.audio.newSound(Gdx.files.internal(source.getPath()));
        GDXSound gdxSound = new GDXSound(sound, source);
        loadedSounds.add(gdxSound);
        return gdxSound;
    }

    @Override
    protected InputDevice getInputDevice() {
        return input;
    }

    @Override
    protected Rect getScreenBounds() {
        return screenBounds;
    }

    public void setWindowTitle(String windowTitle) {
        this.windowTitle = windowTitle;
    }

    public void setWindowIcon(ResourceFile windowIcon) {
        this.windowIcon = windowIcon;
    }
}
