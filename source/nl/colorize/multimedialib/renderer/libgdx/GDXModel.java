//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import lombok.Getter;
import nl.colorize.multimedialib.scene.Timer;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Transform3D;

import static lombok.AccessLevel.PROTECTED;
import static nl.colorize.multimedialib.stage.ColorRGB.WHITE;

public class GDXModel implements Mesh {

    @Getter(PROTECTED) private ModelInstance modelInstance;
    @Getter private Transform3D transform;
    @Getter private Transform3D globalTransform;
    @Getter private Sprite dynamicTexture;

    protected GDXModel(ModelInstance modelInstance) {
        this.modelInstance = modelInstance;
        this.transform = new Transform3D();
        this.globalTransform = new Transform3D();

        for (Material material : modelInstance.materials) {
            material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        }
    }

    protected GDXModel(Model model) {
        this(new ModelInstance(model));
    }

    @Override
    public void animate(Timer animationTimer) {
        if (dynamicTexture != null) {
            dynamicTexture.animate(animationTimer);
            applyTexture(dynamicTexture.getCurrentGraphics());
        }
    }

    @Override
    public void applyColor(ColorRGB color) {
        ColorAttribute colorAttr = ColorAttribute.createDiffuse(GDXMediaLoader.toColor(color));
        for (Material material : modelInstance.materials) {
            material.set(colorAttr);
        }
    }

    @Override
    public void applyTexture(Image texture) {
        GDXImage gdxImage = (GDXImage) texture;
        ColorAttribute colorAttr = ColorAttribute.createDiffuse(GDXMediaLoader.toColor(WHITE));
        TextureAttribute textureAttr = TextureAttribute.createDiffuse(gdxImage.getTextureRegion());
        for (Material material : modelInstance.materials) {
            material.set(colorAttr);
            material.set(textureAttr);
        }
    }

    @Override
    public void applyDynamicTexture(Sprite sprite) {
        dynamicTexture = sprite;
        applyTexture(dynamicTexture.getCurrentGraphics());
    }

    @Override
    public Mesh copy() {
        return new GDXModel(new ModelInstance(modelInstance.model));
    }
}
