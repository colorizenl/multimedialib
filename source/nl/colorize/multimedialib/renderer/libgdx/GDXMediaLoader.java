//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.UBJsonReader;
import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.AnimationInfo;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.PolygonMesh;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.java2d.MP3;
import nl.colorize.util.LogHelper;
import nl.colorize.util.ResourceFile;
import org.teavm.apachecommons.io.Charsets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Loads media assets using the libGDX framework.
 * <p>
 * libGDX requires an explicit dispose step to close all loaded media files
 * and release the associated resources. This can be done globally for all
 * media files by calling {@link #dispose()}.
 */
public class GDXMediaLoader implements MediaLoader, Disposable {

    private G3dModelLoader modelLoader;
    private List<Disposable> loaded;
    private Map<PolygonMesh, Model> models;
    private Map<TTFont, BitmapFont> fonts;
    private Map<String, FileHandle> fontLocations;
    private Map<ColorRGB, Texture> colorTextureCache;

    private static final int COLOR_TEXTURE_SIZE = 8;
    private static final Logger LOGGER = LogHelper.getLogger(GDXMediaLoader.class);

    public GDXMediaLoader() {
        this.modelLoader = new G3dModelLoader(new UBJsonReader(), new InternalFileHandleResolver());
        this.loaded = new ArrayList<>();
        this.models = new HashMap<>();
        this.fonts = new HashMap<>();
        this.fontLocations = new HashMap<>();
        this.colorTextureCache = new HashMap<>();
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

    public Texture getColorTexture(ColorRGB color) {
        Texture colorTexture = colorTextureCache.get(color);
        if (colorTexture == null) {
            colorTexture = generateColorTexture(color);
            colorTextureCache.put(color, colorTexture);
            loaded.add(colorTexture);
        }
        return colorTexture;
    }

    private Texture generateColorTexture(ColorRGB color) {
        Pixmap pixelData = new Pixmap(COLOR_TEXTURE_SIZE, COLOR_TEXTURE_SIZE, Pixmap.Format.RGBA8888);
        pixelData.setColor(toColor(color));
        pixelData.fillRectangle(0, 0, COLOR_TEXTURE_SIZE, COLOR_TEXTURE_SIZE);
        return new Texture(pixelData);
    }

    @Override
    public TTFont loadFont(FilePointer file, String family, int size, ColorRGB color, boolean bold) {
        fontLocations.put(family, getFileHandle(file));
        return new TTFont(family, size, color, bold);
    }

    protected BitmapFont getBitmapFont(TTFont font, int actualSize) {
        TTFont cacheKey = new TTFont(font.getFamily(), actualSize, font.getColor(), font.isBold());
        BitmapFont bitmapFont = fonts.get(cacheKey);

        if (bitmapFont == null) {
            bitmapFont = loadBitmapFont(font, actualSize);
            fonts.put(cacheKey, bitmapFont);
            loaded.add(bitmapFont);
        }

        return bitmapFont;
    }

    private BitmapFont loadBitmapFont(TTFont font, int actualSize) {
        FileHandle fontLocation = fontLocations.get(font.getFamily());
        Preconditions.checkArgument(fontLocation != null, "Unknown font location: " + font);

        FreeTypeFontGenerator.FreeTypeFontParameter config =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        config.size = actualSize;
        config.color = toColor(font.getColor());

        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(fontLocation);
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
    public PolygonMesh loadMesh(FilePointer file) {
        Preconditions.checkArgument(file.getPath().endsWith(".fbx"),
            "Only .fbx files are sypported, they will be converted to .g3db during loading");

        String path = file.getPath().substring(0, file.getPath().length() - 4) + ".g3db";

        Model model = modelLoader.loadModel(getFileHandle(path));
        List<AnimationInfo> animations = parseModelAnimations(model);
        PolygonMesh mesh = new PolygonMesh(file.getPath(), animations);

        loaded.add(model);
        models.put(mesh, model);

        return mesh;
    }

    private List<AnimationInfo> parseModelAnimations(Model model) {
        List<AnimationInfo> animations = new ArrayList<>();
        for (Animation anim : model.animations) {
            animations.add(new AnimationInfo(anim.id, anim.duration));
        }
        return animations;
    }

    protected PolygonMesh registerMesh(String name, Model modelData) {
        PolygonMesh mesh = new PolygonMesh(name, Collections.emptyList());
        loaded.add(modelData);
        models.put(mesh, modelData);
        return mesh;
    }

    protected Model getModelData(PolygonMesh mesh) {
        Model modelData = models.get(mesh);
        Preconditions.checkState(modelData != null, "Model data not loaded: " + mesh);
        return modelData;
    }

    @Override
    public boolean containsResourceFile(FilePointer file) {
        return new ResourceFile(file.getPath()).exists();
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
    public void dispose() {
        for (Disposable disposable : loaded) {
            disposable.dispose();
        }

        loaded.clear();
        models.clear();
        fonts.clear();
        colorTextureCache.clear();
    }
}
