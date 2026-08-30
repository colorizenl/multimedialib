//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
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
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.util.Platform;
import nl.colorize.util.PropertyUtils;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.Subject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static nl.colorize.multimedialib.renderer.java2d.StandardMediaLoader.APPLICATION_DATA_FILE_NAME;

/**
 * Loads media assets using the libGDX framework. Media is loaded using "pure"
 * libGDX and is therefore cross-platform. The only exception is loading and
 * saving application data, which requires a platform-specific delegate
 * (i.e. desktop or browser) that is provided via the constructor.
 * <p>
 * libGDX requires an explicit "dispose" step to close all loaded media files
 * and release the associated resources. This can be done globally for all
 * media files by calling {@link #dispose()}.
 */
public class GDXMediaLoader implements MediaLoader, Disposable {

    private List<Disposable> loaded;
    private GLTFLoader gltfLoader;
    private G3dModelLoader g3dLoader;
    @Getter private Subject<Audio> audioQueue;

    private static final Texture.TextureFilter TEXTURE_FILTER = Texture.TextureFilter.Linear;

    public GDXMediaLoader() {
        this.loaded = new ArrayList<>();
        this.gltfLoader = new GLTFLoader();
        this.g3dLoader = new G3dModelLoader(new UBJsonReader(), new InternalFileHandleResolver());
        this.audioQueue = new Subject<>();
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
        return new GDXAudio(sound, audioQueue);
    }

    @Override
    public FontFace loadFont(ResourceFile file, String family, int size, ColorRGB color) {
        return new FontFace(file, family, size, color);
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
    public String loadText(ResourceFile file) {
        return getFileHandle(file).readString("UTF-8");
    }

    @Override
    public Properties loadApplicationData(String appName) {
        FileHandle dataFile = getApplicationDataFile(appName);
        String contents = dataFile.exists() ? dataFile.readString("UTF-8") : "";
        return PropertyUtils.loadProperties(contents);
    }

    @Override
    public void saveApplicationData(String appName, Properties data) {
        FileHandle dataFile = getApplicationDataFile(appName);
        dataFile.writeString(PropertyUtils.serializeProperties(data), false, "UTF-8");
    }

    private FileHandle getApplicationDataFile(String appName) {
        File dataFile = Platform.getApplicationData(appName, APPLICATION_DATA_FILE_NAME);
        return Gdx.files.absolute(dataFile.getAbsolutePath());
    }

    @Override
    public boolean containsResourceFile(ResourceFile file) {
        return getFileHandle(file).exists();
    }

    @Override
    public void dispose() {
        loaded.forEach(Disposable::dispose);
        loaded.clear();
    }

    protected static FileHandle getFileHandle(ResourceFile file) {
        return Gdx.files.internal(file.path());
    }

    protected static Color toColor(ColorRGB color) {
        return new Color(color.r() / 255f, color.g() / 255f, color.b() / 255f, 1f);
    }

    protected static ColorRGB toColor(Color color) {
        return new ColorRGB(
            Math.round(color.r * 255f),
            Math.round(color.g * 255f),
            Math.round(color.b * 255f)
        );
    }
}
