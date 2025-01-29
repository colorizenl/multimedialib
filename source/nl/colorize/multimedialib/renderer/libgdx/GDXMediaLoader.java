//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.UBJsonReader;
import lombok.Getter;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.java2d.StandardMediaLoader;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.LoadStatus;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.SubscribableCollection;
import nl.colorize.util.stats.Cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Loads media assets using the libGDX framework.
 * <p>
 * and release the associated resources. This can be done globally for all
 * media files by calling {@link #dispose()}.
 */
public class GDXMediaLoader implements MediaLoader, Disposable {

    private List<Disposable> loaded;
    private Cache<FontFace, BitmapFont> fontCache;
    private StandardMediaLoader appDataDelegate;
    private GLTFLoader gltfLoader;
    private G3dModelLoader g3dLoader;
    @Getter private SubscribableCollection<LoadStatus> loadStatus;

    private static final Texture.TextureFilter TEXTURE_FILTER = Texture.TextureFilter.Linear;
    private static final int FONT_CACHE_SIZE = 100;
    private static final int BITMAP_FONT_SCALE = 2;

    public GDXMediaLoader() {
        this.loaded = new ArrayList<>();
        this.loadStatus = SubscribableCollection.wrap(new CopyOnWriteArrayList<>());
        this.fontCache = Cache.from(this::generateBitmapFont, FONT_CACHE_SIZE);
        this.appDataDelegate = new StandardMediaLoader();
        this.gltfLoader = new GLTFLoader();
        this.g3dLoader = new G3dModelLoader(new UBJsonReader(), new InternalFileHandleResolver());
    }

    @Override
    public Image loadImage(ResourceFile file) {
        Texture texture = new Texture(getFileHandle(file));
        texture.setFilter(TEXTURE_FILTER, TEXTURE_FILTER);
        loaded.add(texture);
        return new GDXImage(texture);
    }

    @Override
    public Audio loadAudio(ResourceFile file) {
        Sound sound = Gdx.audio.newSound(getFileHandle(file));
        loaded.add(sound);
        return new GDXAudio(sound);
    }

    @Override
    public FontFace loadFont(ResourceFile file, String family, int size, ColorRGB color) {
        FontFace font = new FontFace(file, family, size, color);
        getBitmapFont(font);
        return font;
    }

    protected BitmapFont getBitmapFont(FontFace font) {
        return fontCache.get(font);
    }

    private BitmapFont generateBitmapFont(FontFace font) {
        var config = new FreeTypeFontGenerator.FreeTypeFontParameter();
        config.size = font.size() * BITMAP_FONT_SCALE;
        config.color = toColor(font.color());
        config.minFilter = TEXTURE_FILTER;
        config.magFilter = TEXTURE_FILTER;

        FileHandle file = getFileHandle(font.origin());

        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(file);
        BitmapFont bitmapFont = fontGenerator.generateFont(config);
        bitmapFont.getData().setScale(1f / BITMAP_FONT_SCALE);
        fontGenerator.dispose();
        return bitmapFont;
    }

    @Override
    public String loadText(ResourceFile file) {
        return getFileHandle(file).readString("UTF-8");
    }

    @Override
    public Mesh loadModel(ResourceFile file) {
        Model model = loadModel(getFileHandle(file));
        return new GDXModel(model);
    }

    private Model loadModel(FileHandle file) {
        if (file.toString().endsWith(".g3db")) {
            return g3dLoader.loadModel(file);
        } else if (file.toString().endsWith(".gltf")) {
            SceneAsset sceneAsset = gltfLoader.load(file, true);
            return sceneAsset.scene.model;
        } else if (file.toString().endsWith(".obj")) {
            ObjLoader objLoader = new ObjLoader();
            return objLoader.loadModel(file);
        } else {
            throw new MediaException("Unsupported model file format: " + file);
        }
    }

    @Override
    public boolean containsResourceFile(ResourceFile file) {
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

    private FileHandle getFileHandle(ResourceFile file) {
        return Gdx.files.internal(file.path());
    }

    @Override
    public void dispose() {
        loaded.forEach(Disposable::dispose);
        loaded.clear();
        fontCache.invalidate();
    }

    protected static Color toColor(ColorRGB color) {
        return new Color(color.r() / 255f, color.g() / 255f, color.b() / 255f, 1f);
    }

    protected static ColorRGB toColor(Color color) {
        return new ColorRGB(Math.round(color.r * 255f), Math.round(color.g * 255f),
            Math.round(color.b * 255f));
    }
}
