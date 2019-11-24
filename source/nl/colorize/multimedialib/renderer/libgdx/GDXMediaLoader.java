//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import nl.colorize.multimedialib.renderer.Audio;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.math.MathUtils;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.util.LogHelper;
import nl.colorize.util.ResourceFile;
import org.teavm.apachecommons.io.Charsets;

import java.io.IOException;
import java.util.ArrayList;
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
public class GDXMediaLoader implements MediaLoader {

    private List<GDXTexture> loadedTextures;
    private List<GDXSound> loadedSounds;
    private Map<ColorRGB, Texture> colorTextureCache;

    private static final int COLOR_TEXTURE_SIZE = 8;
    private static final Logger LOGGER = LogHelper.getLogger(GDXMediaLoader.class);

    public GDXMediaLoader() {
        loadedTextures = new ArrayList<>();
        loadedSounds = new ArrayList<>();
        colorTextureCache = new HashMap<>();
    }

    @Override
    public Image loadImage(FilePointer file) {
        Texture texture = new Texture(getFileHandle(file));
        GDXTexture gdxTexture = new GDXTexture(texture);
        loadedTextures.add(gdxTexture);

        if (!checkTextureDimensions(texture)) {
            LOGGER.warning("Texture dimensions might not be supported by current platform: " +
                file.getPath() + " (" + texture.getWidth() + "x" + texture.getHeight() + ")");
        }

        return gdxTexture;
    }

    private boolean checkTextureDimensions(Texture texture) {
        return MathUtils.isPowerOfTwo(texture.getWidth()) &&
            MathUtils.isPowerOfTwo(texture.getHeight()) &&
            texture.getWidth() == texture.getHeight();
    }

    @Override
    public Audio loadAudio(FilePointer file) {
        Sound sound = Gdx.audio.newSound(getFileHandle(file));
        GDXSound gdxSound = new GDXSound(sound);
        loadedSounds.add(gdxSound);
        return gdxSound;
    }

    public Texture getColorTexture(ColorRGB color) {
        Texture colorTexture = colorTextureCache.get(color);
        if (colorTexture == null) {
            colorTexture = generateColorTexture(color);
            colorTextureCache.put(color, colorTexture);
        }
        return colorTexture;
    }

    private Texture generateColorTexture(ColorRGB color) {
        Pixmap pixelData = new Pixmap(COLOR_TEXTURE_SIZE, COLOR_TEXTURE_SIZE, Pixmap.Format.RGBA8888);
        pixelData.setColor(toColor(color));
        pixelData.fillRectangle(0, 0, COLOR_TEXTURE_SIZE, COLOR_TEXTURE_SIZE);
        return new Texture(pixelData);
    }

    private FileHandle getFileHandle(FilePointer file) {
        return Gdx.files.internal(file.getPath());
    }

    private Color toColor(ColorRGB color) {
        return new Color(color.getR() / 255f, color.getG() / 255f, color.getB() / 255f, 1f);
    }

    @Override
    public TTFont loadFont(String fontFamily, FilePointer file) {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String loadText(FilePointer file) {
        ResourceFile resourceFile = new ResourceFile(file.getPath());
        try {
            return resourceFile.read(Charsets.UTF_8);
        } catch (IOException e) {
            throw new MediaException("Cannot load file: " + file);
        }
    }

    public void dispose() {
        loadedTextures.forEach(texture -> texture.dispose());
        loadedTextures.clear();

        loadedSounds.forEach(sound -> sound.dispose());
        loadedSounds.clear();

        colorTextureCache.values().forEach(colorTexture -> colorTexture.dispose());
        colorTextureCache.clear();
    }

    @Override
    public boolean containsResourceFile(FilePointer file) {
        return new ResourceFile(file.getPath()).exists();
    }
}
