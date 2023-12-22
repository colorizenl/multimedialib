//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.UBJsonReader;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.GeometryBuilder;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.java2d.StandardMediaLoader;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.FontStyle;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.OutlineFont;
import nl.colorize.multimedialib.stage.PolygonModel;
import nl.colorize.util.stats.Cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Loads media assets using the libGDX framework.
 * <p>
 * libGDX requires an explicit dispose step to close all loaded media files
 * and release the associated resources. This can be done globally for all
 * media files by calling {@link #dispose()}.
 */
public class GDXMediaLoader implements MediaLoader, Disposable {

    private List<Disposable> loaded;
    private Cache<FontCacheKey, BitmapFont> fontCache;
    private StandardMediaLoader appDataDelegate;

    private static final Texture.TextureFilter TEXTURE_FILTER = Texture.TextureFilter.Linear;
    private static final int FONT_CACHE_SIZE = 100;
    private static final int BITMAP_FONT_SCALE = 2;
    private static final String CHARSET = "UTF-8";

    public GDXMediaLoader() {
        this.loaded = new ArrayList<>();
        this.fontCache = Cache.from(this::generateBitmapFont, FONT_CACHE_SIZE);
        this.appDataDelegate = new StandardMediaLoader();
    }

    @Override
    public Image loadImage(FilePointer file) {
        Texture texture = new Texture(getFileHandle(file));
        texture.setFilter(TEXTURE_FILTER, TEXTURE_FILTER);
        loaded.add(texture);
        return new GDXImage(texture);
    }

    @Override
    public Audio loadAudio(FilePointer file) {
        Sound sound = Gdx.audio.newSound(getFileHandle(file));
        loaded.add(sound);
        return new GDXAudio(sound);
    }

    @Override
    public OutlineFont loadFont(FilePointer file, String family, FontStyle style) {
        return new GDXBitmapFont(this, getFileHandle(file), family, style);
    }

    protected BitmapFont getBitmapFont(FileHandle source, String family, FontStyle style) {
        FontCacheKey cacheKey = new FontCacheKey(source, family, style);
        return fontCache.get(cacheKey);
    }

    private BitmapFont generateBitmapFont(FontCacheKey cacheKey) {
        FreeTypeFontGenerator.FreeTypeFontParameter config =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        config.size = cacheKey.style.size() * BITMAP_FONT_SCALE;
        config.color = toColor(cacheKey.style.color());
        config.minFilter = TEXTURE_FILTER;
        config.magFilter = TEXTURE_FILTER;

        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(cacheKey.source());
        BitmapFont bitmapFont = fontGenerator.generateFont(config);
        bitmapFont.getData().setScale(1f / BITMAP_FONT_SCALE);
        fontGenerator.dispose();
        return bitmapFont;
    }

    @Override
    public String loadText(FilePointer file) {
        return getFileHandle(file).readString(CHARSET);
    }

    @Override
    public PolygonModel loadModel(FilePointer file) {
        Model model = loadModel(getFileHandle(file));
        return createInstance(model);
    }

    private Model loadModel(FileHandle file) {
        if (file.toString().endsWith(".g3db")) {
            G3dModelLoader g3dLoader = new G3dModelLoader(new UBJsonReader(),
                new InternalFileHandleResolver());
            return g3dLoader.loadModel(file);
        } else if (file.toString().endsWith(".gltf")) {
            GLTFLoader gltfLoader = new GLTFLoader();
            SceneAsset sceneAsset = gltfLoader.load(file, true);
            return sceneAsset.scene.model;
        } else {
            throw new MediaException("Unsupported model file format: " + file);
        }
    }

    @Override
    public GeometryBuilder getGeometryBuilder() {
        return new GDXGeometryBuilder(this);
    }

    protected PolygonModel createInstance(Model model) {
        ModelInstance instance = new ModelInstance(model);
        instance.materials.get(0).set(
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        loaded.add(model);
        return new GDXModel(instance);
    }

    @Override
    public boolean containsResourceFile(FilePointer file) {
        return getFileHandle(file).exists();
    }

    @Override
    public Properties loadApplicationData(String appName) {
        return appDataDelegate.loadApplicationData(appName);
    }

    @Override
    public void saveApplicationData(String appName, Properties data) {
        appDataDelegate.saveApplicationData(appName, data);
    }

    private FileHandle getFileHandle(FilePointer file) {
        return Gdx.files.internal(file.path());
    }

    @Override
    public void dispose() {
        loaded.forEach(Disposable::dispose);
        loaded.clear();
        fontCache.forgetAll();
    }

    public static Color toColor(ColorRGB color) {
        return new Color(color.r() / 255f, color.g() / 255f, color.b() / 255f, 1f);
    }

    private record FontCacheKey(FileHandle source, String family, FontStyle style) {
    }
}
