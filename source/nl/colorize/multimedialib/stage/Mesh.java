//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

/**
 * Polygon mesh that can be used to render 3D graphics. Simple geometry can
 * be created programmatically, more advanced meshes can be loaded from 3D
 * model files.
 */
public interface Mesh extends StageNode3D {

    public void applyColor(ColorRGB color);

    public void applyTexture(Image texture);

    public void applyDynamicTexture(Sprite sprite);

    /**
     * If this mesh is currently using a dynamic texture, returns the sprite
     * that is used as its source. Returns {@code null} if this mesh is not
     * using a dynamic texture.
     */
    public Sprite getDynamicTexture();

    /**
     * Creates a copy of this mesh, which will use the same geometry,
     * materials, textures, and animations as this mesh. The copy is not
     * yet added to the stage.
     */
    public Mesh copy();
}
