//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
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
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.UBJsonReader;
import com.google.common.base.Charsets;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.FontStyle;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.OutlineFont;
import nl.colorize.multimedialib.graphics.PolygonModel;
import nl.colorize.multimedialib.math.Cache;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.GeometryBuilder;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.java2d.MP3;
import nl.colorize.multimedialib.renderer.java2d.StandardMediaLoader;
import nl.colorize.util.Configuration;
import nl.colorize.util.Platform;
import nl.colorize.util.ResourceFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Position;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.TextureCoordinates;

/**
 * Loads media assets using the libGDX framework.
 * <p>
 * libGDX requires an explicit dispose step to close all loaded media files
 * and release the associated resources. This can be done globally for all
 * media files by calling {@link #dispose()}.
 */
public class GDXMediaLoader implements MediaLoader, GeometryBuilder, Disposable {

    private List<Disposable> loaded;
    private Cache<FontCacheKey, BitmapFont> fontCache;

    private static final int FONT_CACHE_SIZE = 100;

    public GDXMediaLoader() {
        this.loaded = new ArrayList<>();
        this.fontCache = Cache.create(this::generateBitmapFont, FONT_CACHE_SIZE);
    }

    @Override
    public Image loadImage(FilePointer file) {
        Texture texture = new Texture(getFileHandle(file));
        loaded.add(texture);

        return new GDXImage(file, texture);
    }

    @Override
    public Audio loadAudio(FilePointer file) {
        return new MP3(new ResourceFile(file.getPath()));
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
        ResourceFile resourceFile = new ResourceFile(file.getPath());
        return resourceFile.read(Charsets.UTF_8);
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

    private PolygonModel createInstance(Model model) {
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
    public Configuration loadApplicationData(String appName, String fileName) {
        if (Platform.isWindows() || Platform.isMac()) {
            StandardMediaLoader delegate = new StandardMediaLoader();
            return delegate.loadApplicationData(appName, fileName);
        } else {
            Preferences preferences = Gdx.app.getPreferences(appName + "." + fileName);
            Map<String, String> data = new LinkedHashMap<>();

            for (String key : preferences.get().keySet()) {
                data.put(key, preferences.getString(key));
            }

            return Configuration.fromProperties(data);
        }
    }

    @Override
    public void saveApplicationData(Configuration data, String appName, String fileName) {
        if (Platform.isWindows() || Platform.isMac()) {
            StandardMediaLoader delegate = new StandardMediaLoader();
            delegate.saveApplicationData(data, appName, fileName);
        } else {
            Preferences preferences = Gdx.app.getPreferences(appName);
            for (String property : data.getPropertyNames()) {
                preferences.putString(property, data.get(property));
            }
            preferences.flush();
        }
    }

    private FileHandle getFileHandle(FilePointer file) {
        return getFileHandle(file.getPath());
    }

    private FileHandle getFileHandle(String path) {
        return Gdx.files.internal(path);
    }

    public Color toColor(ColorRGB color) {
        return new Color(color.getR() / 255f, color.getG() / 255f, color.getB() / 255f, 1f);
    }

    @Override
    public PolygonModel createQuad(Point2D size, ColorRGB color) {
        return createBox(new Point3D(size.getX(), 0.001f, size.getY()), color);
    }

    @Override
    public PolygonModel createQuad(Point2D size, Image texture) {
        return createBox(new Point3D(size.getX(), 0.001f, size.getY()), texture);
    }

    @Override
    public PolygonModel createBox(Point3D size, ColorRGB color) {
        ModelBuilder modelBuilder = new ModelBuilder();
        Model model = modelBuilder.createBox(size.getX(), size.getY(), size.getZ(),
            createMaterial(color), Position | Normal);
        return createInstance(model);
    }

    @Override
    public PolygonModel createBox(Point3D size, Image texture) {
        ModelBuilder modelBuilder = new ModelBuilder();
        Model model = modelBuilder.createBox(size.getX(), size.getY(), size.getZ(),
            createMaterial((GDXImage) texture), Position | Normal | TextureCoordinates);
        return createInstance(model);
    }

    @Override
    public PolygonModel createSphere(float diameter, ColorRGB color) {
        ModelBuilder modelBuilder = new ModelBuilder();
        Model model = modelBuilder.createSphere(diameter, diameter, diameter, 32, 32,
            createMaterial(color), Position | Normal);
        return createInstance(model);
    }

    @Override
    public PolygonModel createSphere(float diameter, Image texture) {
        ModelBuilder modelBuilder = new ModelBuilder();
        Model model = modelBuilder.createSphere(diameter, diameter, diameter, 32, 32,
            createMaterial((GDXImage) texture), Position | Normal | TextureCoordinates);
        return createInstance(model);
    }

    private Material createMaterial(ColorRGB color) {
        ColorAttribute colorAttr = ColorAttribute.createDiffuse(toColor(color));
        return new Material(colorAttr);
    }

    private Material createMaterial(GDXImage texture) {
        TextureAttribute colorAttr = TextureAttribute.createDiffuse(texture.getTextureRegion());
        return new Material(colorAttr);
    }

    @Override
    public void dispose() {
        loaded.forEach(Disposable::dispose);
        loaded.clear();
        fontCache.invalidateAll();
    }

    private record FontCacheKey(FileHandle source, FontStyle style) {
    }
}
