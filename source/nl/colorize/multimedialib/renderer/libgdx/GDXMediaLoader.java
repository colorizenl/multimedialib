//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
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
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.UBJsonReader;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import nl.colorize.multimedialib.math.Cache;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.GeometryBuilder;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.java2d.MP3;
import nl.colorize.multimedialib.renderer.java2d.StandardMediaLoader;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.FontStyle;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.OutlineFont;
import nl.colorize.multimedialib.stage.PolygonModel;
import nl.colorize.multimedialib.stage.Shader;
import nl.colorize.util.AppProperties;
import nl.colorize.util.Platform;
import nl.colorize.util.ResourceFile;

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

    private static final Texture.TextureFilter TEXTURE_FILTER = Texture.TextureFilter.Nearest;
    private static final int FONT_CACHE_SIZE = 100;
    private static final String CHARSET = "UTF-8";

    public GDXMediaLoader() {
        this.loaded = new ArrayList<>();
        this.fontCache = Cache.create(this::generateBitmapFont, FONT_CACHE_SIZE);
    }

    @Override
    public Image loadImage(FilePointer file) {
        Texture texture = new Texture(getFileHandle(file));
        texture.setFilter(TEXTURE_FILTER, TEXTURE_FILTER);
        loaded.add(texture);
        return new GDXImage(file, texture);
    }

    @Override
    public Audio loadAudio(FilePointer file) {
        return new MP3(new ResourceFile(file.path()));
    }

    @Override
    public OutlineFont loadFont(FilePointer file, FontStyle style) {
        return new GDXBitmapFont(this, getFileHandle(file), style);
    }

    protected BitmapFont getBitmapFont(FileHandle source, FontStyle style) {
        FontCacheKey cacheKey = new FontCacheKey(source, style);
        return fontCache.get(cacheKey);
    }

    private BitmapFont generateBitmapFont(FontCacheKey cacheKey) {
        FreeTypeFontGenerator.FreeTypeFontParameter config =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        config.size = cacheKey.style.size();
        config.color = toColor(cacheKey.style.color());

        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(cacheKey.source());
        BitmapFont bitmapFont = fontGenerator.generateFont(config);
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
    public Shader loadShader(FilePointer vertexShaderFile, FilePointer fragmentShaderFile) {
        String vertexGLSL = getFileHandle(vertexShaderFile).readString(CHARSET);
        String fragmentGLSL = getFileHandle(fragmentShaderFile).readString(CHARSET);

        ShaderProgram shaderProgram = new ShaderProgram(vertexGLSL, fragmentGLSL);
        loaded.add(shaderProgram);

        if (!shaderProgram.isCompiled()) {
            throw new MediaException("Failed to compile shader");
        }

        return new GDXShader(shaderProgram);
    }

    @Override
    public boolean containsResourceFile(FilePointer file) {
        return getFileHandle(file).exists();
    }

    @Override
    public AppProperties loadApplicationData(String appName, String fileName) {
        if (Platform.isWindows() || Platform.isMac()) {
            StandardMediaLoader delegate = new StandardMediaLoader();
            return delegate.loadApplicationData(appName, fileName);
        } else {
            Preferences preferences = Gdx.app.getPreferences(appName + "." + fileName);
            Properties properties = new Properties();

            for (String key : preferences.get().keySet()) {
                properties.setProperty(key, preferences.getString(key));
            }

            return AppProperties.from(properties);
        }
    }

    @Override
    public void saveApplicationData(Properties data, String appName, String fileName) {
        if (Platform.isWindows() || Platform.isMac()) {
            StandardMediaLoader delegate = new StandardMediaLoader();
            delegate.saveApplicationData(data, appName, fileName);
        } else {
            Preferences preferences = Gdx.app.getPreferences(appName);
            for (String property : data.stringPropertyNames()) {
                preferences.putString(property, data.getProperty(property));
            }
            preferences.flush();
        }
    }

    private FileHandle getFileHandle(FilePointer file) {
        return Gdx.files.internal(file.path());
    }

    @Override
    public void dispose() {
        loaded.forEach(Disposable::dispose);
        loaded.clear();
        fontCache.invalidateAll();
    }

    public static Color toColor(ColorRGB color) {
        return new Color(color.getR() / 255f, color.getG() / 255f, color.getB() / 255f, 1f);
    }

    private record FontCacheKey(FileHandle source, FontStyle style) {
    }
}
